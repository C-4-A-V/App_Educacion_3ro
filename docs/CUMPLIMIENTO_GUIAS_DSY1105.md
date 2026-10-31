# Cambios de cumplimiento DSY1105

Este documento actualiza el estado técnico después de REVISION_GUIAS_DSY1105.md. La revisión inicial se conserva como antecedente.

## Implementado

| Guía | Implementación |
| --- | --- |
| 7: MVVM y herramientas | Capas existentes conservadas; README con contexto, reglas, ejecución y repositorio; planificación documentada para el tablero real |
| 8: Compose | Componentes Material 3, tema infantil, imágenes reutilizables y controles con descripciones accesibles |
| 9: adaptación | Clases Material 3 calculadas desde las restricciones reales; una, dos y tres columnas; formularios centrados de ancho limitado; pruebas a 400/610/900 dp |
| 10: navegación | NavHost y rutas existentes conservados; TopAppBar y NavigationSuiteScaffold en panel docente; barra inferior compacta y lateral con más ancho |
| 11: formularios | Errores por cada campo, alternativa y selección correcta; lógica en validación/ViewModel; borrador de actividades separado por curso, docente y asignatura |
| 12: estados | StateFlow, SavedStateHandle para perfil estudiantil y borrador, feedback de entrada y selección animado, progreso animado y Lectura cómoda persistente en DataStore |
| 13: cámara y galería | Contratos TakePicture/GetContent y FileProvider; importación privada; cancelación conserva foto previa; eliminación de temporales al cancelar o importar |

El cambio de diseño se prueba sin bloquear la orientación ni usar configChanges para evitar la recreación de Activity. No fue necesario migrar Navigation Compose a Navigation 3 para los requisitos del curso.

## Actividades de Milo

- Cuenta objetivo: milo@gmail.com.
- Si la cuenta ya existe, no se cambia su contraseña ni nombre.
- Si no existe, se crea la cuenta de demostración con clave inicial Alegria2002.
- Se utiliza el primer curso existente de Milo; si no hay ninguno, se crea 3.º Básico A.
- Se agregan diez preguntas originales por cada una de las cuatro asignaturas, con cuatro alternativas y explicación.
- Son prácticas de hasta tres intentos.
- La carga ocurre en una transacción y no repite una actividad con el mismo curso, asignatura y título.
- No elimina ni reemplaza actividades o respuestas existentes. No requiere desinstalar la aplicación.

## Robustez adicional

- Se eliminó la captura silenciosa de errores al observar progreso; la UI puede mostrar el fallo.
- Se agregó una acción para reintentar la carga de actividades.
- El inicio espera la restauración de perfiles antes de resolver rutas protegidas.
- La contraseña no se recorta antes de comprobarla; el correo sí se normaliza.
- Mostrar/ocultar contraseña tiene una descripción accesible y los errores mantienen su icono.
- La preferencia de lectura cambia el tamaño de texto respetando la escala del sistema.
- Las pruebas de Compose usan una actividad de debug explícita, ajena al flujo productivo.

## Verificación

Resultado final: **10 pruebas unitarias y 21 pruebas instrumentadas aprobadas**, APK compilado y lintDebug completado correctamente.

La compilación y las suites de validación, persistencia, carga predeterminada, DataStore, formulario, recursos de imagen y distribución adaptativa se ejecutaron en el entorno del proyecto. Consultar docs/testing.md para comandos, alcance y pruebas manuales pendientes.

El renderizador de previews de Studio mostró una limitación de caché de clases durante la revisión. Las pruebas de adaptación en el dispositivo confirmaron una, dos y tres columnas con las clases correspondientes.

## Pendientes que no se resuelven modificando código

- Confirmar nombres completos, número de grupo y tablero Trello.
- Demostrar cámara y galería en físico, guardando capturas reales.
- Completar responsables y evidencias de trabajo colaborativo.
- Confirmar visibilidad y accesos del repositorio con el docente.
- Publicar la versión autorizada antes del cierre y comprobar la entrega grupal/individual en AVA.
- Ensayar y demostrar dominio individual durante la defensa.

No se crearon registros falsos de colaboración, no se enviaron mensajes externos ni se publicaron cambios del repositorio. Estas tareas requieren acciones y evidencia del equipo; no se declara cumplimiento académico total hasta completarlas.
