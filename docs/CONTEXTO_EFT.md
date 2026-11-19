# MPAZ PRO: contexto y alcance de la Experiencia 3

Estado: **borrador para revisar con el equipo y contrastar con la pauta completa del EFT**.

## Contexto

MPAZ PRO es una aplicación educativa Android para apoyar la práctica de estudiantes de 3.º básico en Chile. El docente organiza cursos y actividades; los estudiantes resuelven preguntas de Lenguaje, Matemática, Ciencias Naturales e Historia y reciben retroalimentación.

El proyecto plantea un contexto académico simulado. El banco predeterminado es contenido original que el docente debe revisar; no se presenta como material certificado por MINEDUC. Las demostraciones deben usar perfiles y datos ficticios.

## Problema que aborda

El docente necesita preparar actividades y consultar el avance de su curso. El estudiante necesita una forma clara y motivadora de practicar, recibir pistas y retomar su progreso. Actualmente los datos académicos se guardan en el dispositivo, por lo que no se comparten entre equipos.

## Usuarios y flujos

- **Docente:** ingresar, gestionar cursos y estudiantes, preparar actividades y consultar resultados.
- **Estudiante:** seleccionar su perfil, practicar por asignatura, recibir retroalimentación y consultar su progreso.
- **Equipo desarrollador:** mantener la app y los servicios, ejecutar pruebas y documentar cómo reproducir la solución.

El alcance final, en particular las operaciones CRUD y las relaciones entre cuentas, cursos y resultados, se definirá antes de desarrollar el backend. El acceso mediante selección de perfil es local y no debe presentarse como autenticación segura para datos reales.

## Punto de partida

La app utiliza Kotlin, Jetpack Compose, navegación, ViewModels, repositorios, Room y DataStore. Ya existen actividades, perfiles, respuestas, progreso y pruebas. Según la documentación actual, los datos se almacenan localmente; no existe sincronización entre dispositivos.

Las conexiones externas, el backend Spring Boot, la cobertura exigida y la firma release requieren implementación y verificación específica. No se consideran completados por figurar en este documento.

## Alcance propuesto para la Experiencia 3

1. **API externa:** incorporar información pertinente al contenido educativo mediante Retrofit, con estados visibles de carga y error. La API y su flujo aún no están elegidos.
2. **Microservicios propios:** diseñar los servicios Spring Boot y su base de datos en función de los datos y operaciones que el proyecto necesite. Definir contratos, seguridad y flujo CRUD antes de implementarlos.
3. **Integración móvil:** conectar las pantallas pertinentes con los servicios. Definir qué datos siguen en Room, cómo se sincronizan y cómo manejar desconexiones y conflictos sin perder avances.
4. **Pruebas:** mantener las pruebas existentes, añadir pruebas de repositorios y ViewModels y generar un reporte que permita verificar el requisito de al menos 80 % del código lógico.
5. **Entrega:** generar y probar un APK release firmado; documentar instalación, configuración, endpoints, pruebas y evidencias reales de colaboración.

La sincronización de datos y el significado de «tiempo real» en la pauta se confirmarán con el docente. El alcance de la API externa y el número y responsabilidades de los microservicios siguen pendientes de decisión.

## Criterios de aceptación del contexto

- El equipo puede describir el problema, los usuarios y los flujos principales.
- Las funciones remotas propuestas tienen relación clara con el aprendizaje o la gestión docente.
- Se identifica el punto de partida local y se evita prometer sincronización que aún no existe.
- El alcance definitivo se contrasta con la pauta completa del EFT y con las aclaraciones del docente.

## Consultas pendientes al docente

- ¿Qué comportamiento concreto se espera para la actualización «en tiempo real»?
- La pauta solicita repositorio público y entrega de una llave de firma: ¿puede la llave .jks entregarse por un medio privado, dejando fuera del repositorio las contraseñas y el material secreto?
- ¿Cómo se define y mide el 80 % de cobertura del código lógico para esta entrega?
- ¿La pauta del EFT exige una API o cantidad de microservicios específica?
