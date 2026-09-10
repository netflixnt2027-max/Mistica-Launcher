# Mística Launcher

Launcher Android do servidor Brasil Terra Mística (`190.102.40.7:7821`).

## Recursos

- Consulta UDP SA-MP para mostrar online/offline e jogadores.
- Botão Jogar agora via `samp://190.102.40.7:7821`.
- Conteúdo remoto para GMX, evento e atualizações.
- Painel PHP simples, compatível com hospedagem InfiniteFree.

## Configurar o painel

1. Abra `painel/config.php` e troque `troque-esta-senha`.
2. Envie toda a pasta `painel` para sua hospedagem.
3. Teste `https://SEU-DOMINIO/painel/conteudo.php` no navegador.
4. Em `app/build.gradle`, preencha `CONTENT_URL` com essa URL.

## Gerar o APK no Android Studio

1. Abra a pasta `MisticaLauncher` no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Use **Build > Build APK(s)**.
4. O APK de teste ficará em `app/build/outputs/apk/debug/app-debug.apk`.

Para publicação, use **Build > Generate Signed Bundle / APK** e guarde a chave de assinatura.

## Observação legal

O launcher abre um cliente SA-MP instalado. Ele não distribui arquivos do GTA San Andreas.


## Compilação automática

Cada envio para `main` gera o artefato `Mistica-Launcher-APK` no GitHub Actions.
