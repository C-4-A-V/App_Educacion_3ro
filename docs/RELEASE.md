# Preparación de APK release

## Estado actual

La variante release se compila con R8 y reducción de recursos, pero las funciones de aula compartida están limitadas a depuración. La dirección local por USB solo se configura en debug. Release tiene una dirección reservada no operativa y no debe considerarse conectada a Spring.

El backend `aula-local` no autentica docentes y solo debe utilizarse para pruebas locales. Antes de habilitar acceso desde release hay que implementar autenticación y autorización, usar HTTPS y disponer de una dirección accesible para el teléfono. No incluir contraseñas del servidor ni credenciales de base de datos en la app o el repositorio.

Las cuentas de demostración siguen en el código común; separarlas para release sigue pendiente. Los estudiantes, fotos y resultados locales aún no se sincronizan entre dispositivos.

## Firma

El APK release requiere una clave de firma. El proyecto acepta cuatro variables de entorno:

- `MPAZ_FIRMA_ARCHIVO`: ruta absoluta al archivo de clave.
- `MPAZ_FIRMA_ALMACEN`: contraseña del almacén.
- `MPAZ_FIRMA_ALIAS`: alias de la clave.
- `MPAZ_FIRMA_CLAVE`: contraseña de la clave.

Se deben proporcionar las cuatro o ninguna. Sin ellas, `assembleRelease` produce un APK sin firmar. Los archivos de claves están excluidos de Git; no compartir ni subir las contraseñas. Una clave temporal de prueba sirve para verificar el procedimiento, no para firmar la entrega.

La clave de entrega y su respaldo deben conservarse de forma segura: futuras actualizaciones deben firmarse de manera compatible. Una instalación existente con la misma aplicación pero firmada con otra clave no puede actualizarse directamente; no desinstalar la app existente para probar, porque eso puede borrar sus datos locales.

## Verificaciones pendientes para la entrega

1. Confirmar la identidad del equipo y crear o localizar la clave de firma de entrega.
2. Completar y proteger el flujo compartido de Spring para release y configurar su dirección HTTPS.
3. Probar en un dispositivo separado o con una instalación aislada para no comprometer los datos existentes.
4. Verificar firma, arranque, conexión, publicación y respuesta, además de las pruebas y la documentación.
