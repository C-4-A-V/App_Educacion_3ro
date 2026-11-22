package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.remote.ActividadRemotaEstudiante
import com.example.mpazpro.data.remote.ActividadesServidorApi
import com.example.mpazpro.data.remote.AulaActividadesApi
import com.example.mpazpro.model.Asignatura

/** Consulta sin modificar actividades ni copiar respuestas correctas al dispositivo. */
class ConsultaAulaDocente(
    private val database: MpazDatabase,
    private val catalogo: ActividadesServidorApi,
    private val aula: AulaActividadesApi
) {
    suspend fun listar(cursoId: Long, asignaturaId: String, docenteId: Long): List<ActividadRemotaEstudiante>? {
        require(Asignatura.buscarPorId(asignaturaId) != null)
        requireNotNull(database.cursoDao().buscarPorIdYDocente(cursoId, docenteId))
        val vinculo = database.vinculoCursoDao().buscar(cursoId) ?: return null
        val identidad = catalogo.identidadCatalogo()
        check(identidad.persistente && identidad.identificador == vinculo.catalogoId) { "Catálogo diferente" }
        val lista = aula.listar(vinculo.catalogoId, vinculo.cursoServidorId, asignaturaId)
        check(lista.map { it.id }.distinct().size == lista.size &&
            lista.all { it.id > 0 && it.asignaturaId == asignaturaId && it.titulo.isNotBlank() &&
                it.pregunta.isNotBlank() && it.instrucciones.isNotBlank() && it.maxIntentos in listOf(1, 3) &&
                listOf(it.alternativaA, it.alternativaB, it.alternativaC, it.alternativaD).all(String::isNotBlank) &&
                (it.maxIntentos == 3 || it.pista.isEmpty()) }) { "Lista inválida" }
        return lista
    }
}
