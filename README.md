<div align="center">

<img src="https://media1.tenor.com/m/EpgxcRD1ydsAAAAd/madara-uchiha.gif" width="100%" alt="Madara Uchiha animated banner" />

# ROBOCODE · IFSC ADS 2026

### An evolving Robocode AI project

**One baseline. Three generations. Continuous experimentation.**

[![Java](https://img.shields.io/badge/Language-Java-cc3333?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Robocode](https://img.shields.io/badge/Platform-Robocode-6f42c1?style=for-the-badge)](https://robocode.sourceforge.io/)
[![Versions](https://img.shields.io/badge/Madara-V1%20%7C%20V2%20%7C%20V3-bb8f35?style=for-the-badge)](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/branches)

**Language / Idioma:** [English](#english-version) · [Português](#versao-em-portugues)

[**Robot lineup**](#-robot-lineup) · [**Branch map**](#-branch-map) · [**Experiments**](#-experiments)

</div>

---

<a id="english-version"></a>

## English version

### Project overview

This repository documents the development of Robocode combat robots, starting with **StationaryTracker** as the original baseline and continuing through the **Madara V1, V2, and V3** iterations.

Each version has its own branch so its source can be inspected and compared without mixing implementations. The `main` branch serves as the project landing page and preserves the original baseline source.

## ⚔ Robot lineup

<table>
  <tr>
    <td width="25%" align="center">
      <img src="https://media1.tenor.com/m/EIUK28UjRqUAAAAC/china-artillery.gif" width="220" height="124" alt="StationaryTracker animation" />
      <h3>StationaryTracker</h3>
      <strong>Original baseline</strong>
      <p>Starting point for performance comparisons and the project's first robot.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/stationary-tracker">Open branch ↗</a>
    </td>
    <td width="25%" align="center">
      <img src="https://media1.tenor.com/m/xwKNvNW00NEAAAAC/madara.gif" width="220" height="124" alt="Madara V1 animation" />
      <h3>Madara V1</h3>
      <strong>First generation</strong>
      <p>The first Madara implementation, kept separate for direct comparison.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v1">Open branch ↗</a>
    </td>
    <td width="25%" align="center">
      <img src="https://media1.tenor.com/m/xaurczvDhkwAAAAd/naruto-uchiha-madara.gif" width="220" height="124" alt="Madara V2 animation" />
      <h3>Madara V2</h3>
      <strong>Second generation</strong>
      <p>A separate iteration for tracking changes from the V1 design.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2">Open branch ↗</a>
    </td>
    <td width="25%" align="center">
      <img src="https://media1.tenor.com/m/EpgxcRD1ydsAAAAd/madara-uchiha.gif" width="220" height="124" alt="Madara V3 animation" />
      <h3>Madara V3</h3>
      <strong>Learning & experimentation</strong>
      <p>Includes its configuration and persisted learning data.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3">Open branch ↗</a>
    </td>
  </tr>
</table>

## 🗂 Branch map

| Branch | Role | Contents |
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

<a id="versao-em-portugues"></a>

## Versão em português

### Visão geral

Este repositório documenta o desenvolvimento de robôs de combate para Robocode, começando pelo **StationaryTracker** como referência original e avançando pelas versões **Madara V1, V2 e V3**.

Cada versão possui sua própria branch para permitir inspeção e comparação do código sem misturar implementações. A branch `main` funciona como página inicial do projeto e mantém o código-fonte da referência original.

## ⚔ Linha de robôs

<table>
  <tr>
    <td width="25%" align="center">
      <img src="https://media1.tenor.com/m/EIUK28UjRqUAAAAC/china-artillery.gif" width="220" height="124" alt="Animação do StationaryTracker" />
      <h3>StationaryTracker</h3>
      <strong>Referência original</strong>
      <p>Ponto de partida para comparações de desempenho e primeiro robô do projeto.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/stationary-tracker">Abrir branch ↗</a>
    </td>
    <td width="25%" align="center">
      <img src="https://media1.tenor.com/m/xwKNvNW00NEAAAAC/madara.gif" width="220" height="124" alt="Animação do Madara V1" />
      <h3>Madara V1</h3>
      <strong>Primeira geração</strong>
      <p>Primeira implementação do Madara, mantida separada para comparação direta.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v1">Abrir branch ↗</a>
    </td>
    <td width="25%" align="center">
      <img src="https://media1.tenor.com/m/xaurczvDhkwAAAAd/naruto-uchiha-madara.gif" width="220" height="124" alt="Animação do Madara V2" />
      <h3>Madara V2</h3>
      <strong>Segunda geração</strong>
      <p>Iteração separada para acompanhar as mudanças em relação ao projeto V1.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2">Abrir branch ↗</a>
    </td>
    <td width="25%" align="center">
      <img src="https://media1.tenor.com/m/EpgxcRD1ydsAAAAd/madara-uchiha.gif" width="220" height="124" alt="Animação do Madara V3" />
      <h3>Madara V3</h3>
      <strong>Aprendizado e experimentação</strong>
      <p>Inclui sua configuração e os dados de aprendizado persistidos.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3">Abrir branch ↗</a>
    </td>
  </tr>
</table>

## 🗂 Mapa de branches

| Branch | Função | Conteúdo |
|---|---|---|
| [`main`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/main) | Índice do projeto | Este README e o código original `StationaryTracker.java` |
| [`stationary-tracker`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/stationary-tracker) | Referência | Código-fonte original do robô |
| [`madara-v1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v1) | Versão 1 | `MadaraV1.java` e código de referência |
| [`madara-v2`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2) | Versão 2 | `MadaraV2.java` e código de referência |
| [`madara-v3`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3) | Versão 3 | `MadaraV3.java`, propriedades, dados de aprendizado e código de referência |
| [`experiment/madara-v3-melee-movement`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/experiment/madara-v3-melee-movement) | Experimental | Trabalho de movimentação corpo a corpo do V3 e documentação do experimento |

## 🧪 Experimentos

Mudanças experimentais devem permanecer isoladas das branches de versão até estarem prontas para avaliação. O trabalho atual de movimentação corpo a corpo do V3 possui uma branch própria:

<div align="center">

[![Abrir branch experimental](https://img.shields.io/badge/EXPERIMENTO-Madara%20V3%20Melee%20Movement-6f42c1?style=for-the-badge&logo=github)](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/experiment/madara-v3-melee-movement)

</div>

<details>
  <summary><strong>Como inspecionar uma versão</strong></summary>

  1. Abra a branch desejada na tabela acima.
  2. Examine o código Java da versão e os arquivos de configuração ou dados associados.
  3. Execute o robô escolhido no seu ambiente Robocode.
  4. Compare os resultados sob as mesmas condições de batalha para manter a avaliação consistente.

</details>

<details>
  <summary><strong>Convenções do repositório</strong></summary>

  - Mantenha cada geração do Madara em sua própria branch.
  - Use o `StationaryTracker` como referência original de comparação.
  - Mantenha experimentos isolados até que os resultados justifiquem sua integração ou promoção.
  - Evite enviar arquivos compilados `.class` e robôs de teste temporários.
  - Preserve os arquivos de configuração e os dados de aprendizado necessários ao V3 junto ao código-fonte.

</details>

---

<div align="center">

<img src="https://media1.tenor.com/m/kU3OB3wwHx8AAAAC/madara-naruto.gif" width="180" alt="Madara animated footer" />

**Built through iteration. Evaluated in battle.**  
**Construído por iteração. Avaliado em batalha.**

<sub>Robocode · IFSC ADS · 2026</sub>

[Back to English](#english-version) · [Voltar ao português](#versao-em-portugues)

</div>
