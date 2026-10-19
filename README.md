<div align="center">

<img src="https://media1.tenor.com/m/LqxYy-Xu_z0AAAAd/madara-uchiha-madara-ten-tails-jinchuriki.gif" width="320" alt="Madara V3.1" />

# Madara V3.1

### Robocode · IFSC ADS 2026

**Optimization and refactoring — behavior and learning compatibility first**

[![Java](https://img.shields.io/badge/Language-Java-cc3333?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Version](https://img.shields.io/badge/Madara-V3.1-6f42c1?style=for-the-badge)](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1)

[Repository home](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS) · [V3.1 source](./MadaraV3_1.java) · [V3 baseline](./MadaraV3.java)

</div>

---

## Objective

Madara V3.1 is a dedicated optimization and refactoring branch based on Madara V3. The initial goal is to improve code organization, reduce redundant work, and make the implementation easier to maintain without intentionally changing combat behavior.

## Scope

- Factor repeated logic into clear, reusable methods.
- Identify avoidable calculations, allocations, and repeated iterations.
- Preserve current combat systems and behavior while establishing a baseline.
- Keep the V3 learning-data format compatible.

## First optimization pass

The first structural pass optimizes the movement danger scan while preserving the existing scoring rules:

- Reuses each candidate direction's sine and cosine values across evaluation and dodge diagnostics.
- Calculates projectile danger once per movement candidate and reuses that exact score for both movement selection and diagnostics.
- Precomputes each active enemy bullet's projected coordinates once per tick, then reuses those coordinates across all movement candidates.
- Keeps the 24 candidate directions, danger weights, threat handling, aiming/firing logic, and learning serialization format unchanged.

This is a code-level optimization, not a measured benchmark. Compile V3.1 and compare it with V3 over identical battle settings before judging runtime or combat performance.

## Learning-data compatibility

V3.1 continues to use the existing learning file name: `madara-v3-learning.dat`.

The file format and GuessFactor statistics should remain compatible with Madara V3. The V3 source and its original metadata are retained for comparison. For isolated experiments, do not run both versions against the same live data file simultaneously. Back up the learning file before testing.

## Files

- `MadaraV3_1.java` — V3.1 working implementation, initially copied from V3 with only the Java class name changed.
- `MadaraV3_1.properties` — Robocode metadata for V3.1.
- `MadaraV3.java` and `MadaraV3.properties` — original V3 baseline retained for comparison.
- `MadaraV3.data/madara-v3-learning.dat` — existing persisted learning data, retained from V3 when present.

## Validation plan

1. Compile V3.1 before making optimization changes.
2. Run it against the same opponents and battle settings used for V3.
3. Compare win rate, damage dealt/received, hit rate, wall collisions, dodge activity, and execution stability.
4. Apply refactoring in small steps and compare results after each step.
5. Do not change learning-data serialization or combat logic as part of a purely structural refactor.

## Version lineage

- [Madara V3 — baseline](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3)
- **Madara V3.1 — optimization and refactoring**

<div align="center"><sub>ROBOCODE · IFSC ADS · 2026</sub></div>
