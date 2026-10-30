# TV Launcher

Launcher mínimo para Android TV: una fila de apps instaladas, fondos que rotan cada 30 s, sin anuncios ni red.

- Control remoto: ◀ ▶ para moverte, **OK** abre la app, **OK largo** abre sus ajustes (desinstalar, etc.). **Atrás** no cierra el launcher.
- Categorías declaradas: `HOME`, `DEFAULT`, `LEANBACK_LAUNCHER`.

## Descarga directa

`https://github.com/<usuario>/<repo>/releases/latest/download/TVLauncher.apk`

## Instalar y poner como inicio

```bash
adb connect <IP_DEL_TV>
adb install -r TVLauncher.apk
# Si el sistema no pregunta qué launcher usar:
adb shell cmd package set-home-activity com.tvlauncher/.MainActivity
```

## Publicar una versión

```bash
git tag v1.0.1 && git push origin v1.0.1
```

El workflow compila y sube `TVLauncher.apk` a Releases. Firma: por defecto con la clave debug; para una firma estable
define los secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` y `KEY_PASSWORD`.
