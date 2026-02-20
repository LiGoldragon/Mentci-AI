use std::f64::consts::PI;
use std::time::{SystemTime, UNIX_EPOCH};

#[derive(Debug, Clone, Copy)]
struct EclipticTime {
    year_am: i32,
    sign_ordinal: i32,
    degree: i32,
    minute: i32,
    second: i32,
    sign_symbol: &'static str,
    sign_name: &'static str,
}

#[derive(Debug, Clone, Copy)]
enum Precision {
    Sign,
    Degree,
    Minute,
    Second,
}

#[derive(Debug, Clone, Copy)]
enum OutputFormat {
    Version,
    Numeric,
    Unicode,
    Am,
    Json,
}

fn main() {
    let args: Vec<String> = std::env::args().skip(1).collect();
    let format = parse_format(&args).unwrap_or(OutputFormat::Unicode);
    let precision = parse_precision(&args).unwrap_or(Precision::Second);

    let now = SystemTime::now();
    let ecliptic = solar_ecliptic_time(now);
    let output = format_output(ecliptic, format, precision);
    println!("{output}");
}

fn parse_format(args: &[String]) -> Option<OutputFormat> {
    let value = value_after_flag(args, "--format")?;
    match value.as_str() {
        "version" => Some(OutputFormat::Version),
        "numeric" => Some(OutputFormat::Numeric),
        "unicode" => Some(OutputFormat::Unicode),
        "am" => Some(OutputFormat::Am),
        "json" => Some(OutputFormat::Json),
        _ => None,
    }
}

fn parse_precision(args: &[String]) -> Option<Precision> {
    let value = value_after_flag(args, "--precision")?;
    match value.as_str() {
        "sign" => Some(Precision::Sign),
        "degree" => Some(Precision::Degree),
        "minute" => Some(Precision::Minute),
        "second" => Some(Precision::Second),
        _ => None,
    }
}

fn value_after_flag(args: &[String], flag: &str) -> Option<String> {
    args.iter()
        .position(|arg| arg == flag)
        .and_then(|idx| args.get(idx + 1))
        .cloned()
}

fn format_output(time: EclipticTime, format: OutputFormat, precision: Precision) -> String {
    match format {
        OutputFormat::Version => format!(
            "v0.{}.{}{}",
            time.sign_ordinal,
            time.degree,
            format_precision_suffix(time, precision)
        ),
        OutputFormat::Numeric => format!(
            "{}.{}{} | {} AM",
            time.sign_ordinal,
            time.degree,
            format_precision_suffix(time, precision),
            time.year_am
        ),
        OutputFormat::Unicode => format!(
            "{} {}{} | {} AM",
            time.sign_symbol,
            time.degree,
            format_unicode_suffix(time, precision),
            time.year_am
        ),
        OutputFormat::Am => format!(
            "{}.{}.{}{}",
            time.year_am,
            time.sign_ordinal,
            time.degree,
            format_precision_suffix(time, precision)
        ),
        OutputFormat::Json => format!(
            "{{\"year_am\":{},\"sign\":{},\"degree\":{},\"minute\":{},\"second\":{},\"symbol\":\"{}\",\"name\":\"{}\"}}",
            time.year_am,
            time.sign_ordinal,
            time.degree,
            time.minute,
            time.second,
            time.sign_symbol,
            time.sign_name
        ),
    }
}

fn format_precision_suffix(time: EclipticTime, precision: Precision) -> String {
    match precision {
        Precision::Sign => String::new(),
        Precision::Degree => String::new(),
        Precision::Minute => format!(".{}", time.minute),
        Precision::Second => format!(".{}.{}", time.minute, time.second),
    }
}

fn format_unicode_suffix(time: EclipticTime, precision: Precision) -> String {
    match precision {
        Precision::Sign => "".to_string(),
        Precision::Degree => "°".to_string(),
        Precision::Minute => format!("° {}'", time.minute),
        Precision::Second => format!("° {}' {}\"", time.minute, time.second),
    }
}

fn solar_ecliptic_time(now: SystemTime) -> EclipticTime {
    let jd = julian_day(now);
    let t = (jd - 2451545.0) / 36525.0;

    let mean_long = normalize_degrees(280.46646 + 36000.76983 * t + 0.0003032 * t * t);
    let mean_anomaly = normalize_degrees(357.52911 + 35999.05029 * t - 0.0001537 * t * t);

    let c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * deg_sin(mean_anomaly)
        + (0.019993 - 0.000101 * t) * deg_sin(2.0 * mean_anomaly)
        + 0.000289 * deg_sin(3.0 * mean_anomaly);

    let true_long = normalize_degrees(mean_long + c);
    let (sign_index, degree, minute, second) = zodiac_ordinals(true_long);
    let (symbol, name) = zodiac_symbol(sign_index);

    let year_am = gregorian_to_am_year(now);

    EclipticTime {
        year_am,
        sign_ordinal: sign_index + 1,
        degree,
        minute,
        second,
        sign_symbol: symbol,
        sign_name: name,
    }
}

fn julian_day(time: SystemTime) -> f64 {
    let unix_seconds = time
        .duration_since(UNIX_EPOCH)
        .unwrap_or_default()
        .as_secs_f64();
    2440587.5 + unix_seconds / 86400.0
}

fn deg_sin(deg: f64) -> f64 {
    (deg * PI / 180.0).sin()
}

fn normalize_degrees(mut value: f64) -> f64 {
    value %= 360.0;
    if value < 0.0 {
        value += 360.0;
    }
    value
}

fn zodiac_ordinals(longitude: f64) -> (i32, i32, i32, i32) {
    let sign_index = (longitude / 30.0).floor() as i32;
    let within_sign = longitude - (sign_index as f64 * 30.0);

    let degree_zero = within_sign.floor();
    let minute_float = (within_sign - degree_zero) * 60.0;
    let minute_zero = minute_float.floor();
    let second_float = (minute_float - minute_zero) * 60.0;
    let second_zero = second_float.floor();

    let degree = clamp_ordinal(degree_zero as i32 + 1, 30);
    let minute = clamp_ordinal(minute_zero as i32 + 1, 60);
    let second = clamp_ordinal(second_zero as i32 + 1, 60);

    (sign_index, degree, minute, second)
}

fn clamp_ordinal(value: i32, max: i32) -> i32 {
    if value < 1 {
        1
    } else if value > max {
        max
    } else {
        value
    }
}

fn zodiac_symbol(sign_index: i32) -> (&'static str, &'static str) {
    match sign_index {
        0 => ("♈︎", "Aries"),
        1 => ("♉︎", "Taurus"),
        2 => ("♊︎", "Gemini"),
        3 => ("♋︎", "Cancer"),
        4 => ("♌︎", "Leo"),
        5 => ("♍︎", "Virgo"),
        6 => ("♎︎", "Libra"),
        7 => ("♏︎", "Scorpio"),
        8 => ("♐︎", "Sagittarius"),
        9 => ("♑︎", "Capricorn"),
        10 => ("♒︎", "Aquarius"),
        11 => ("♓︎", "Pisces"),
        _ => ("♈︎", "Aries"),
    }
}

fn gregorian_to_am_year(time: SystemTime) -> i32 {
    let unix_seconds = time
        .duration_since(UNIX_EPOCH)
        .unwrap_or_default()
        .as_secs();
    let days = unix_seconds / 86_400;
    let (year, _, _) = civil_from_days(days as i64);
    year + 3893
}

// Civil date conversion from days since 1970-01-01.
fn civil_from_days(days: i64) -> (i32, u32, u32) {
    let z = days + 719468;
    let era = if z >= 0 { z } else { z - 146096 } / 146097;
    let doe = z - era * 146097;
    let yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365;
    let y = yoe + era * 400;
    let doy = doe - (365 * yoe + yoe / 4 - yoe / 100);
    let mp = (5 * doy + 2) / 153;
    let d = doy - (153 * mp + 2) / 5 + 1;
    let m = mp + if mp < 10 { 3 } else { -9 };
    let year = (y + if m <= 2 { 1 } else { 0 }) as i32;
    (year, m as u32, d as u32)
}
