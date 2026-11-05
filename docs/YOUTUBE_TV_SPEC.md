# Referência: YouTube para TV (web UI do app 7.32.300, tela 1920x1080)

Fonte: o app de TV do YouTube é um navegador embutido (Cobalt/Chromium, `libchrobalt.so`); a interface é uma página web
servida pelo YouTube, não XML no APK. As medidas abaixo foram lidas da página real renderizada (modo convidado, 1920x1080).
Só estrutura e proporção. Cores, logo e ícones são do Smart Hub. A Shield roda 1080p a 320dpi (2x): dp = px / 2.

## Menu lateral ("guide")
- Recolhido: 156px (78dp) de largura, só ícones; item 84x78px (42x39dp), começa em x=33px (16,5dp). Avatar/"Fazer login" no topo.
- Expandido: 510px (255dp); item 471x78px (235,5x39dp); item selecionado = pílula clara com texto escuro; separador entre grupos.
- Grupos: [conta, busca, Início, Shorts, Inscrições, Biblioteca] | [Música, Ao vivo, Jogos, Notícias, Esportes] | Configurações embaixo.

## Topo
- Barra de busca em pílula: 720x72px (360x36dp) em x=156px, y=66px. Logo no canto direito (243x86px). Sem relógio.

## Prateleiras e cards
- Conteúdo começa em x=156px (78dp). Lista de seções começa em y=162px (81dp).
- Prateleira: 582px (291dp) de altura; título da prateleira 63px (31,5dp), texto ~30px (15sp).
- Card: 528x492px (264x246dp). Miniatura 16:9 de 528x297px (264x148,5dp); metadados 171px (85,5dp) abaixo, **sem painel de fundo**.
- Espaço entre cards: 36px (18dp). Título do vídeo ~36px (18sp), 2 linhas; canal e visualizações ~26px (13sp), cor secundária.
- Foco: anel branco de ~8px ao redor da miniatura com pequena folga; o card não aumenta.
- Selos "AO VIVO" vermelho no canto inferior direito da miniatura.

## Player (medido no YouTube TV web, 1920x1080; Shield = 320dpi, dp = px/2)

Referência: `youtube.com/tv#/watch?v=aircAruvnKk` (vídeo com capítulos), controles abertos.

| Elemento | x,y (px) | tamanho (px) | dp |
|---|---|---|---|
| Margem lateral | 96 | – | 48dp |
| Título (bold, ~40px) | 96,96 | 1036x102 | topo 48dp |
| Linha de metadados (canal • views • idade) | 96,165 | 1036x33 | 16sp |
| Rótulo "tempo • capítulo" (acima da barra) | 96,~678 | – | 15sp |
| Barra de progresso (segmentada por capítulos, marcador de capítulo dourado) | 96,639 | 1728x102 (barra ~8px) | – |
| Linha de ações | 96,741 | 1728x96 | 48dp |
| Avatar do canal | 96,753 | 72x72 | 36dp |
| Botões "Descrição", "Assinar" (pílulas escuras) | 180,753 | 195x72 | 36dp altura |
| Anterior / Play-Pause / Próximo (centro) | 822/912/1026 | 72 / 96 / 72 | play 48dp |
| Grupo like/dislike/comentários/salvar (pílula) | 1380,753 | 144 por botão | – |
| CC e Configurações (pílula) | 1680,753 | 72 por botão | – |
| Prateleira de recomendados (abaixo) | 0,879 | cards 552x504 (thumb 552x310) | 276dp |

Regras:
- Os capítulos NÃO têm linha própria: aparecem como segmentos na barra e como "0:03 • Nome do capítulo" acima dela.
- O título nunca é coberto por outra linha.
- Botões de ação são pílulas de fundo escuro translúcido com ícone branco; o foco vira pílula clara.
- Sem relógio e sem estatísticas técnicas (HD/FPS) por padrão.
