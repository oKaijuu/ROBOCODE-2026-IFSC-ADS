<div align="center">

<img src="https://media1.tenor.com/m/EpgxcRD1ydsAAAAd/madara-uchiha.gif" width="420" alt="Madara V3" />

# Madara V3

Robocode · IFSC ADS 2026

**The version that introduced persistent learning into the series.**  
*A versão que trouxe aprendizado persistente para a série.*

[English](#english) · [Português](#portugues) · [Main repository](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS)

</div>

---

<a id="english"></a>
## English

Madara V3 takes the project further with enemy tracking, shot prediction, danger-aware movement, and targeting statistics. The aim is to use information collected during battles to make better decisions, instead of treating every encounter as completely new.

The learning file matters here. The robot can reuse stored data, so losing that file means losing whatever state was saved. The V3.1 branch continues this work with changes to how movement is evaluated.

### Run in Robocode

Keep `MadaraV3.java` and `MadaraV3.properties` together in the `TriStateRobots` robot package and compile in Robocode. If you use saved learning data, keep it under `MadaraV3.data/madara-v3-learning.dat` and back it up before resetting the environment.

**Source:** [MadaraV3.java](./MadaraV3.java)  
**Related files:** `MadaraV3.properties` · `MadaraV3.data/madara-v3-learning.dat`

**Explore:** [Madara V2](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2) · [Madara V3.1](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1)

---

<a id="portugues"></a>
## Português

Madara V3 amplia o projeto com rastreamento de inimigos, previsão de tiros, movimentação baseada em perigo e estatísticas de mira. A proposta é aproveitar informações das batalhas para tomar decisões melhores, em vez de tratar cada encontro como se fosse o primeiro.

O arquivo de aprendizado faz diferença nessa versão. O robô pode reutilizar dados gravados; apagá-los significa perder o estado salvo. A branch V3.1 continua esse trabalho com mudanças na avaliação de movimento.

### Como executar no Robocode

Mantenha `MadaraV3.java` e `MadaraV3.properties` juntos no pacote `TriStateRobots` e compile no Robocode. Se utilizar o aprendizado salvo, mantenha o arquivo em `MadaraV3.data/madara-v3-learning.dat` e faça backup antes de redefinir o ambiente.

**Código-fonte:** [MadaraV3.java](./MadaraV3.java)  
**Arquivos relacionados:** `MadaraV3.properties` · `MadaraV3.data/madara-v3-learning.dat`

**Outras versões:** [Madara V2](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v2) · [Madara V3.1](https://github.com/oKaijuu/ROBOCODE-2026-IFSC-ADS/tree/madara-v3.1)

---

<div align="center"><sub>ROBOCODE · IFSC ADS · 2026</sub></div>
