# Estrategia de pruebas

## Herramientas

JUnit 4 para pruebas locales y pruebas instrumentadas; Compose Test APIs para interacción visual. Room usa bases en memoria o una base exclusiva de migración. DataStore se prueba con un archivo temporal y un scope que se cierra antes de reabrirlo.

Las pantallas comprobadas por UI reciben estado y callbacks, para probarlas sin depender de una cuenta o de los datos reales del teléfono. La actividad vacía PruebaComposeActivity y su manifest solo existen en debug; no se exportan ni se incluyen en release.

## Resultado verificado de esta implementación

Para el perfil estudiantil se verificaron **12 pruebas unitarias y 34 pruebas instrumentadas únicas** en el teléfono, con compilación y lintDebug correctos. PreferenciasEstudianteTest comprueba objetivo, modo oscuro y texto grande por perfil tras reabrir DataStore; PerfilEstudianteUiTest comprueba el acceso junto al saludo, ajustes, selección del objetivo y continuación. En la ejecución completa, dos comprobaciones de interfaz necesitaron corregir su desplazamiento dentro de listas diferidas; sus suites se repitieron y pasaron. La cámara y galería reutilizan SelectorFotoEstudiante y FotoStorage; su interacción con aplicaciones externas sigue requiriendo comprobación manual.

### Revisión general anterior

En la revisión general de diseño y funcionamiento se aprobaron **12 pruebas unitarias y la suite completa de 31 pruebas instrumentadas en el teléfono conectado**. assembleDebug y lintDebug finalizaron correctamente. Se ajustó la paleta docente y la distribución de sus tarjetas, la recuperación de errores, la protección frente a toques repetidos en Siguiente y la conservación de la selección al reintentar el progreso. Las pruebas existentes cubren navegación, formularios, ventanas adaptativas, Room y migraciones, pistas, preferencias e importación de imágenes; no sustituyen la revisión manual de todos los dispositivos ni la captura con una aplicación de cámara real.

### Ejecución anterior de pistas

Compilación y lintDebug completados correctamente. En la actualización de pistas y acompañamiento se aprobaron **12 pruebas unitarias**, **13 pruebas instrumentadas de datos en el teléfono y en el emulador**, y **9 pruebas de interfaz en el teléfono** (AyudaEstudianteUiTest y AventuraUiTest). Se verificaron la migración 7→8, conservación de perfiles e intentos, pistas por modalidad y edición autorizada, recuperación de pendientes, apertura/cierre de pista sin perder selección y celebración al completar la asignatura.

En la primera ejecución de interfaz, cuatro pruebas de AventuraUiTest no encontraron la jerarquía Compose del teléfono; al repetir esa suite con la interfaz disponible, sus seis pruebas pasaron. La suite completa histórica de adaptabilidad, cámara y demás pantallas no se volvió a ejecutar en esta actualización.

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
| ActividadesAprendizajeTest | Room, migración 6 a 8, continuación, intentos, cierre, historial, informes y envío simultáneo |
| PistasAprendizajeTest | Migración 7 a 8 conservando respuestas, pista por modalidad, autorización de edición y recuperación de pregunta pendiente |
| AyudaEstudianteUiTest | Abrir/cerrar pista sin alterar la selección, ausencia de pistas en evaluación y celebración al llegar al 100 % |
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
