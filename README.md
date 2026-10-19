<div align="center">

<img src="https://media1.tenor.com/m/LqxYy-Xu_z0AAAAd/madara-uchiha-madara-ten-tails-jinchuriki.gif" width="320" alt="Madara V3.1" />

# Madara V3.1

### Robocode · IFSC ADS 2026

**Adaptive combat, intelligent movement, and learned targeting**

[![Java](https://img.shields.io/badge/Language-Java-cc3333?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Version](https://img.shields.io/badge/Madara-V3.1-6f42c1?style=for-the-badge)](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1)

[Repository home](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS) · [Source file](./MadaraV3_1.java) · [Configuration](./MadaraV3_1.properties) · [Previous version: V3](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3)

</div>

---

## Overview

Madara V3.1 is a Java robot for Robocode, developed as an evolution of Madara V3. It combines enemy tracking, adaptive targeting, and danger-aware movement to survive and fight in battles with multiple opponents.

## Features

- **Enemy tracking:** follows scanned opponents and periodically searches for enemies that have not been detected recently.
- **Adaptive targeting:** uses GuessFactor statistics learned from enemy movement, with a linear targeting fallback.
- **Danger-aware movement:** evaluates movement directions while accounting for walls, nearby enemies, collisions, and incoming bullets.
- **Bullet dodging:** tracks enemy shots and estimates their trajectories to choose safer movement.
- **Adaptive firing:** adjusts bullet power based on combat conditions.
- **Persistent learning:** stores targeting data so learned statistics can be reused across rounds.

## Files

- `MadaraV3_1.java` — main robot implementation.
- `MadaraV3_1.properties` — Robocode robot metadata and configuration.
- `MadaraV3.java` and `MadaraV3.properties` — V3 files retained as a reference baseline.
- `MadaraV3.data/madara-v3-learning.dat` — V3 learning data, when included in the branch.

## Run it

1. Place the source and properties files in your Robocode robot source directory.
2. Compile `MadaraV3_1.java` in Robocode.
3. Select **MadaraV3_1** in the robot list and start a battle.
4. Run multiple battles to observe movement, targeting, and survival against different opponents.

## Version lineage

- [StationaryTracker — original baseline](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/stationary-tracker)
- [Madara V1 — first generation](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v1)
- [Madara V2 — second generation](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2)
- [Madara V3 — learning and persistence](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3)
- **Madara V3.1 — adaptive combat and movement**

<div align="center"><sub>ROBOCODE · IFSC ADS · 2026</sub></div>
