<div align="center">

<img src="https://media1.tenor.com/m/LzC8ISbABmoAAAAd/%EA%B8%B0%EB%AA%A8%EC%B0%8C.gif" width="100%" alt="Madara V3.1 animated banner" />

# ROBOCODE · IFSC ADS 2026

### An evolving Robocode AI project

**One baseline. Four Madara versions. Continuous improvement.**

[![Java](https://img.shields.io/badge/Language-Java-cc3333?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Robocode](https://img.shields.io/badge/Platform-Robocode-6f42c1?style=for-the-badge)](https://robocode.sourceforge.io/)
[![Versions](https://img.shields.io/badge/Madara-V1%20%7C%20V2%20%7C%20V3%20%7C%20V3.1-bb8f35?style=for-the-badge)](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/branches)

**Language / Idioma:** [English](#english-version) · [Português](#versao-em-portugues)

[**Download Madara V3.1 (.jar)**](./TriStateRobots.MadaraV3_1_3.1.jar) · [**Robot lineup**](#-robot-lineup) · [**Branch map**](#-branch-map)

</div>

---

<a id="english-version"></a>

## English version

### Project overview

This project began with **StationaryTracker** and grew into the Madara series: **V1, V2, V3, and V3.1**. Each version explores how to improve aiming, movement, and survival in Robocode, while V3.1 refines V3's movement evaluation.

Each version has its own branch for comparison. The `main` branch provides the **ready-to-use Madara V3.1 JAR**. The Java source and configuration remain available in the [`madara-v3.1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1) development branch, while the original StationaryTracker has its own branch.

### Download and install Madara V3.1

**Official ready-to-use build:** [**`TriStateRobots.MadaraV3_1_3.1.jar` — Download JAR**](./TriStateRobots.MadaraV3_1_3.1.jar)

This is the packaged version of Madara V3.1. It can be added to Robocode **without compiling the Java source**:

1. Download the JAR from the link above.
2. Copy it to your Robocode `robots` directory (for example, `D:\robocode\robots` on Windows or `~/robocode/robots` on Linux).
3. Open or restart Robocode, refresh its robot list if needed, and select `TriStateRobots.MadaraV3_1` for a new battle.

For the editable source and robot configuration, see the [`madara-v3.1` development branch](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1).

## ⚔ Robot lineup

<table>
  <tr>
    <td width="50%" align="center">
      <img src="https://media1.tenor.com/m/EIUK28UjRqUAAAAC/china-artillery.gif" width="220" height="124" alt="StationaryTracker animation" />
      <h3>StationaryTracker</h3>
      <strong>Original baseline</strong>
      <p>Starting point for performance comparisons and the project's first robot.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/stationary-tracker">Open branch ↗</a>
    </td>
    <td width="50%" align="center">
      <img src="https://media1.tenor.com/m/xwKNvNW00NEAAAAC/madara.gif" width="220" height="124" alt="Madara V1 animation" />
      <h3>Madara V1</h3>
      <strong>First generation</strong>
      <p>The first Madara implementation, kept separate for direct comparison.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v1">Open branch ↗</a>
    </td>
  </tr>
</table>

<table>
  <tr>
    <td width="50%" align="center">
      <img src="https://media1.tenor.com/m/xaurczvDhkwAAAAd/naruto-uchiha-madara.gif" width="220" height="124" alt="Madara V2 animation" />
      <h3>Madara V2</h3>
      <strong>Second generation</strong>
      <p>A separate iteration for tracking changes from the V1 design.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2">Open branch ↗</a>
    </td>
    <td width="50%" align="center">
      <img src="https://media1.tenor.com/m/EpgxcRD1ydsAAAAd/madara-uchiha.gif" width="220" height="124" alt="Madara V3 animation" />
      <h3>Madara V3</h3>
      <strong>Learning & experimentation</strong>
      <p>Includes its configuration and persisted learning data.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3">Open branch ↗</a>
    </td>
  </tr>
</table>

<table>
  <tr>
    <td width="100%" align="center">
      <img src="https://media1.tenor.com/m/LzC8ISbABmoAAAAd/%EA%B8%B0%EB%AA%A8%EC%B0%8C.gif" width="220" height="124" alt="Madara V3.1 animation" />
      <h3>Madara V3.1</h3>
      <strong>Structural optimization</strong>
      <p>Refactors V3's movement evaluation to reduce redundant calculations while preserving combat scoring and learning-data compatibility.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1">Open branch ↗</a>
    </td>
    
  </tr>
</table>

## 🗂 Branch map

| Branch | Role | Contents |
|---|---|---|
| [`main`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/main) | Ready-to-use release | [`TriStateRobots.MadaraV3_1_3.1.jar`](./TriStateRobots.MadaraV3_1_3.1.jar) and this README |
| [`stationary-tracker`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/stationary-tracker) | Baseline | Original robot source |
| [`madara-v1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v1) | Version 1 | `MadaraV1.java` and first-generation code |
| [`madara-v2`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2) | Version 2 | `MadaraV2.java` and second-generation code |
| [`madara-v3`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3) | Version 3 | `MadaraV3.java`, properties, and learning data |
| [`madara-v3.1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1) | Version 3.1 | Development branch for `MadaraV3_1.java` and its configuration |
| [`experiment/madara-v3-melee-movement`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/experiment/madara-v3-melee-movement) | Experiment | Movement experiments for multi-robot battles |

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
  - Avoid committing generated `.class` files and temporary test robots.
  - Preserve each version's required configuration and learning data with its source; keep V3 and V3.1 learning files separate.

</details>

---

<a id="versao-em-portugues"></a>

## Versão em português

### Visão geral

O projeto começou com o **StationaryTracker** e evoluiu para a série Madara: **V1, V2, V3 e V3.1**. Cada versão experimenta maneiras de melhorar a mira, a movimentação e a sobrevivência no Robocode. A V3.1 refina a avaliação de movimento da V3.

Cada versão tem sua própria branch para facilitar as comparações. A `main` disponibiliza o **JAR pronto para uso do Madara V3.1**. O código Java e o arquivo de configuração continuam na [branch `madara-v3.1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1), enquanto o StationaryTracker original permanece em sua própria branch.

### Baixar e instalar o Madara V3.1

**Versão compilada pronta para uso:** [**`TriStateRobots.MadaraV3_1_3.1.jar` — Baixar JAR**](./TriStateRobots.MadaraV3_1_3.1.jar)

Este é o pacote do Madara V3.1 para usar no Robocode **sem precisar compilar o código Java**:

1. Baixe o JAR pelo link acima.
2. Coloque o arquivo na pasta `robots` do Robocode (por exemplo, `D:\robocode\robots` no Windows ou `~/robocode/robots` no Linux).
3. Abra ou reinicie o Robocode, atualize a lista de robôs se necessário e selecione `TriStateRobots.MadaraV3_1` em uma nova batalha.

O código-fonte editável e a configuração ficam na [branch de desenvolvimento `madara-v3.1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1).

## ⚔ Linha de robôs

<table>
  <tr>
    <td width="50%" align="center">
      <img src="https://media1.tenor.com/m/EIUK28UjRqUAAAAC/china-artillery.gif" width="220" height="124" alt="Animação do StationaryTracker" />
      <h3>StationaryTracker</h3>
      <strong>Referência original</strong>
      <p>Ponto de partida para comparações de desempenho e primeiro robô do projeto.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/stationary-tracker">Abrir branch ↗</a>
    </td>
    <td width="50%" align="center">
      <img src="https://media1.tenor.com/m/xwKNvNW00NEAAAAC/madara.gif" width="220" height="124" alt="Animação do Madara V1" />
      <h3>Madara V1</h3>
      <strong>Primeira geração</strong>
      <p>Primeira implementação do Madara, mantida separada para comparação direta.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v1">Abrir branch ↗</a>
    </td>
  </tr>
</table>

<table>
  <tr>
    <td width="50%" align="center">
      <img src="https://media1.tenor.com/m/xaurczvDhkwAAAAd/naruto-uchiha-madara.gif" width="220" height="124" alt="Animação do Madara V2" />
      <h3>Madara V2</h3>
      <strong>Segunda geração</strong>
      <p>Iteração separada para acompanhar as mudanças em relação ao projeto V1.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2">Abrir branch ↗</a>
    </td>
    <td width="50%" align="center">
      <img src="https://media1.tenor.com/m/EpgxcRD1ydsAAAAd/madara-uchiha.gif" width="220" height="124" alt="Animação do Madara V3" />
      <h3>Madara V3</h3>
      <strong>Aprendizado e experimentação</strong>
      <p>Inclui sua configuração e os dados de aprendizado persistidos.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3">Abrir branch ↗</a>
    </td>
  </tr>
</table>

<table>
  <tr>
    <td width="100%" align="center">
      <img src="https://media1.tenor.com/m/LzC8ISbABmoAAAAd/%EA%B8%B0%EB%AA%A8%EC%B0%8C.gif" width="220" height="124" alt="Animação do Madara V3.1" />
      <h3>Madara V3.1</h3>
      <strong>Otimização estrutural</strong>
      <p>Refatora a avaliação de movimento do V3 para reduzir cálculos redundantes, preservando a pontuação de combate e a compatibilidade dos dados de aprendizado.</p>
      <a href="https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1">Abrir branch ↗</a>
    </td>
    
  </tr>
</table>

## 🗂 Mapa de branches

| Branch | Função | Conteúdo |
|---|---|---|
| [`main`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/main) | Versão pronta para uso | [`TriStateRobots.MadaraV3_1_3.1.jar`](./TriStateRobots.MadaraV3_1_3.1.jar) e este README |
| [`stationary-tracker`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/stationary-tracker) | Referência | Código-fonte original do robô |
| [`madara-v1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v1) | Versão 1 | `MadaraV1.java` e código da primeira geração |
| [`madara-v2`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2) | Versão 2 | `MadaraV2.java` e código da segunda geração |
| [`madara-v3`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3) | Versão 3 | `MadaraV3.java`, propriedades e dados de aprendizado |
| [`madara-v3.1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1) | Versão 3.1 | Branch de desenvolvimento do `MadaraV3_1.java` e sua configuração |
| [`experiment/madara-v3-melee-movement`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/experiment/madara-v3-melee-movement) | Experimentos | Testes de movimentação em batalhas com vários robôs |

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
  - Evite enviar arquivos compilados `.class` e robôs de teste temporários.
  - Preserve os arquivos de configuração e aprendizado de cada versão junto ao código-fonte; mantenha separados os dados de aprendizado do V3 e do V3.1.

</details>

---

<div align="center">

<img src="https://media1.tenor.com/m/kU3OB3wwHx8AAAAC/madara-naruto.gif" width="180" alt="Madara animated footer" />

**Built through iteration. Evaluated in battle.**  
**Construído por iteração. Avaliado em batalha.**

<sub>Robocode · IFSC ADS · 2026</sub>

[Back to English](#english-version) · [Voltar ao português](#versao-em-portugues)

</div>

---

## 🐧 Automatic Robocode setup on Linux

Use the quick installer to restore the repository and install the project robots. It uses Robocode's original launcher at `~/robocode/robocode.sh` instead of creating a wrapper in `~/.local/bin`. It is designed for Ubuntu/Debian-based lab environments and does not require administrator privileges.

### Quick install

Run in a terminal:

```bash
curl -fsSL https://raw.githubusercontent.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/main/scripts/setup-robocode-linux.sh -o /tmp/setup-robocode-linux.sh
bash /tmp/setup-robocode-linux.sh
```

The script uses the existing Git, curl, and Java installations. It never runs `apt`, `apt-get`, `sudo`, or a system package manager. If a required command is missing, it stops and explains what needs to be provided by the lab administrator. It installs Robocode under `~/robocode` by default and automatically patches `~/robocode/robocode.sh`, changing `. ./set_java_options.sh` to `./set_java_options.sh`. The installer makes the original `robocode.sh` executable and points the desktop-menu shortcut directly to it; it does not create `~/.local/bin/robocode` or `~/.local/bin/robocode-project`.

It fetches the project branches and installs **StationaryTracker, Madara V1, V2, V3, and V3.1** source files for development. For the packaged V3.1 version intended for direct use without compilation, download [`TriStateRobots.MadaraV3_1_3.1.jar`](./TriStateRobots.MadaraV3_1_3.1.jar) above.

### V3.1 learning-data compatibility

Madara V3.1 is based on V3 and uses the same learning-data filename and data format. During installation, the V3 learning file is copied into V3.1's own data directory only if the V3.1 file does not already exist. The two robots then keep their progress separate:

- V3: `~/robocode/robots/TriStateRobots/MadaraV3.data/madara-v3-learning.dat`
- V3.1: `~/robocode/robots/TriStateRobots/MadaraV3_1.data/madara-v3-learning.dat`

Existing learning files are not overwritten. This lets V3.1 start from the available V3 learning state without making both versions write to the same file. If the V3 learning file is not available in the repository, the installer skips seeding it.

### Launch Robocode

The installer uses the original Robocode launcher and adds a **Robocode** application-menu shortcut that points directly to it. Launch Robocode with:

```bash
~/robocode/robocode.sh
```

If you installed Robocode in a different directory, replace `~/robocode` with that installation path. The launcher is patched before it is opened, and no wrapper launcher is created under `~/.local/bin`.

Choose `~/robocode` as the installation directory in the graphical installer if Robocode is not already installed. In Robocode, make sure the robot root directory is set to:

```
~/robocode/robots
```

If the robots do not appear, add that directory in Robocode's robot path settings and restart the application.

**Important:** if the lab environment deletes your entire home directory at shutdown, the installer and locally saved learning data will also be deleted. Keep the quick-install command accessible and rerun it after a reset.

