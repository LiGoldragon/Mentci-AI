# Strategy: Chronos Calibration

## Objective
Implement a calibration mechanism for `chronos` to ensure high-fidelity solar time tracking.

## Roadmap
1. [ ] **Phase 1: Research**
    - [x] Identify sources of inaccuracy.
    - [x] Identify JPL Horizons as the calibration source.
2. [ ] **Phase 2: Feature Implementation**
    - [ ] Add `calibrate` subcommand to `chronos`.
    - [ ] Implement data fetching from JPL Horizons.
    - [ ] Calculate and display the longitude delta.
3. [ ] **Phase 3: Integration**
    - [ ] Apply calibration offsets.
    - [ ] Automate periodic recalibration.

## Architectural Notes
- **Isolation:** Ephemeris data must live in `/tmp/`.
- **Durability:** The calibration offset should be considered a `Stable Contract` once applied.
