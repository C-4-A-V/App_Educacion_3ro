# Backend de MPAZ PRO

Proyecto Spring Boot independiente de Android. La versión de depuración permite conectar cursos, compartir preguntas confirmadas y recibir respuestas de prueba. La sincronización de estudiantes y resultados entre dispositivos, y la autenticación del servidor, todavía no están terminadas.

## Requisitos

- JDK 25 (por ejemplo, el que viene con Android Studio).
- Acceso a Maven Central para descargar las dependencias.

## Ejecutar en Windows

Desde la raíz de MPAZ PRO, usando el wrapper de Gradle existente:

```powershell
.\gradlew.bat -p backend '-Dorg.gradle.java.home=C:\Program Files\Android\Android Studio\jbr' bootRun
```

El servidor escucha únicamente en el computador local. Abre `http://127.0.0.1:8080/api/salud`: debe indicar que el servicio está activo. Para detenerlo, presiona Ctrl+C en la terminal.

## Catálogo compartido de desarrollo

Se utiliza una base separada de la base anterior `datos/mpaz`. Desde la raíz del proyecto:

```powershell
.\gradlew.bat -p backend '-Dorg.gradle.java.home=C:\Program Files\Android\Android Studio\jbr' bootRun '--args=--server.port=8083 --spring.datasource.url=jdbc:h2:file:./datos/catalogo_compartido --spring.jpa.hibernate.ddl-auto=update --spring.profiles.active=aula-local'
```

Para el emulador conectado, en otra terminal ejecuta:

```powershell
adb -s emulator-5554 reverse tcp:8080 tcp:8083
```

Si el identificador del emulador cambia, consulta `adb devices` y usa el indicado. La pantalla docente «Actividades del servidor · prueba» permite revisar cursos. Los cursos vinculados mantienen sus datos locales. Las actividades nuevas se guardan primero en el dispositivo y el docente decide cuáles compartir; el envío requiere confirmación. Las publicaciones pendientes se pueden reintentar desde la app. En depuración, un estudiante cuyo curso esté vinculado puede consultar y responder preguntas remotas, usando un identificador aleatorio local en lugar de RUT o PIN. Este flujo es experimental: los resultados remotos todavía no aparecen en el resumen docente ni se incluyen en el progreso local, y la identidad del estudiante no se sincroniza entre dispositivos. El perfil `aula-local` es solo para pruebas con datos ficticios: escucha en el computador local, no autentica docentes y no debe exponerse a Internet. Las bases temporales no permiten guardar vínculos.

## Consulta de actividades

- `POST http://127.0.0.1:8080/api/actividades`: crea una actividad de prueba con `asignaturaId`, `titulo` y `pregunta`.
- `GET http://127.0.0.1:8080/api/actividades/{id}`: consulta una actividad por su identificador.
- `PUT http://127.0.0.1:8080/api/actividades/{id}`: actualiza los tres campos de una actividad de prueba.
- `DELETE http://127.0.0.1:8080/api/actividades/{id}`: elimina esa actividad; responde con 404 si no existe.
- `GET http://127.0.0.1:8080/api/actividades`: devuelve todas las actividades del backend.
- `GET http://127.0.0.1:8080/api/actividades?asignaturaId=matematica`: filtra por asignatura.
- Una instalación nueva devuelve `[]` porque la base comienza vacía. Las actividades guardadas en Room no se copian automáticamente.

Para crear una actividad ficticia con el servidor en ejecución, usa una segunda terminal de PowerShell:

```powershell
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/actividades' -Method Post -ContentType 'application/json' -Body '{"asignaturaId":"matematica","titulo":"Suma de prueba","pregunta":"Cuanto es 2 + 2?"}'
```

Se aceptan las asignaturas `matematica`, `lenguaje`, `ciencias` e `historia`; el título y la pregunta son obligatorios. Esta primera versión es un registro de prueba de tres campos, no una actividad completa con alternativas y curso.

La base H2 de desarrollo se guarda en `backend/datos/`, que está excluida de Git. La configuración de actualización automática de tablas es para desarrollo local; antes de una entrega con datos reales habrá que definir migraciones y protección apropiadas.

Para ejecutar la prueba de la API con datos ficticios en una base independiente en memoria:

```powershell
.\gradlew.bat -p backend '-Dorg.gradle.java.home=C:\Program Files\Android\Android Studio\jbr' test
```

El backend usa sus propios archivos de configuración Gradle, aunque por ahora comparte el wrapper del proyecto Android. La API de actividades completas permanece deshabilitada por defecto: su perfil experimental no debe usarse con datos reales.
