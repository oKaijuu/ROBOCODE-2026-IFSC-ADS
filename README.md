<div align="center">

<img src="https://media1.tenor.com/m/LzC8ISbABmoAAAAd/%EA%B8%B0%EB%AA%A8%EC%B0%8C.gif" width="540" alt="Madara V3.1 — Robocode" />

# Madara · Robocode

**IFSC · Análise e Desenvolvimento de Sistemas · 2026**

From StationaryTracker to Madara V3.1 — different strategies, one arena.

[English](#english) · [Português](#portugues) · [Current robot](./MadaraV3_1.java)

</div>

---

<a id="english"></a>
## English

### About the project

This project started with StationaryTracker and grew into a series of Java robots built for [Robocode](https://robocode.sourceforge.io/). Each Madara version explores a different approach to aiming, movement, and surviving fights against multiple opponents.

**Madara V3.1 is the current robot in `main`.** It builds on V3's tracking, GuessFactor targeting, and danger-aware movement, with changes intended to reduce repeated calculations during movement decisions. Older versions have their own branches, so their code remains available without getting mixed into the latest version.

### Robots and branches

| Branch | What's inside |
|---|---|
| [`main`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/main) | This README and the current `MadaraV3_1.java` robot, including its `.properties` file |
| [`stationary-tracker`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/stationary-tracker) | The original `StationaryTracker.java` robot |
| [`madara-v1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v1) | The first Madara implementation |
| [`madara-v2`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2) | The second Madara iteration |
| [`madara-v3`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3) | V3 source, configuration, and learning data |
| [`madara-v3.1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1) | V3.1 development files and its dedicated README |
| [`experiment/madara-v3-melee-movement`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/experiment/madara-v3-melee-movement) | Experimental movement work for multi-robot battles |

### Running Madara V3.1

The `main` branch includes [`MadaraV3_1.java`](./MadaraV3_1.java) and [`MadaraV3_1.properties`](./MadaraV3_1.properties). Place them together in the `TriStateRobots` package under your Robocode robot directory, then compile the Java file using Robocode's Robot Editor.

For comparisons between versions, use the same opponents, battlefield dimensions, and number of rounds. One win is not enough to tell whether a movement or targeting change helped.

### Quick setup on Linux

On Ubuntu/Debian-based environments with Git, curl, and Java already installed, you can use the repository's setup script:

```bash
curl -fsSL https://raw.githubusercontent.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/main/scripts/setup-robocode-linux.sh -o /tmp/setup-robocode-linux.sh
bash /tmp/setup-robocode-linux.sh
```

It checks out the project, retrieves the robot branches, and copies the source files into `~/robocode/robots/TriStateRobots`. If Robocode is missing, its official graphical installer is opened. The script does not run `sudo`, `apt`, or other system package managers.

Robocode is launched through its **original script**, not a wrapper inside `~/.local/bin`. Before you open it, the installer adjusts `robocode.sh` from `. ./set_java_options.sh` to `./set_java_options.sh` when that exact line is found.

```bash
~/robocode/robocode.sh
```

A desktop-menu shortcut is also created to point to the original launcher. If your installation is elsewhere, use that directory instead of `~/robocode`.

**Learning data:** V3 and V3.1 keep separate data folders under `TriStateRobots`. The installer can seed V3.1 from the V3 learning file when one exists, but does not overwrite an existing V3.1 file. If the lab resets your home directory between sessions, back up anything you want to keep.

---

<a id="portugues"></a>
## Português

### Sobre o projeto

O projeto começou com o StationaryTracker e evoluiu para uma série de robôs em Java feitos para o [Robocode](https://robocode.sourceforge.io/). Cada versão do Madara experimenta formas diferentes de mirar, se movimentar e sobreviver em batalhas com vários adversários.

**Madara V3.1 é o robô atual da branch `main`.** Ele aproveita o rastreamento, a mira com GuessFactor e a movimentação baseada em perigo da V3, com mudanças voltadas a reduzir cálculos repetidos nas decisões de movimento. As versões anteriores continuam em branches próprias, sem misturar seus códigos com a versão atual.

### Robôs e branches

| Branch | O que você encontra |
|---|---|
| [`main`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/main) | Este README, o `MadaraV3_1.java` atual e seu arquivo `.properties` |
| [`stationary-tracker`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/stationary-tracker) | O `StationaryTracker.java` original |
| [`madara-v1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v1) | A primeira versão do Madara |
| [`madara-v2`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2) | A segunda versão do Madara |
| [`madara-v3`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3) | Código, configuração e dados de aprendizado da V3 |
| [`madara-v3.1`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1) | Arquivos de desenvolvimento da V3.1 e seu README |
| [`experiment/madara-v3-melee-movement`](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/experiment/madara-v3-melee-movement) | Experimentos de movimentação em batalhas com muitos robôs |

### Como executar o Madara V3.1

A branch `main` contém [`MadaraV3_1.java`](./MadaraV3_1.java) e [`MadaraV3_1.properties`](./MadaraV3_1.properties). Coloque os dois arquivos no pacote `TriStateRobots`, dentro da pasta de robôs do Robocode, e compile o Java usando o editor do programa.

Para comparar versões, mantenha os mesmos adversários, o mesmo tamanho de arena e a mesma quantidade de rounds. Uma vitória isolada não prova que uma mudança de mira ou movimentação foi melhor.

### Instalação rápida no Linux

Em ambientes Ubuntu/Debian que já tenham Git, curl e Java instalados, utilize o instalador do projeto:

```bash
curl -fsSL https://raw.githubusercontent.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/main/scripts/setup-robocode-linux.sh -o /tmp/setup-robocode-linux.sh
bash /tmp/setup-robocode-linux.sh
```

Ele atualiza o repositório, busca as branches dos robôs e copia os códigos para `~/robocode/robots/TriStateRobots`. Se o Robocode não estiver instalado, o instalador gráfico oficial será aberto. O script não executa `sudo`, `apt` nem gerenciadores de pacotes do sistema.

O Robocode é iniciado pelo **arquivo original**, sem criar um iniciador em `~/.local/bin`. Antes da abertura, o instalador troca a linha `. ./set_java_options.sh` por `./set_java_options.sh` em `robocode.sh`, quando encontra essa linha exata.

```bash
~/robocode/robocode.sh
```

Um atalho para o menu de aplicativos também aponta para o arquivo original. Se você instalou o Robocode em outro diretório, use o caminho correspondente.

**Dados de aprendizado:** V3 e V3.1 usam pastas separadas dentro de `TriStateRobots`. Quando há dados salvos da V3, o instalador pode usá-los como ponto de partida da V3.1, sem sobrescrever os dados que ela já tenha. Se o laboratório apagar sua pasta pessoal após o uso, faça backup do que quiser preservar.

---

<div align="center">

**Madara · Robocode · IFSC 2026**

[English](#english) · [Português](#portugues)

</div>
