# Referência: YouTube mobile 21.44 (medidas lidas dos recursos do APK)

Só estrutura e proporção. Cores, logo e ícones são do Smart Hub.

## Navegação
- Barra inferior ("pivot bar"): altura 48dp, aba mínima 48dp, ícone 24dp, rótulo 10dp, abas de peso igual.
- Chips de categoria: altura 32dp, raio 16dp (pílula), padding horizontal 12dp, espaço 8dp; chip ativo invertido.
- Abas da Home: texto 14sp, altura da barra 48dp, divisor inferior de 1dp.

## Lista / cards
- Margem lateral do conteúdo: 16dp. Avatar do canal: 40dp. Miniatura 16:9 em largura total, raio 12dp.
- Título até 2 linhas; linha secundária (canal · visualizações · data) em cor secundária.

## Player
- Botões de ação: 48dp com padding 12dp. Barra inferior de controles: 42dp. Scrubber: 42dp de altura.
- Margem lateral dos controles: 16dp. Degradê superior: 180dp. Padding superior (insets): 16dp.
- Tela cheia na horizontal; Shorts em retrato.

## Decisões de implementação
- Fonte: layouts do APK são quase todos Litho (dirigidos por dados); só a casca (barra, chips, player) é XML.
- Feito: barra inferior, chips, margem de 16dp, cards 16:9, player em paisagem, busca ajustada.
- Pendente: cards com avatar, menus em folha inferior, gestos do player (toque duplo, deslizar), Shorts em tela cheia.
