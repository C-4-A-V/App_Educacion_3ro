# MPAZ PRO

Aplicación Android educativa para estudiantes de 3.º básico en Chile. Permite que docentes organicen cursos y actividades, y que estudiantes practiquen Lenguaje, Matemática, Ciencias Naturales e Historia con retroalimentación y seguimiento de progreso.

Proyecto académico de **DSY1105: Desarrollo de Aplicaciones Móviles**, Duoc UC.

## Equipo y seguimiento

- Camilo Alejandro Alegría Vargas: identidad presente en el historial de Git.
- Javier / jav-rgb: identidad presente en el historial de Git; confirmar nombre completo antes de entregar.
- Número de grupo: pendiente de confirmación del equipo.
- Repositorio configurado: [App_Educacion_3ro](https://github.com/C-4-A-V/App_Educacion_3ro).
- Tablero Trello: pendiente de proporcionar el enlace.
- [Planificación para trasladar al tablero](docs/PLANIFICACION.md).
- [Cumplimiento técnico y evidencias pendientes](docs/CUMPLIMIENTO_GUIAS_DSY1105.md).

El registro de autores de Git no sustituye la confirmación de los integrantes ni las evidencias reales de colaboración.

## Funcionalidades

- Acceso y registro local de docentes, con hash y salt de contraseña.
- Creación de cursos y registro de estudiantes con fotografía.
- Cámara mediante TakePicture, galería mediante GetContent y FileProvider de alcance limitado.
- Creación de actividades con cuatro alternativas, validaciones por campo y explicación de la respuesta.
- Prácticas de hasta tres intentos y evaluaciones de un intento.
- Bienvenida personalizada después de seleccionar el perfil del estudiante.
- Una estrella por actividad acertada; el progreso cuenta actividades terminadas, sin duplicar reintentos.
- Informes por estudiante, curso y asignatura y exportación JSON.
- Cuadrículas de una, dos y tres columnas para ventanas compactas, medianas y expandidas.
- Navegación docente con barra inferior o lateral, según espacio disponible.
- Feedback animado de selección, errores y resultados.
- Preferencia de Lectura cómoda guardada con DataStore.

## Ejecutar en Android Studio

1. Clonar este repositorio o abrir su carpeta raíz en Android Studio.
2. Usar una versión de Android Studio compatible con **AGP 9.5.0** y el JDK integrado compatible con el proyecto.
3. Instalar Android SDK **API 37** desde SDK Manager y aceptar sus licencias.
4. Sincronizar Gradle. Las dependencias se administran en gradle/libs.versions.toml.
5. Seleccionar el módulo app y un teléfono o emulador Android **API 26 o superior**.
6. Pulsar Run. En una instalación nueva se preparan la cuenta y las actividades de demostración.
7. Abrir “Ingresar como docente”. Desde el panel se pueden crear estudiantes en el curso para probar su área.

Para cámara y galería, probar también en un teléfono físico. La captura pide permiso de cámara porque la app lo declara; el selector de galería concede acceso al archivo elegido sin requerir lectura de toda la biblioteca.

## Cuenta y actividades de demostración

| Dato | Valor |
| --- | --- |
| Correo | milo@gmail.com |
| Clave inicial de instalación nueva | Alegria2002 |
| Curso inicial si no existe ninguno | 3.º Básico A |
| Actividades | 10 por asignatura; 40 preguntas originales con explicación |
| Modalidad de las actividades predeterminadas | Práctica, hasta tres intentos |

La carga no modifica la contraseña de una cuenta Milo existente. Si ya tiene cursos, se utiliza el primero por ID. Las preguntas se agregan solo cuando no existe una actividad con el mismo curso, asignatura y título; no se borran actividades, estudiantes ni resultados.

Son credenciales ficticias de demostración, no una cuenta externa ni un sistema de autenticación en la nube. Los estudiantes se registran desde el panel docente y seleccionan su perfil para ingresar.

El banco es contenido original orientado al nivel escolar. El docente puede revisar su pertinencia; no se presenta como material certificado por MINEDUC.

## Reglas de negocio

- Cada curso pertenece a un docente; las consultas de gestión comprueban esa relación.
- Un estudiante responde actividades de su propio curso.
- Una práctica termina al acertar o consumir tres intentos. Una evaluación termina con el primer envío.
- Una respuesta intermedia incorrecta conserva sus oportunidades al salir y volver a entrar.
- La respuesta correcta y la explicación se muestran al finalizar.
- Se conserva el historial de intentos; el informe usa el último estado.
- Los envíos repetidos con el mismo número esperado no consumen otra oportunidad.
- Se requieren cuatro alternativas distintas y una respuesta correcta válida.
- Las fotografías importadas tienen un límite de 10 MB y se comprueba que tengan dimensiones de imagen válidas.
- La sesión docente es local y vuelve a requerir acceso tras recrear el proceso; las contraseñas no se guardan en SavedStateHandle.
- El perfil estudiantil seleccionado y el borrador de actividades se restauran con SavedStateHandle; la base de datos conserva las respuestas.

## Arquitectura

| Capa | Responsabilidad |
| --- | --- |
| ui/screens y ui/components | Presentación declarativa, accesibilidad y callbacks |
| ui/viewmodel | Estado reactivo, acciones y coordinación mediante corrutinas |
| model y validation | Datos de UI y validación desacoplada de Compose |
| data/repository | Operaciones de datos y reglas de acceso |
| data/local | Entidades, DAOs, Room, migraciones y carga predeterminada |
| data/media | Importación privada y archivos temporales de imágenes |
| navigation | Rutas reutilizables y NavHost |

Room usa actualmente el esquema 7 con migraciones explícitas; no se borra la base de datos como mecanismo de actualización. DataStore guarda la preferencia pequeña de lectura, separada de las entidades académicas. Los datos se guardan en este dispositivo; no hay sincronización entre teléfonos.

## Pruebas

En PowerShell desde la raíz:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

El segundo comando requiere un dispositivo conectado y disponible. Las pruebas utilizan bases de datos y preferencias de prueba; no borran los cursos del usuario.

Cobertura y ejecución detalladas en [docs/testing.md](docs/testing.md). Las vistas previas de InicioAventuraPreview incluyen teléfono, ventana mediana, expandida, horizontal, tema oscuro y texto ampliado.

## Preparar la entrega

- Confirmar nombres completos, grupo y enlace del tablero.
- Adjuntar capturas auténticas de cámara, galería, formularios y tres tamaños de ventana.
- Confirmar acceso del docente y del equipo al repositorio y al tablero.
- Revisar con el docente la diferencia entre repositorio privado de la guía inicial y público en la pauta de entrega.
- Confirmar que GitHub y el archivo entregado en AVA contienen la misma versión, incluido el esquema Room.
- Respetar el cierre de entrega y no publicar cambios posteriores si la pauta ya está en ese período.
- Ensayar la defensa individual de 15 minutos, incluida una modificación en vivo; durante la defensa rigen las restricciones de ayuda indicadas por la asignatura.
