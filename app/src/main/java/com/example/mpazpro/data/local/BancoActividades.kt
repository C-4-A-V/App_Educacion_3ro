package com.example.mpazpro.data.local

import com.example.mpazpro.model.Asignatura

/** Contenido original de práctica, orientado a 3.º básico; revisable por el docente. */
object BancoActividades {
    data class Pregunta(
        val asignatura: Asignatura, val titulo: String, val enunciado: String,
        val opciones: List<String>, val correcta: Int, val explicacion: String
    )
    val preguntas: List<Pregunta> = listOf(
        Pregunta(Asignatura.LENGUAJE, "Una visita a la biblioteca", "Ana fue a la biblioteca para pedir un cuento. ¿Para qué fue Ana a la biblioteca?", listOf("Para comprar pan", "Para pedir un cuento", "Para jugar fútbol", "Para tomar un bus"), 1, "El texto dice que Ana fue a pedir un cuento."),
        Pregunta(Asignatura.LENGUAJE, "La semilla de Tomás", "Tomás regó su semilla cada mañana. Después de varios días apareció un brote. ¿Qué hizo Tomás primero?", listOf("Vio un árbol grande", "Cortó una flor", "Regó su semilla", "Recogió una fruta"), 2, "Primero regó la semilla; después apareció el brote."),
        Pregunta(Asignatura.LENGUAJE, "Palabras que se parecen", "¿Qué palabra significa algo parecido a alegre?", listOf("Contento", "Triste", "Cansado", "Enojado"), 0, "Alegre y contento tienen un significado parecido: son sinónimos."),
        Pregunta(Asignatura.LENGUAJE, "Palabras contrarias", "¿Cuál es el contrario de la palabra frío?", listOf("Helado", "Pequeño", "Lento", "Caliente"), 3, "Frío y caliente expresan ideas contrarias: son antónimos."),
        Pregunta(Asignatura.LENGUAJE, "Una pregunta curiosa", "¿Qué signos usamos al escribir una pregunta en español?", listOf("¡ !", "¿ ?", "( )", "« »"), 1, "Las preguntas se escriben con un signo de apertura ¿ y uno de cierre ?."),
        Pregunta(Asignatura.LENGUAJE, "El nombre de las cosas", "En la oración 'El pudú camina despacio', ¿qué palabra nombra al animal?", listOf("El", "Camina", "Pudú", "Despacio"), 2, "Pudú es un sustantivo: nombra al animal."),
        Pregunta(Asignatura.LENGUAJE, "Una acción en el bosque", "¿Qué palabra indica una acción?", listOf("Saltar", "Verde", "Mesa", "Pequeño"), 0, "Saltar es un verbo porque expresa una acción."),
        Pregunta(Asignatura.LENGUAJE, "Describimos una flor", "¿Qué palabra describe a la flor en 'La flor amarilla crece'?", listOf("La", "Flor", "Crece", "Amarilla"), 3, "Amarilla es un adjetivo que describe el color de la flor."),
        Pregunta(Asignatura.LENGUAJE, "La mochila de Elisa", "Elisa guardó un paraguas porque el cielo estaba oscuro y comenzaban a caer gotas. ¿Qué esperaba Elisa?", listOf("Un día de mucho calor", "Que lloviera", "Una nevada en su pieza", "Que saliera un arcoíris de noche"), 1, "Las gotas y el paraguas permiten inferir que esperaba lluvia."),
        Pregunta(Asignatura.LENGUAJE, "Leemos una receta", "¿Para qué sirve principalmente una receta de cocina?", listOf("Contar un sueño", "Dar noticias del país", "Indicar cómo preparar una comida", "Mostrar un mapa"), 2, "Una receta da ingredientes y pasos para preparar una comida."),

        Pregunta(Asignatura.MATEMATICA, "Sumamos lápices", "En una caja hay 125 lápices y en otra hay 34. ¿Cuántos lápices hay en total?", listOf("159", "149", "169", "151"), 0, "125 + 34 = 159: sumamos 30 y después 4."),
        Pregunta(Asignatura.MATEMATICA, "Libros prestados", "Había 200 libros y se prestaron 75. ¿Cuántos quedaron?", listOf("175", "135", "115", "125"), 3, "200 - 75 = 125 libros."),
        Pregunta(Asignatura.MATEMATICA, "Grupos de semillas", "Hay 4 bolsas con 3 semillas en cada una. ¿Cuántas semillas hay?", listOf("7", "12", "9", "16"), 1, "4 grupos de 3 son 3 + 3 + 3 + 3 = 12."),
        Pregunta(Asignatura.MATEMATICA, "Repartimos frutas", "Repartimos 20 manzanas en partes iguales entre 5 niños. ¿Cuántas recibe cada uno?", listOf("5", "3", "4", "6"), 2, "20 dividido en 5 grupos iguales da 4 manzanas por niño."),
        Pregunta(Asignatura.MATEMATICA, "El valor de una cifra", "¿Qué valor tiene el 6 en el número 364?", listOf("60", "6", "600", "16"), 0, "El 6 está en el lugar de las decenas: vale 60."),
        Pregunta(Asignatura.MATEMATICA, "Comparamos números", "¿Cuál de estos números es el mayor?", listOf("398", "409", "390", "490"), 3, "490 tiene 4 centenas y 9 decenas; es mayor que los otros números."),
        Pregunta(Asignatura.MATEMATICA, "Un patrón numérico", "¿Qué número sigue en 5, 10, 15, 20, ...?", listOf("21", "25", "30", "24"), 1, "El patrón suma 5 cada vez: 20 + 5 = 25."),
        Pregunta(Asignatura.MATEMATICA, "Partes iguales", "Una tortilla se divide en 4 partes iguales. Si comes una parte, ¿qué fracción comiste?", listOf("Un medio", "Tres cuartos", "Un cuarto", "Un entero"), 2, "Una de cuatro partes iguales representa un cuarto."),
        Pregunta(Asignatura.MATEMATICA, "El borde del jardín", "Un jardín rectangular mide 5 metros de largo y 3 de ancho. ¿Cuánto mide todo su borde?", listOf("16 metros", "8 metros", "15 metros", "10 metros"), 0, "El perímetro suma sus cuatro lados: 5 + 3 + 5 + 3 = 16."),
        Pregunta(Asignatura.MATEMATICA, "Miramos el reloj", "Han pasado 30 minutos desde las 3:00. ¿Qué hora es?", listOf("4:00", "2:30", "3:15", "3:30"), 3, "Treinta minutos después de las tres son las tres y media."),

        Pregunta(Asignatura.CIENCIAS, "La raíz de una planta", "¿Qué parte de la planta absorbe agua del suelo?", listOf("La flor", "La raíz", "El fruto", "La semilla seca"), 1, "Las raíces absorben agua y ayudan a sujetar la planta al suelo."),
        Pregunta(Asignatura.CIENCIAS, "Lo que necesita una planta", "¿Qué necesita una planta para crecer?", listOf("Solo piedras", "Solo oscuridad", "Agua, aire y luz adecuada", "Solo juguetes"), 2, "Las plantas necesitan agua, aire y luz, además de condiciones adecuadas para crecer."),
        Pregunta(Asignatura.CIENCIAS, "El viaje del agua", "¿Cómo se llama el cambio de agua líquida a vapor?", listOf("Evaporación", "Congelación", "Fusión", "Solidificación"), 0, "En la evaporación, el agua líquida pasa al estado gaseoso."),
        Pregunta(Asignatura.CIENCIAS, "El hielo se derrite", "Un cubo de hielo se derrite. ¿A qué estado pasa?", listOf("Gaseoso", "Sólido", "No cambia", "Líquido"), 3, "Al derretirse, el hielo pasa de sólido a líquido."),
        Pregunta(Asignatura.CIENCIAS, "Cuidamos los dientes", "¿Qué hábito ayuda a cuidar nuestros dientes?", listOf("No lavarlos nunca", "Cepillarlos diariamente", "Comer solo dulces", "Usar los dientes para abrir botellas"), 1, "El cepillado diario ayuda a retirar restos de comida y cuidar los dientes."),
        Pregunta(Asignatura.CIENCIAS, "Alimentos variados", "¿Qué colación incluye una fruta?", listOf("Solo caramelos", "Solo bebida", "Una manzana y agua", "Solo galletas rellenas"), 2, "La manzana es una fruta y el agua ayuda a mantenernos hidratados."),
        Pregunta(Asignatura.CIENCIAS, "El Sol y la luz", "¿Cuál es una fuente natural de luz?", listOf("El Sol", "Un espejo", "Una mesa", "Un cuaderno"), 0, "El Sol produce luz propia. Un espejo refleja la luz que recibe."),
        Pregunta(Asignatura.CIENCIAS, "Una sombra en el patio", "¿Cuándo se forma una sombra?", listOf("Cuando no hay ningún objeto", "Cuando todo es transparente", "Cuando no existe luz", "Cuando un objeto bloquea la luz"), 3, "Una sombra aparece cuando un objeto impide que la luz llegue a una superficie."),
        Pregunta(Asignatura.CIENCIAS, "Cuidamos al pudú", "¿Qué acción ayuda a proteger a los animales del bosque?", listOf("Dejar basura", "Respetar su hábitat", "Asustarlos", "Alimentarlos con dulces"), 1, "Respetar su hábitat y no dejar basura ayuda a proteger a los animales."),
        Pregunta(Asignatura.CIENCIAS, "Una Tierra en movimiento", "¿Qué movimiento de la Tierra explica el día y la noche?", listOf("La lluvia", "La traslación por sí sola", "La rotación", "El viento"), 2, "La Tierra gira sobre su eje. Ese movimiento se llama rotación y origina el día y la noche."),

        Pregunta(Asignatura.HISTORIA, "Nos orientamos", "¿Cuáles son los cuatro puntos cardinales?", listOf("Norte, sur, este y oeste", "Arriba, abajo, cerca y lejos", "Lunes, martes, miércoles y jueves", "Rojo, azul, verde y amarillo"), 0, "Los puntos cardinales permiten orientarnos: norte, sur, este y oeste."),
        Pregunta(Asignatura.HISTORIA, "Chile y su continente", "¿En qué continente se encuentra Chile continental americano?", listOf("Europa", "África", "Asia", "América"), 3, "Chile continental americano se ubica en América del Sur."),
        Pregunta(Asignatura.HISTORIA, "El océano de Chile", "¿Qué océano está al oeste de Chile continental americano?", listOf("Atlántico", "Pacífico", "Índico", "Ártico"), 1, "El océano Pacífico se encuentra al oeste de Chile continental americano."),
        Pregunta(Asignatura.HISTORIA, "Leemos un mapa", "¿Para qué sirve la leyenda de un mapa?", listOf("Para dar la hora", "Para contar chistes", "Para explicar sus símbolos", "Para cambiar el clima"), 2, "La leyenda explica qué representan los símbolos y colores del mapa."),
        Pregunta(Asignatura.HISTORIA, "Una norma del curso", "¿Qué acción ayuda a convivir respetuosamente?", listOf("Escuchar cuando otra persona habla", "Burlarse de una respuesta", "Empujar en la fila", "Romper los materiales"), 0, "Escuchar y respetar turnos ayuda a que todas las personas participen."),
        Pregunta(Asignatura.HISTORIA, "Nuestros derechos", "¿Cuál es un derecho de los niños y niñas?", listOf("Dañar las cosas de otros", "No respetar a nadie", "Impedir que otros aprendan", "Recibir educación"), 3, "Todos los niños y niñas tienen derecho a recibir educación."),
        Pregunta(Asignatura.HISTORIA, "Participamos en el curso", "¿Cómo podemos elegir una actividad entre varias propuestas?", listOf("Sin escuchar a nadie", "Conversando y votando con respeto", "Gritando más fuerte", "Escondiendo las propuestas"), 1, "Conversar y votar respetando los acuerdos permite participar de una decisión."),
        Pregunta(Asignatura.HISTORIA, "El pasado en una fotografía", "¿Qué nos puede ayudar a conocer cómo era nuestra comunidad antes?", listOf("Una predicción del tiempo", "Un videojuego inventado", "Una fotografía antigua del lugar", "Una hoja en blanco"), 2, "Las fotografías antiguas son fuentes que entregan información sobre el pasado."),
        Pregunta(Asignatura.HISTORIA, "Conocemos a los griegos", "¿Qué actividad actual tiene su origen en los juegos de la antigua Grecia?", listOf("Los Juegos Olímpicos", "El uso del teléfono móvil", "Los viajes en avión", "La televisión"), 0, "Los Juegos Olímpicos modernos se inspiraron en los juegos de la antigua Grecia."),
        Pregunta(Asignatura.HISTORIA, "La vida en la antigua Roma", "¿Para qué servían los acueductos romanos?", listOf("Para lanzar cohetes", "Para fabricar celulares", "Para guardar bicicletas", "Para transportar agua"), 3, "Los acueductos conducían agua hacia las ciudades y otros lugares.")
    )

    fun entidades(cursoId: Long): List<ActividadEntity> = preguntas.map {
        ActividadEntity(
            cursoId = cursoId, asignaturaId = it.asignatura.id,
            titulo = it.titulo, instrucciones = "Lee con calma y elige una alternativa.",
            pregunta = it.enunciado,
            alternativaA = it.opciones[0], alternativaB = it.opciones[1],
            alternativaC = it.opciones[2], alternativaD = it.opciones[3],
            respuestaCorrecta = it.correcta, maxIntentos = 3, explicacion = it.explicacion
        )
    }
}
