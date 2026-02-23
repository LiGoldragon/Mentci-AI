# Research Artifact: Chronos Accuracy and Calibration

- **Solar:** ♓︎ 6° 3' 49" | 5919 AM
- **Subject:** `Chronos-Calibration`
- **Title:** `chronos-accuracy-and-calibration`
- **Status:** `in-progress`

## 1. Problem Statement
The `chronos` tool is reported to give "slightly-untrue solar time." Preliminary analysis of `Components/chronos/src/main.rs` shows that it uses simplified low-precision astronomical formulas (Meeus Ch 25) which are accurate to approximately 0.01 degrees (~36 arcseconds).

## 2. Potential Sources of Inaccuracy
*   **Formula Precision:** The current implementation uses a truncated set of perturbation terms. High precision requires more terms or direct ephemeris lookups (JPL DE440).
*   **Time Scales:** `chronos` uses UTC (`SystemTime::now()`), but astronomical ephemerides expect **Terrestrial Time (TT)** or **Barycentric Dynamical Time (TDB)**. The difference (**Delta T**) is currently ~69 seconds and grows.
*   **Coordinate Frame:** Ensuring the ecliptic of date vs J2000 is correctly handled.

## 3. Calibration Source: NASA JPL Horizons
The gold standard for planetary positions is the NASA JPL Horizons system.
*   **Target Body:** `10` (Sun)
*   **Observer Location:** `500@399` (Earth Geocenter)
*   **Quantity:** `31` (Observer-centered ecliptic longitude)
*   **Time Scale:** TDB

## 4. Proposed Feature: `chronos calibrate`
A new feature to fetch high-precision data from Horizons and calculate a local calibration offset.
*   **Data Storage:** Ephemeris data should be stored in a temporary directory (e.g., `/tmp/mentci-chronos-ephem/`) to avoid VCS noise.
*   **Calibration:** Apply an offset to the calculated solar longitude to align with Horizons for the current epoch.

## 5. Implementation Strategy
1.  Add a `calibrate` subcommand to `chronos`.
2.  Use `web_fetch` (or internal Rust `reqwest` if available) to query JPL Horizons.
3.  Store results in a temp dir.
4.  Compare `chronos` output with Horizons and output the delta.
5.  Allow applying this delta as a persistent (or session-local) offset.
