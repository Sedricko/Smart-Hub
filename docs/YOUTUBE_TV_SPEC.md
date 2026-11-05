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
