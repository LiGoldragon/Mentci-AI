# Research Artifact: Chronos Calibration Success

- **Solar:** ♓︎ 6° 3' 44" | 5919 AM
- **Subject:** `Chronos-Calibration`
- **Title:** `chronos-calibration-success`
- **Status:** `finalized`

## 1. Summary
The `chronos` utility has been successfully calibrated against the NASA JPL Horizons system. A discrepancy of approximately **-14.3 arcseconds (-0.003975°)** was identified between the local low-precision formulas and the high-fidelity ephemeris data.

## 2. Implementation
*   **Subcommand:** Added `chronos calibrate` to fetch current solar longitude from JPL Horizons.
*   **Networking:** Added `reqwest` (with `rustls-tls`) to handle API requests without system OpenSSL dependencies.
*   **Offset Persistence:** Calibration offsets are stored in a temporary session file (`/tmp/mentci-chronos-calibration.json`). This ensures that the data is not picked up by VCS while remaining available for all subsequent `chronos` calls in the current environment.

## 3. Verification
*   **Initial:** Calculated longitude was slightly ahead of JPL Horizons.
*   **Post-Calibration:** `chronos` now applies the identified offset, bringing the local solar clock into alignment with the NASA "gold standard."

## 4. Maintenance
Recalibration is recommended periodically (e.g., once per session or monthly) to account for cumulative drift in simplified formulas.
