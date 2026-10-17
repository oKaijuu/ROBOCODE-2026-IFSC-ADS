<div align="center">

<img src="https://media1.tenor.com/m/EpgxcRD1ydsAAAAd/madara-uchiha.gif" width="100%" alt="Madara Uchiha animated banner" />

# ROBOCODE · IFSC ADS 2026

### An evolving Robocode AI project

**One baseline. Three generations. Continuous experimentation.**

[![Java](https://img.shields.io/badge/Language-Java-cc3333?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Robocode](https://img.shields.io/badge/Platform-Robocode-6f42c1?style=for-the-badge)](https://robocode.sourceforge.io/)
[![Versions](https://img.shields.io/badge/Madara-V1%20%7C%20V2%20%7C%20V3-bb8f35?style=for-the-badge)](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/branches)

[**Explore the versions**](#-robot-lineup) · [**Branch map**](#-branch-map) · [**Experiments**](#-experiments)

</div>

---

## Project overview

This repository documents the development of Robocode combat robots, starting with **StationaryTracker** as the original baseline and continuing through the **Madara V1, V2, and V3** iterations.

Each version has its own branch so its source can be inspected and compared without mixing the implementations. The `main` branch serves as the project landing page and preserves the original baseline source.

## ⚔ Robot lineup

<table>
  <tr>
    <td width="25%" align="center">
      <img src="https://media1.tenor.com/m/xaurczvDhkwAAAAd/naruto-uchiha-madara.gif" width="100%" alt="Madara animation" />
      <h3>StationaryTracker</h3>
      <strong>Original baseline</strong>
      <p>Starting point for performance comparisons and the project's first robot.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/stationary-tracker">Open branch ↗</a>
    </td>
    <td width="25%" align="center">
      <img src="https://media1.tenor.com/m/NRXWuz3FwQEAAAAd/weee.gif" width="100%" alt="Madara animation" />
      <h3>Madara V1</h3>
      <strong>First generation</strong>
      <p>The first Madara implementation, kept separate for direct comparison.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v1">Open branch ↗</a>
    </td>
    <td width="25%" align="center">
      <img src="https://media1.tenor.com/m/xaurczvDhkwAAAAd/naruto-uchiha-madara.gif" width="100%" alt="Madara animation" />
      <h3>Madara V2</h3>
      <strong>Second generation</strong>
      <p>A separate iteration for tracking changes from the V1 design.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2">Open branch ↗</a>
    </td>
    <td width="25%" align="center">
      <img src="https://media1.tenor.com/m/EpgxcRD1ydsAAAAd/madara-uchiha.gif" width="100%" alt="Madara animation" />
      <h3>Madara V3</h3>
      <strong>Learning & experimentation</strong>
      <p>Includes its configuration and persisted learning data.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3">Open branch ↗</a>
    </td>
  </tr>
</table>

## 🗂 Branch map

| Branch | Role | What it contains |
|---|---|---|
| [`main`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/main) | Project index | This README and the original `StationaryTracker.java` baseline |
| [`stationary-tracker`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/stationary-tracker) | Baseline | Original robot source |
| [`madara-v1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v1) | Version 1 | `MadaraV1.java` and baseline source |
| [`madara-v2`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2) | Version 2 | `MadaraV2.java` and baseline source |
| [`madara-v3`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3) | Version 3 | `MadaraV3.java`, properties, learning data and baseline source |
| [`experiment/madara-v3-melee-movement`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/experiment/madara-v3-melee-movement) | Experimental | V3 melee-movement work and experiment documentation |

## 🧪 Experiments

Experimental changes should remain isolated from the version branches until they are ready to be evaluated. The current melee-movement work has its own branch:

<div align="center">

[![Open experiment branch](https://img.shields.io/badge/EXPERIMENT-Madara%20V3%20Melee%20Movement-6f42c1?style=for-the-badge&logo=github)](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/experiment/madara-v3-melee-movement)

</div>

<details>
  <summary><strong>How to inspect a version</strong></summary>

  1. Open the branch from the table above.
  2. Review that version's Java source and any associated configuration or data files.
  3. Run the selected robot in your Robocode environment.
  4. Compare results under the same battle conditions to keep the evaluation meaningful.

</details>

<details>
  <summary><strong>Repository conventions</strong></summary>

  - Keep each Madara generation in its own branch.
  - Use `StationaryTracker` as the original comparison baseline.
  - Keep experiments isolated until their results justify merging or promoting them.
  - Avoid committing generated `.class` files and temporary test robots.
  - Preserve V3's required configuration and learning data with its source.

</details>

---

<div align="center">

<img src="https://media1.tenor.com/m/NRXWuz3FwQEAAAAd/weee.gif" width="180" alt="Madara animated footer" />

**Built through iteration. Evaluated in battle.**

<sub>Robocode · IFSC ADS · 2026</sub>

</div>
