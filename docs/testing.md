# Estrategia de pruebas

## Herramientas

JUnit 4 para pruebas locales y pruebas instrumentadas; Compose Test APIs para interacción visual. Room usa bases en memoria o una base exclusiva de migración. DataStore se prueba con un archivo temporal y un scope que se cierra antes de reabrirlo.

Las pantallas comprobadas por UI reciben estado y callbacks, para probarlas sin depender de una cuenta o de los datos reales del teléfono. La actividad vacía PruebaComposeActivity y su manifest solo existen en debug; no se exportan ni se incluyen en release.

## Resultado verificado de esta implementación

Compilación y lintDebug completados correctamente. Suite completa: **10 pruebas unitarias y 21 pruebas instrumentadas aprobadas** en el teléfono conectado. Esto incluye las tres distribuciones adaptativas, errores del formulario, la migración y la conservación de la cuenta de demostración.

## Comandos

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Para una suite concreta:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.mpazpro.AdaptabilidadUiTest
```

Mantener el teléfono desbloqueado y conectado. No ejecutar pruebas sobre una entrega congelada sin comprobar primero las instrucciones del docente.

## Cobertura

| Suite | Qué verifica |
| --- | --- |
| ActividadAprendizajeValidatorTest | Modalidades y obligatoriedad/límites de explicación |
| ActividadCamposTest | Errores simultáneos por campo, duplicados y banco de 40 preguntas válido |
| WindowSizeTest | Puntos de corte 600/840 dp y columnas por clase |
| ActividadesAprendizajeTest | Room, migración 6 a 7, intentos, cierre, historial, informes y envío simultáneo |
| DatosDemostracionTest | Cuenta inicial, diez preguntas por asignatura, concurrencia e idempotencia, conservación de clave y resultados |
| AventuraUiTest | Saludo, texto ampliado, envío bloqueado, reintento y explicación final |
| FormularioActividadUiTest | Errores visibles junto al control y corrección del título |
| AdaptabilidadUiTest | Posiciones reales de tarjetas en 400, 610 y 900 dp usando ForcedSize |
| PreferenciasLecturaTest | Preferencia persistente tras reabrir DataStore |
| FotoStorageTest | Importación de una imagen sintética, rechazo de formato inválido y limpieza de capturas |
| ExampleUnitTest / ExampleInstrumentedTest | Comprobaciones básicas originales del proyecto |

## Vistas previas y evidencia visual

InicioAventuraPreview contiene teléfono, ventana mediana, expandida, horizontal, oscuro y texto ampliado. Los previews pasan por ContenidoAdaptativo, igual que la app. El ancho se obtiene del espacio disponible y se clasifica con Material 3.

Las pruebas instrumentadas verifican la distribución real de tarjetas, sin modificar la resolución global del teléfono. No se generaron ni aprobaron imágenes de referencia de screenshot testing.

En esta sesión el renderizador de previews de Studio presentó caché de clases con NoSuchMethodError después de actualizar utilidades. El APK y las pruebas del dispositivo compilan y ejecutan el código actualizado. Si sucede en el editor, refrescar/reconstruir el preview; no confundir ese fallo del renderizador con un fallo de la app sin verificarlo en Run.

## Prueba manual previa a entregar

- Ingresar como docente y recorrer cursos, estudiantes, actividades y resultados; probar Atrás.
- Registrar datos inválidos en cada formulario y corregirlos.
- Capturar una foto, aceptarla y verla; seleccionar una imagen de galería; cancelar ambas acciones.
- Denegar cámara y comprobar que galería sigue disponible.
- Cerrar y abrir la app: conservar fotos, cursos, actividades e intentos.
- Activar Lectura cómoda y comprobar su persistencia.
- Abrir una práctica, fallar, salir y continuar; confirmar oportunidades conservadas.
- Verificar teléfono, tablet/ventana mediana, ventana expandida, horizontal y texto grande.
- Guardar capturas reales en el tablero del equipo, sin datos personales de menores.

Las pruebas de importación usan imágenes sintéticas. No sustituyen la demostración manual de la aplicación de cámara y del selector de galería de un teléfono físico.
