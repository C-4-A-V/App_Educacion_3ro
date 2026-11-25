# Preparación de APK Release

## Firma y Compilación

La variante Release de la aplicación se compila con optimizaciones R8 y reducción de código.

Para compilar el APK Release directamente:

```bash
./gradlew assembleRelease
```

El script de Gradle configura una firma automática por defecto para permitir la instalación del APK generado en `app/build/outputs/apk/release/app-release.apk`.

### Firma Personalizada de Producción (Opcional)
Para firmar con un almacén de claves (keystore) personalizado en un entorno de integración continua (CI/CD), configura las siguientes variables de entorno:

- `MPAZ_FIRMA_ARCHIVO`: ruta absoluta al archivo `.keystore` / `.jks`.
- `MPAZ_FIRMA_ALMACEN`: contraseña del almacén.
- `MPAZ_FIRMA_ALIAS`: alias de la clave.
- `MPAZ_FIRMA_CLAVE`: contraseña del alias.

### Dirección del Servidor de Producción (HTTPS)
Para especificar la dirección HTTPS de producción durante la compilación Release:

```bash
./gradlew assembleRelease -PmpazServidorUrl=https://servidor-produccion.ejemplo.cl/
```

Debe ser una URL HTTPS válida terminada en `/`.

## Verificaciones para la entrega

1. Compilar y generar `app-release.apk` ejecutando `./gradlew assembleRelease`.
2. Verificar la instalación del APK generado en un dispositivo o emulador de prueba.
3. Ejecutar las pruebas unitarias e instrumentadas antes de publicar.
