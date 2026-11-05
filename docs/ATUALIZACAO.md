# Atualização direta do Smart Hub

O app lê `https://github.com/Sedricko/Smart-Hub/releases/download/latest/smarthub.json`
(Configurações > Sobre > Verificar se há atualizações, e o aviso automático).

## Condições
1. **Repositório público** (privado devolve 404 e o app mostra "Não foi possível verificar atualizações").
2. **Versão maior que a instalada.** O app só oferece o que tem `versionCode` maior (`smarttubetv/build.gradle`). Atual: 2468.
3. **Sempre a mesma chave de assinatura.** Os segredos `SIGNING_KEY`, `KEY_STORE_PASSWORD`, `ALIAS`, `KEY_PASSWORD` não podem mudar.
   Se a chave mudar, o Android recusa a atualização e é preciso desinstalar.

## Como publicar
Actions > "Release Smart Hub" > Run workflow:
- `publish = false`: ensaio. Compila, assina e confere; deixa os APKs como artefato e não publica nada.
- `publish = true`: publica em "latest" (APKs `SmartHub_beta_<versão>_<abi>.apk` e `smarthub.json`) e apaga arquivos antigos do release.
- `changelog`: linha mostrada no diálogo de atualização do app.

O passo "Verify signatures and manifest" aborta se algum APK estiver assinado com a chave de teste (debug)
ou se o `smarthub.json` não tiver o mesmo `versionCode` do código.

## Primeira instalação (uma vez só)
Os apps instalados até agora são de teste (assinatura debug). Desinstale o antigo e instale o APK do release
(faça o backup pelo app antes). A partir dele, as atualizações entram por cima, com um toque em Instalar.

## A cada nova versão
Aumentar `versionCode` em `smarttubetv/build.gradle` antes de publicar.
