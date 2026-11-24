# Preparación de la migración H2 a MySQL

Estado: el 24 de noviembre se copió el respaldo H2, abierto en solo lectura, a un MySQL aislado de ensayo y después al nuevo volumen persistente `mpaz_mysql_aula`. Se comparó cada columna de cada registro antes de confirmar la transacción. Tras reiniciar MySQL y Spring se comprobaron 2 cursos, 89 preguntas activas y 2 intentos; la identidad del catálogo se conservó. El archivo H2 original y su respaldo permanecen disponibles. La app en el emulador aún requiere comprobación visual.

## Revisión local del 24 de noviembre

Se guardó una copia de `backend/datos/catalogo_compartido.mv.db` como `backend/datos/catalogo_respaldo_20261124.mv.db` y se verificó su hash frente al original. La carpeta está excluida de Git. La inspección fue de solo lectura: se encontraron 2 cursos, 89 actividades completas, 89 publicaciones y 2 intentos (conteos estimados de H2), además de tablas antiguas vacías.

La tabla de identidades de estudiante todavía no está en esa versión de H2. El importador acepta su ausencia y deja vacía la tabla nueva del destino. Se ensayó esa situación en una base ficticia con tablas antiguas vacías. Las 33 columnas de las tablas activas coinciden por nombre con el esquema MySQL generado para el ensayo. Posteriormente, la copia de ensayo y la importación persistente compararon cada valor durante la transacción.

## Alcance

H2 contiene el catálogo compartido del servidor. Room contiene los estudiantes, fotos referenciadas, credenciales locales y avances locales; migrar H2 no mueve esos datos ni los sincroniza entre dispositivos.

## Condiciones para migrar

- Detener el servidor que usa el archivo H2 antes de respaldarlo; verificar que terminó y conservar la copia original sin modificar.
- Crear un destino MySQL nuevo y aislado, sin sobrescribir el contenedor ni el catálogo de prueba existentes.
- Copiar todas las tablas presentes, respetando claves foráneas y valores nulos; no omitir tablas desconocidas.
- Conservar exactamente el UUID de identidad_catalogo, los ID de cursos y actividades, las claves de publicación y las claves de publicaciones retiradas.
- Conservar intentos y tokens anónimos. Si existen datos en tablas antiguas retiradas del proyecto, detenerse y revisarlos sin eliminarlos.
- Ajustar la generación de ID para que las siguientes inserciones no reemplacen registros importados.
- Tratar errores de integridad o discrepancias como fallas: no conectar la app al destino hasta resolverlas.

## Ensayo y comprobación

1. Preparar una base H2 ficticia con identidad, cursos, actividades, publicaciones activas y retiradas, e intentos relacionados.
2. Importar la copia ficticia a un MySQL desechable e inicialmente vacío.
3. Comparar tablas y registros, UUID, IDs, texto Unicode, nulos y relaciones; comprobar que las claves de retirada no se reactiven.
4. Reiniciar Spring y MySQL; comprobar consultas y que los nuevos IDs no colisionen.
5. Repetir el ensayo para errores de conexión, destino no vacío, esquema desconocido y reintentos: debe detenerse sin pérdida de datos.

## Cambio de entorno

La migración mantuvo el UUID del catálogo. El servidor de prueba se inicia en el puerto 8083 apuntando al contenedor local `mpaz-mysql-aula`, puerto MySQL 3308. El archivo H2 anterior es una copia del estado anterior a la migración: si se crean datos nuevos en MySQL, volver a H2 no los incluiría. No forzar vínculos ni modificar Room para ocultar discrepancias.
