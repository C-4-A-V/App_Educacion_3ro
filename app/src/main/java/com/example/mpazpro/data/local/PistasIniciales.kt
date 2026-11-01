package com.example.mpazpro.data.local

/** Ayudas originales: proponen una estrategia, sin indicar letras ni copiar la solución. */
object PistasIniciales {
    private val pistas = mapOf(
        "Una visita a la biblioteca" to "Vuelve a leer la parte que viene después de «para». Allí se cuenta el propósito de la visita.",
        "La semilla de Tomás" to "Busca qué ocurrió antes de que apareciera el brote. Puedes ordenar los hechos en tu mente.",
        "Palabras que se parecen" to "Imagina cómo te sientes cuando recibes una buena noticia. Busca una palabra con ese mismo sentimiento.",
        "Palabras contrarias" to "Piensa en cómo cambia la sensación al tocar un hielo y después acercarte a una taza de té tibio.",
        "Una pregunta curiosa" to "Mira los signos que rodean la pregunta que estás leyendo. ¿Cómo comienza y cómo termina?",
        "El nombre de las cosas" to "Separa las palabras que dicen qué hace el animal de la palabra que dice quién es.",
        "Una acción en el bosque" to "Prueba poner «yo puedo» antes de cada palabra. ¿Cuál expresa algo que puedes hacer?",
        "Describimos una flor" to "Busca la palabra que responde: ¿cómo es la flor? No busques lo que hace.",
        "La mochila de Elisa" to "Relaciona las gotas con el uso de un paraguas. ¿En qué situaciones lo llevarías?",
        "Leemos una receta" to "Recuerda dónde has visto una lista de ingredientes y pasos. ¿Qué te permite hacer?",
        "Sumamos lápices" to "Descompón 34 en decenas y unidades. Añade primero las decenas a 125 y luego las unidades.",
        "Libros prestados" to "Parte de 200 y quita primero 70. Después quita los 5 que faltan.",
        "Grupos de semillas" to "Dibuja una bolsa por cada grupo y pon tres puntos en cada bolsa. Luego cuenta los puntos.",
        "Repartimos frutas" to "Dibuja cinco platos y reparte las manzanas de una en una hasta que no queden.",
        "El valor de una cifra" to "Ubica centenas, decenas y unidades de derecha a izquierda. ¿En qué posición está la cifra?",
        "Comparamos números" to "Compara primero las centenas. Si empatan, compara las decenas y después las unidades.",
        "Un patrón numérico" to "Observa cuánto aumenta un número para llegar al siguiente. Repite ese mismo salto.",
        "Partes iguales" to "Para nombrar una fracción, cuenta cuántas partes iguales hay en total y cuántas tomaste.",
        "El borde del jardín" to "Recorre los cuatro lados con tu dedo. En un rectángulo, los lados opuestos miden lo mismo.",
        "Miramos el reloj" to "En un reloj, una vuelta completa del minutero dura una hora. Piensa qué parte de la vuelta son 30 minutos.",
        "La raíz de una planta" to "Piensa qué parte de la planta está en contacto directo con el suelo húmedo.",
        "Lo que necesita una planta" to "Recuerda cómo cuidas una planta en casa. ¿Qué le das y dónde la ubicas?",
        "El viaje del agua" to "Imagina un charco que se va secando al sol. Busca el nombre del cambio, no el del agua cuando se enfría.",
        "El hielo se derrite" to "Piensa si el agua que queda mantiene la forma del cubo o toma la forma del recipiente.",
        "Cuidamos los dientes" to "Busca una costumbre que ayude a retirar los restos de comida después de comer.",
        "Alimentos variados" to "Revisa cada colación y busca un alimento que crezca en un árbol frutal.",
        "El Sol y la luz" to "Distingue entre algo que produce su propia luz y algo que solo refleja la que recibe.",
        "Una sombra en el patio" to "Imagina una linterna apuntando a una pared. ¿Qué pasa si pones tu mano en medio?",
        "Cuidamos al pudú" to "Imagina que visitas la casa de un animal. ¿Qué harías para que siga seguro y tranquilo?",
        "Una Tierra en movimiento" to "Distingue entre girar sobre ti mismo y caminar alrededor de otro objeto. ¿Cuál cambia el lado que mira al Sol?",
        "Nos orientamos" to "Recuerda las letras de una brújula. Busca nombres de direcciones que sirven para orientarnos.",
        "Chile y su continente" to "Ubica Chile en un mapa del mundo. Observa el nombre del gran territorio donde está.",
        "El océano de Chile" to "Busca Chile en un mapa y mira el gran océano que queda a su izquierda.",
        "Leemos un mapa" to "Imagina que un mapa tiene un dibujo que no conoces. ¿Qué información necesitas para interpretarlo?",
        "Una norma del curso" to "Piensa qué acción permite que cada compañero se sienta respetado y pueda participar.",
        "Nuestros derechos" to "Busca algo que todos los niños necesitan para crecer y aprender, sin perjudicar a otras personas.",
        "Participamos en el curso" to "Piensa cómo tomar una decisión en la que todas las personas puedan expresar su opinión.",
        "El pasado en una fotografía" to "Busca una fuente creada en otra época que permita observar cómo era un lugar.",
        "Conocemos a los griegos" to "Piensa en un encuentro deportivo internacional que reúne distintas disciplinas.",
        "La vida en la antigua Roma" to "La palabra «acueducto» contiene una parte relacionada con «acuático». ¿Qué transportaría?"
    )
    fun paraPregunta(titulo: String, asignaturaId: String): String =
        pistas[titulo] ?: general(asignaturaId)

    fun general(asignaturaId: String): String = when (asignaturaId) {
        "matematica" -> "Identifica los datos y lo que te preguntan. Puedes dibujar el problema y revisar cada paso."
        "lenguaje" -> "Lee otra vez con calma y busca las palabras que apoyan tu elección."
        "ciencias" -> "Relaciona la pregunta con algo que hayas observado en la naturaleza o en tu vida diaria."
        else -> "Ubica el lugar o la época de la pregunta y piensa qué evidencia apoya cada alternativa."
    }
}
