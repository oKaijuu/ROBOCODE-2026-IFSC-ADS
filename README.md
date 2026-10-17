# Robocode — IFSC ADS 2026

This repository tracks the original robot and each Madara iteration on separate branches.

## Robot history

| Branch | Robot | Purpose |
|---|---|---|
| [`stationary-tracker`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/stationary-tracker) | **StationaryTracker** | Original robot and baseline for comparisons. |
| [`madara-v1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v1) | **Madara V1** | First Madara iteration. |
| [`madara-v2`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2) | **Madara V2** | Second iteration. |
| [`madara-v3`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3) | **Madara V3** | Third iteration, including its properties and learning data. |
| [`experiment/madara-v3-melee-movement`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/experiment/madara-v3-melee-movement) | **Madara V3 experiment** | Experimental melee-movement work and actual robot in use. |

## Repository organization

- `main` keeps **StationaryTracker.java** as the original baseline and entry point to the project history.
- Each version branch contains the baseline source and only the corresponding Madara source.
- Compiled `.class` files and the temporary `Test` robot are not tracked in the organized branches.
- Madara V3's `MadaraV3.properties` and `MadaraV3.data/` are preserved on `madara-v3`.

Switch branches to inspect or run a specific version. Keep generated Robocode build artifacts out of version control.
