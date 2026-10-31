# Revisión de MPAZ PRO frente a guías y evaluaciones DSY1105

Fecha: 31 de octubre de 2026.

## Resultado general

El proyecto tiene una base funcional de Compose, Material 3, MVVM, navegación y persistencia con Room. No se puede declarar cumplimiento total. Las principales brechas son adaptación por tamaños de ventana, validación visual completa en el formulario de actividades, animaciones explícitas, documentación de entrega y evidencias externas de colaboración.

Esta revisión no modifica el código de la aplicación. Se inspeccionaron los textos de los nueve PDF, el código actual, el historial local de Git y resultados de compilación y pruebas. Los ejemplos de código incrustados como imágenes en los PDF no se transcribieron: los requisitos se contrastaron con las instrucciones y rúbricas textuales. No se asigna una nota ni se presume cumplimiento por el solo hecho de compilar.

## Comprobaciones ejecutadas

| Comprobación | Resultado | Alcance |
| --- | --- | --- |
| assembleDebug | Correcto | Compila con la configuración actual del proyecto, incluidas las actualizaciones de AGP y KSP del usuario. |
| testDebugUnitTest | 5 aprobadas | Incluye validación de explicaciones y modalidades de actividad; no cubre todos los formularios. |
| lintDebug | Tarea completada correctamente | No equivale a afirmar que existan cero advertencias. El informe de build está excluido de lectura por políticas del IDE. |
| assembleDebugAndroidTest | Correcto | Compilan las pruebas instrumentadas. |
| ActividadesAprendizajeTest en teléfono conectado | 7 aprobadas | Migración 6 a 7, datos conservados, práctica, evaluación, historial, conteos y envíos simultáneos. |
| AventuraUiTest en teléfono conectado | 4 fallidas | Las cuatro fallan con “No compose hierarchies found”. La inspección previa mostraba bloqueo/AOD. Repetir desbloqueado; si persiste, investigar actividad y regla de pruebas. No se consideran verificadas las interacciones visuales. |

La ejecución de pruebas usa bases de datos de prueba, sin borrar la base de datos real de la app.

## Matriz por documento

| Documento | Estado | Evidencia y diferencia pendiente |
| --- | --- | --- |
| Guía 7: MVVM y colaboración | Parcial | Existen paquetes model, ui/screens, ui/viewmodel, data/repository, data/local y navigation. Hay historial de Git con varias identidades de autor y referencia origin/master. No se encontró README ni evidencia local de Trello, accesos de colaboradores o contexto/reglas de negocio documentados. |
| Guía 8: pantalla Compose | Cubierto en código; demostración visual parcial | MainActivity usa setContent, tema y Scaffold. Hay componentes, tarjetas, imágenes, espaciado y colores temáticos. Las vistas previas actuales cubren teléfono, modo oscuro y texto ampliado. No hay evidencia local de capturas adjuntas al tablero. |
| Guía 9: adaptabilidad | No cubierto el objetivo específico | No se encontraron Window Size Classes ni diseños diferenciados compacto/mediano/expandido. LazyColumn, pesos y fillMaxWidth ayudan a evitar desbordes, pero no cumplen por sí solos el ejercicio de adaptación. |
| Guía 10: navegación | Parcial | Screen es sealed class y AppNavigation usa NavHost, argumentos tipados, callbacks y control de sesión. No se encontraron TopAppBar, NavigationBar o Drawer propios ni la capa de eventos descrita por el ejercicio. El flujo por callbacks es válido como arquitectura; la reproducción literal de ese ejercicio debe acordarse con el docente. |
| Guía 11: formularios | Parcial | Login y registro docente usan StateFlow, validadores, isError y supportingText. Cursos y estudiantes también tienen errores específicos. Crear actividad solo muestra un mensaje general: no marca ni describe el error junto a cada campo. Los iconos de error tampoco son uniformes en todos los formularios. |
| Guía 12: estados y animaciones | Parcial | Hay StateFlow, observación con ciclo de vida, estados de carga y persistencia Room. No se encontraron AnimatedVisibility, animateColorAsState ni otras animaciones propias de feedback. Los loaders y transiciones predeterminadas aportan algo, pero no evidencian todo el ejercicio. No hay DataStore ni un estado de preferencia equivalente al interruptor de la guía. |
| Guía 13: cámara y galería | Implementado; prueba manual pendiente | SelectorFotoEstudiante usa TakePicture y GetContent, permisos de cámara, URI guardado con rememberSaveable y cancelación sin reemplazar la foto previa. FileProvider no exportado y limitado a cache/camara. FotoStorage copia imágenes al almacenamiento interno y valida tamaño/formato. ImagenInteligente muestra recorte circular y recurso de respaldo. Falta demostrar cámara, galería y cancelaciones en físico. |
| Parcial 2: entrega | Cumplimiento parcial | La app cubre arquitectura y persistencia; formularios, adaptación, animaciones y evidencia colaborativa necesitan trabajo. README obligatorio ausente. Publicación, accesos, Trello y entregas AVA no se pueden comprobar con el repositorio local. |
| Parcial 3: defensa individual | No certificable mediante inspección | El código ofrece funcionalidades para demostrar. La defensa exige ejecutar, explicar y modificar en vivo validaciones, componentes, persistencia y recursos nativos. Ese dominio individual debe ensayarse. |

### Cómo interpretar las diferencias con los ejercicios

- No es necesario crear un módulo Gradle por pantalla: la separación actual por responsabilidades es válida para este tamaño de app.
- No hace falta sustituir Room por DataStore. Room cumple el almacenamiento de datos del proyecto; DataStore sería apropiado si se incorpora una preferencia persistente como la del ejercicio.
- Cámara y galería son los dos recursos nativos de la guía 13. No es necesario añadir GPS u otro recurso sin una necesidad de la app.
- Las versiones antiguas que aparecen en ejemplos no obligan a degradar las dependencias actuales. La configuración actual pasó compilación.
- La guía 7 propone repositorio privado; la evaluación 2 pide público. Para la entrega se debe aclarar esa diferencia con el docente y seguir la pauta que indique. Esta revisión no cambia la visibilidad.
- El nombre de proyecto no sigue literalmente AppNombre_GrupoX de la guía 7. Confirmar si esa convención se exige en la entrega; no renombrar por cuenta propia solo por el ejemplo.

## Hallazgos prioritarios

### 1. Adaptación real de la interfaz - prioridad alta

**Base:** guía 9, páginas 1 a 5; IL2.1 de ambas evaluaciones.

**Evidencia:** ui/screens/InicioEstudianteScreen.kt mantiene una sola LazyColumn para cualquier ancho. ui/screens/AventuraPreviews.kt tiene vistas de 320 y 360 dp; no hay vistas de tamaños mediano y expandido. La búsqueda de APIs de tamaños de ventana no encontró implementaciones.

**Corrección propuesta:** incorporar un cálculo compartido de clase de tamaño de ventana y distribuir el inicio en una columna en compacto, dos columnas en mediano y un diseño más amplio en expandido. Aplicar límites de ancho a formularios. Mantener navegación y estado al rotar o redimensionar.

**Aceptación:** capturas y pruebas en tres anchos representativos, orientación horizontal y texto ampliado, sin campos inaccesibles ni pérdida del formulario durante rotación.

### 2. Errores por campo en Crear actividad - prioridad alta

**Base:** guía 11, páginas 3 a 6; IE2.1.2 de la evaluación 2.

**Evidencia:** validation/ActividadValidator.kt devuelve el primer mensaje como String. ui/screens/CrearActividadScreen.kt, función CampoActividad, no recibe error ni usa isError, supportingText o icono de error. El mensaje general aparece en la parte superior y puede quedar fuera de la vista del campo.

**Corrección propuesta:** modelar errores de título, instrucciones, pregunta, explicación, alternativas y respuesta correcta. Mantener validación en la capa lógica y mostrar cada error junto al control. Validar de nuevo al editar después del primer intento de guardar.

**Aceptación:** enviar vacío, introducir alternativas repetidas y exceder límites; cada control debe indicar su propio problema, conservar valores y permitir corregirlo sin ambigüedad.

### 3. Animaciones relacionadas con acciones - prioridad alta

**Base:** guía 12, especialmente páginas 2 y 5; IE2.2.2 de la evaluación 2.

**Evidencia:** no se encontraron APIs de animación propias en los Kotlin del proyecto. Hay indicadores de progreso y la navegación puede usar transiciones de biblioteca; eso no basta para presentar como implementado todo el objetivo de la guía.

**Corrección propuesta:** animar la aparición de feedback al guardar o responder y el cambio de estado de una alternativa o del progreso. Relacionar la animación con el estado real, sin retrasos artificiales ni efectos continuos que distraigan.

**Aceptación:** demostrar éxito, error y carga; explicar qué estado activa cada animación y confirmar que no guarda datos ni duplica eventos por recomposición.

### 4. README y evidencia de entrega - prioridad alta

**Base:** evaluación 2, página 3; guías 7 a 13.

**Evidencia:** find_files no encontró README. Git tiene commits técnicos y varias identidades de autor; solo se observaron master y origin/master. Existen cambios pendientes y archivos nuevos, incluidos el esquema Room 7 y pruebas. Esto no prueba que estén subidos al remoto.

**Corrección propuesta:** documentar descripción, equipo, contexto, alcance, reglas de negocio, requisitos de ejecución, arquitectura, funcionalidades, dos recursos nativos, pruebas y enlaces a evidencias. Registrar tareas reales en Trello, responsables, checklist y capturas. No inventar actividad histórica ni asumir que varias identidades de Git representan personas distintas.

**Aceptación:** otra persona puede clonar la entrega, sincronizarla y ejecutarla siguiendo el README; el docente puede acceder al repositorio y tablero. Comprobar las entregas grupal e individual en AVA.

### 5. Pruebas visuales todavía fallidas - prioridad alta

**Evidencia:** las cuatro AventuraUiTest fallan antes de poder consultar la jerarquía Compose. La inspección previa del teléfono mostró AOD.

**Corrección propuesta:** desbloquear el teléfono, comprobar la actividad de pruebas y repetir la suite. Si sigue fallando, revisar lanzamiento de ComponentActivity, ComposeTestRule y ejecución instrumentada antes de atribuirlo a la UI de producción.

**Aceptación:** pasan las cuatro pruebas y se demuestra manualmente selección de perfil, envío sin selección bloqueado, reintento y explicación final. No declarar que toda la app falla a partir de este error de preparación.

## Mejoras de mantenibilidad y robustez

Estas observaciones complementan las guías; no son requisitos nuevos de la rúbrica.

- **Errores silenciosos:** ui/viewmodel/AccesoEstudianteViewModel.kt termina la observación del progreso con catch (_: Exception) vacío. Debe conservar CancellationException y comunicar un fallo recuperable; de lo contrario, una falla puede dejar cifras viejas sin aviso.
- **Reintento de carga:** ResolverActividadViewModel.cargar guarda contexto antes de terminar y retorna si se pide el mismo contexto. Después de una excepción, la misma llamada no vuelve a intentar. Incorporar una acción de reintento que fuerce carga.
- **Restauración de estado:** se usa rememberSaveable en cámara y menú, pero no SavedStateHandle para sesión o formularios. Los ViewModels sobreviven a rotación; eso no garantiza restauración tras muerte del proceso. Persistir solo lo necesario y evitar guardar contraseñas.
- **Responsabilidades:** AppNavigation concentra unas 750 líneas y algunos ViewModels consultan DAOs directamente. Separar grafos y uniformar acceso mediante repositorios puede simplificar pruebas; no requiere una reestructuración total.
- **Accesibilidad:** los botones de mostrar contraseña usan emojis sin una descripción explícita de la acción. Usar iconos con “Mostrar contraseña” / “Ocultar contraseña”. Extraer textos de UI a recursos; strings.xml contiene solo app_name.
- **Fotografías:** las capturas temporales y las fotos importadas que se reemplazan o abandonan no tienen limpieza visible. Definir eliminación de archivos huérfanos, sin borrar la imagen vigente.
- **Uso académico con menores:** mantener datos de demostración y fotografías ficticias para la entrega; documentar qué se guarda localmente y la finalidad de las imágenes. La foto obligatoria en registro de estudiantes es una regla actual que conviene justificar, no una exigencia de estas guías.

### Permisos: diferencia importante con la guía

No se debe añadir acceso amplio a toda la galería solo para reproducir una lista de permisos. El selector delega al sistema mediante GetContent y la app copia el URI elegido a almacenamiento interno. La ausencia de READ_MEDIA_IMAGES no demuestra que falte esa funcionalidad.

La [documentación oficial sobre minimizar permisos](https://developer.android.com/privacy-and-security/minimize-permission-requests) favorece selectores y acceso acotado. También explica que la captura delegada a la aplicación de cámara puede prescindir de CAMERA; si se conserva su declaración actual, debe mantenerse el flujo de concesión que ya existe. Cualquier ajuste debe probarse en el dispositivo y explicarse durante la defensa.

## Protocolo de preparación de la entrega y defensa

1. Completar adaptación, validaciones por campo y animaciones; repetir pruebas con el teléfono desbloqueado.
2. Demostrar cámara y galería en físico: permiso concedido, denegado, cancelación, foto válida y persistencia tras cerrar y abrir.
3. Demostrar Room: crear curso, estudiante y actividad; responder; cerrar y volver a abrir; confirmar datos e informes.
4. Preparar README, evidencias, tareas y commits técnicos con participación real del equipo.
5. Antes del plazo, comprobar que el contenido local, GitHub y el archivo entregado en AVA corresponden a la misma versión.
6. Confirmar accesos del docente y los miembros, visibilidad exigida y activaciones individual/grupal en AVA.
7. La evaluación 2 establece que no se modifique la entrega entre el cierre y la evaluación. Confirmar la fecha aplicable antes de publicar nuevas modificaciones.
8. Ensayar individualmente los 15 minutos de defensa de la evaluación 3: entorno, MVVM, diseño, formularios, estados, animaciones, persistencia, recursos y cambio de código en vivo.
9. Practicar sin asistencia: la evaluación 3 indica que durante la defensa no se admite IA ni recursos de apoyo.

## Fuentes revisadas

Todas las rutas siguientes corresponden a los archivos proporcionados por el usuario:

- Guía 7: Configuración inicial del proyecto móvil con MVVM y herramientas colaborativas, páginas 1 y 2.
- Guía 8: Construcción visual de pantalla base con Jetpack Compose, páginas 1 a 3.
- Guía 9: Adaptabilidad del diseño, páginas 1 a 5.
- Guía 10: Navegación y estructura visual en aplicaciones, páginas 1 a 11.
- Guía 11: Formularios, validaciones y pasado de información, páginas 1 a 11.
- Guía 12: Animaciones y Estados, páginas 1 a 7.
- Guía 13: Integrando Recurso Nativo Camara, páginas 1 a 3.
- Ev. Parcial 2_Estudiante_Encargo_DSY1105, páginas 1 a 6.
- Evaluación Parcial 3_Estudiante_Presentación_DSY1105, páginas 1 a 8.

La revisión del código no permite certificar el tablero externo, permisos remotos del repositorio, fechas reales de entrega, registros de AVA ni dominio individual durante la defensa.
