package com.example.mpazpro.ui.viewmodel

import com.example.mpazpro.data.repository.TemaPudu


class BolsaCuriosidades {
    private val pendientes = mutableMapOf<TemaPudu, MutableList<String>>()
    private val ultimoMostrado = mutableMapOf<TemaPudu, String>()

    fun siguiente(tema: TemaPudu, candidatos: List<String>, excluir: Set<String> = emptySet()): String {
        require(candidatos.distinct().size == candidatos.size && candidatos.any { it !in excluir })
        var bolsa = pendientes.getOrPut(tema) { mutableListOf() }
        bolsa.retainAll(candidatos.toSet())
        if (bolsa.none { it !in excluir }) {
            bolsa = candidatos.shuffled().toMutableList()
            pendientes[tema] = bolsa
            val ultimo = ultimoMostrado[tema]
            val elegibles = bolsa.filter { it !in excluir }
            if (elegibles.size > 1 && elegibles.first() == ultimo) {
                bolsa.remove(ultimo)
                if (ultimo != null) bolsa.add(ultimo)
            }
        }
        val indice = bolsa.indexOfFirst { it !in excluir }
        return bolsa.removeAt(indice)
    }

    fun devolver(tema: TemaPudu, id: String) {
        val bolsa = pendientes.getOrPut(tema) { mutableListOf() }
        if (id !in bolsa) bolsa.add(id)
    }

    fun mostrado(tema: TemaPudu, id: String) {
        ultimoMostrado[tema] = id
    }
}
