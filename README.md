<div align="center">

<img src="https://media1.tenor.com/m/LzC8ISbABmoAAAAd/%EA%B8%B0%EB%AA%A8%EC%B0%8C.gif" width="420" alt="Madara V3.1" />

# Madara V3.1

Robocode · IFSC ADS 2026

**Refining V3's decisions without starting from scratch.**  
*Refinando as decisões da V3 sem começar do zero.*

[English](#english) · [Português](#portugues) · [Main repository](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS)

</div>

---

<a id="english"></a>
## English

Madara V3.1 is a continuation of V3, not an unrelated rewrite. It keeps the robot's approach to radar, adaptive aiming, and danger-aware movement, while reorganizing parts of the movement evaluation to avoid repeated calculations.

During a match, it tracks opponents, estimates where shots may travel, and weighs potential routes around threats and walls. Its targeting uses GuessFactor statistics alongside a linear fallback. The goal of this version is to make those decisions less wasteful without losing the behavior developed in V3.

This is also the robot featured in the repository's `main` branch.

### Run in Robocode

Use `MadaraV3_1.java` and `MadaraV3_1.properties` in the `TriStateRobots` package. Compile the Java file with Robocode's Robot Editor and select `MadaraV3_1` for a battle. Saved learning data should remain separate from V3 under `MadaraV3_1.data/`.

**Source:** [MadaraV3_1.java](./MadaraV3_1.java)  
**Related files:** `MadaraV3_1.properties`

**Explore:** [Madara V3](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3) · [Repository home](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/main)

---

<a id="portugues"></a>
## Português

Madara V3.1 é uma continuação da V3, não um robô refeito do zero. Ela mantém a abordagem de radar, mira adaptativa e movimentação baseada em perigo, mas reorganiza partes da avaliação de movimento para evitar cálculos repetidos.

Durante a partida, acompanha adversários, estima trajetórias de tiros e avalia caminhos considerando ameaças e paredes. A mira utiliza estatísticas de GuessFactor e uma alternativa linear. O objetivo desta versão é tornar essas decisões menos custosas sem perder o comportamento desenvolvido na V3.

Esta também é a versão apresentada como principal na branch `main`.

### Como executar no Robocode

Use `MadaraV3_1.java` e `MadaraV3_1.properties` no pacote `TriStateRobots`. Compile o Java no editor do Robocode e escolha `MadaraV3_1` para a batalha. Os dados de aprendizado devem permanecer separados dos da V3, em `MadaraV3_1.data/`.

**Código-fonte:** [MadaraV3_1.java](./MadaraV3_1.java)  
**Arquivos relacionados:** `MadaraV3_1.properties`

**Outras versões:** [Madara V3](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3) · [Repository home](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/main)

---

<div align="center"><sub>ROBOCODE · IFSC ADS · 2026</sub></div>
