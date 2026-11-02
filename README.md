# Smart Hub

Aplicativo de vídeo para Android TV.

## Instalação

Baixe o APK da [última release](https://github.com/Sedricko/Smart-Hub/releases/tag/latest) e instale com `adb install -r`.
Para o Shield e outros aparelhos ARM de 64 bits, use `SmartHub_beta_<versão>_arm64-v8a.apk`.

## Publicar uma versão

1. Aumente `versionCode` em `smarttubetv/build.gradle`.
2. Em *Actions*, rode **Release Smart Hub** (exige os secrets `SIGNING_KEY`, `KEY_STORE_PASSWORD`, `ALIAS` e `KEY_PASSWORD`).

## Identidade

Veja [BRAND.md](BRAND.md).

## Licença e créditos

Este projeto é baseado no SmartTube, de yuliskov, licenciado sob MIT. O aviso de copyright original foi mantido em [LICENSE](LICENSE), como a licença exige.
