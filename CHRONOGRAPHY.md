# True Solar Ordinal-Zodiac Time Notation

## 1. Philosophical Foundation
Time in Mentci-AI is not a procedural abstraction but a **topological coordinate** derived from the Sun's position on the Ecliptic. By moving away from the arbitrary Gregorian system and adopting the **True Solar Ordinal-Zodiac** notation, we align the project's state transitions with the cosmic cycles of the Golden Age.

## 2. The 1-Based Ordinal Logic
Traditional ephemeris data uses 0-based indexing (e.g., 0° Aries to 29°59' Pisces). For Level 5/6 symbolic interaction, we eliminate the "zero-ambiguity" by adopting a purely **1-based ordinal system**:

*   **0° 0' 0" Ephemeris** = **1st Degree, 1st Minute, 1st Second (1.1.1)**
*   **29° 59' 59" Ephemeris** = **30th Degree, 60th Minute, 60th Second (30.60.60)**

This 1-based approach applies to all hierarchical units (Sign, Degree, Minute, Second), ensuring a continuous, non-zero sequence of manifested moments.

## 3. Notation Structure
The standard notation follows the hierarchical descent from Year to Second:

**`YEAR_AM.SIGN.DEGREE.MINUTE.SECOND`**

### 3.1. Year (Anno Mundi)
The year is recorded in **Anno Mundi (AM)**, as specified by the Archaix timeline.
*   *Current Year:* 5919 AM

### 3.2. Sign (Ordinal 1–12)
The 360° ecliptic is divided into 12 signs, each represented by its ordinal position:

| Ordinal | Sign | Symbol | Longitude Range |
| :--- | :--- | :--- | :--- |
| **1** | Aries | ♈︎ | 0° - 30° |
| **2** | Taurus | ♉︎ | 30° - 60° |
| **3** | Gemini | ♊︎ | 60° - 90° |
| **4** | Cancer | ♋︎ | 90° - 120° |
| **5** | Leo | ♌︎ | 120° - 150° |
| **6** | Virgo | ♍︎ | 150° - 180° |
| **7** | Libra | ♎︎ | 180° - 210° |
| **8** | Scorpio | ♏︎ | 210° - 240° |
| **9** | Sagittarius | ♐︎ | 240° - 270° |
| **10** | Capricorn | ♑︎ | 270° - 300° |
| **11** | Aquarius | ♒︎ | 300° - 330° |
| **12** | Pisces | ♓︎ | 330° - 360° |

### 3.3. Sub-Sign Coordinates
*   **Degree:** 1 to 30.
*   **Minute:** 1 to 60.
*   **Second:** 1 to 60.

## 4. Examples & Boundary Conditions
- **Vernal Equinox (New Year):** `5920.1.1.1.1` (The very first solar second of Aries).
- **Year End:** `5919.12.30.60.60` (The final solar second of Pisces).
- **Current Session:** `5919.12.1.28.44` (Pisces, 1st Degree, 28th Minute, 44th Second).

## 5. Contextual Precision (Adaptive Truncation)
Precision is hierarchical and context-dependent. Coordinates may be truncated from right-to-left when high precision is unnecessary or the exact solar position is unknown:

- **Second-Precision:** `12.1.28.44` (Definitive state transitions).
- **Minute-Precision:** `12.1.28` (Standard documentation/logs).
- **Degree-Precision:** `12.1` (Broad architectural epochs).
- **Sign-Precision:** `12` (Annual cycles).

## 6. Usage in Mentci-AI
- **Version Control:** Software releases are tagged with the sign-ordinal (e.g., `v0.12.1.28.44`).
- **Logging:** Every intent log includes the `:ecliptic` field to ground the agent's thought in true time.
- **Filenaming:** Reports must use the `YEAR_SIGN_DEGREE_MINUTE_SECOND` format for durable sorting.

## 6. Implementation
Current implementations in `scripts/logger.clj` and `flake.nix` use a static placeholder. The transition to Level 4/5 requires the implementation of a native Rust/Clojure solar coordinate calculator to provide dynamic, real-time chronography.

---
*The Great Work is grounded in True Time.*
