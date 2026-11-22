package com.example.mpazpro.data.repository

import androidx.room.withTransaction
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.local.VinculoCursoEntity
import com.example.mpazpro.data.local.CursoEntity
import com.example.mpazpro.data.remote.CursoCompartidoServidor
import com.example.mpazpro.data.remote.ActividadesServidorApi
import com.example.mpazpro.data.remote.AsegurarCursoApi
import java.util.UUID

enum class ResultadoConexionCurso { CONECTADO, REVISION }
enum class ResultadoImportacionCursos { ACTUALIZADO, REVISION }

/** Vincula solo secciones únicas, en el mismo catálogo persistente, sin reemplazar vínculos. */
class VinculacionAutomaticaCursos(
    private val database: MpazDatabase,
    private val catalogoApi: ActividadesServidorApi,
    private val cursosApi: AsegurarCursoApi
) {
    /** Incorpora cursos compartidos sin duplicar secciones ni reemplazar datos locales. */
    suspend fun incorporarCompartidos(docenteId: Long): ResultadoImportacionCursos {
        val inicial = catalogoApi.identidadCatalogo()
        val catalogo = inicial.identificador
        if (!inicial.persistente || catalogo == null || runCatching { UUID.fromString(catalogo).toString() }.getOrNull() != catalogo)
            return ResultadoImportacionCursos.REVISION
        val remotos = catalogoApi.listarCursos()
        val posteriores = catalogoApi.identidadCatalogo()
        if (!posteriores.persistente || posteriores.identificador != catalogo) return ResultadoImportacionCursos.REVISION

        return database.withTransaction {
            val locales = database.cursoDao().listarPorDocente(docenteId)
            val vinculos = database.vinculoCursoDao().listarPorDocente(docenteId)
            val seccionesLocales = locales.map { PropuestaVinculacionCursos.seccion(it.nombre) }
            val seccionesRemotas = remotos.map { PropuestaVinculacionCursos.seccion(it.nombre) }
            if (seccionesLocales.any { it == null } || seccionesLocales.distinct().size != seccionesLocales.size ||
                seccionesRemotas.any { it == null } || seccionesRemotas.distinct().size != seccionesRemotas.size ||
                remotos.any { it.id <= 0 } || remotos.map { it.id }.distinct().size != remotos.size ||
                vinculos.any { it.catalogoId != catalogo } || !vinculosValidos(locales, vinculos, remotos))
                return@withTransaction ResultadoImportacionCursos.REVISION

            remotos.forEach { remoto ->
                val seccion = checkNotNull(PropuestaVinculacionCursos.seccion(remoto.nombre))
                if (seccion !in seccionesLocales) {
                    val id = database.cursoDao().insertar(CursoEntity(docenteId = docenteId, nombre = "3.º Básico $seccion"))
                    check(id > 0)
                    database.vinculoCursoDao().insertar(VinculoCursoEntity(id, catalogo, remoto.id))
                }
            }
            ResultadoImportacionCursos.ACTUALIZADO
        }
    }

    private fun vinculosValidos(
        locales: List<CursoEntity>, vinculos: List<VinculoCursoEntity>, remotos: List<CursoCompartidoServidor>
    ): Boolean = vinculos.all { vinculo ->
        val local = locales.singleOrNull { it.id == vinculo.cursoLocalId }
        val seccion = local?.let { PropuestaVinculacionCursos.seccion(it.nombre) }
        seccion != null && remotos.count {
            it.id == vinculo.cursoServidorId && PropuestaVinculacionCursos.seccion(it.nombre) == seccion
        } == 1
    }

    suspend fun conectar(docenteId: Long, cursoId: Long): ResultadoConexionCurso {
        val locales = database.cursoDao().listarPorDocente(docenteId)
        val curso = locales.singleOrNull { it.id == cursoId } ?: return ResultadoConexionCurso.REVISION
        val seccion = PropuestaVinculacionCursos.seccion(curso.nombre) ?: return ResultadoConexionCurso.REVISION
        if (locales.count { PropuestaVinculacionCursos.seccion(it.nombre) == seccion } != 1)
            return ResultadoConexionCurso.REVISION

        val inicial = catalogoApi.identidadCatalogo()
        val catalogo = inicial.identificador
        if (!inicial.persistente || catalogo == null || runCatching { UUID.fromString(catalogo).toString() }.getOrNull() != catalogo)
            return ResultadoConexionCurso.REVISION
        val vinculos = database.vinculoCursoDao().listarPorDocente(docenteId)
        if (vinculos.any { it.catalogoId != catalogo }) return ResultadoConexionCurso.REVISION
        val previo = vinculos.singleOrNull { it.cursoLocalId == cursoId }
        val remotos = catalogoApi.listarCursos()
        // No se amplía un catálogo si algún vínculo existente apunta a otra sección o ya no existe.
        if (!vinculosValidos(locales, vinculos, remotos)) return ResultadoConexionCurso.REVISION
        val coincidencias = remotos.filter { PropuestaVinculacionCursos.seccion(it.nombre) == seccion }
        if (coincidencias.size > 1) return ResultadoConexionCurso.REVISION
        if (previo != null && (previo.catalogoId != catalogo || coincidencias.singleOrNull()?.id != previo.cursoServidorId))
            return ResultadoConexionCurso.REVISION
        val remoto = coincidencias.singleOrNull() ?: cursosApi.asegurar(seccion.toString(), catalogo)
        if (remoto.id <= 0 || PropuestaVinculacionCursos.seccion(remoto.nombre) != seccion)
            return ResultadoConexionCurso.REVISION
        val posterior = catalogoApi.identidadCatalogo()
        if (!posterior.persistente || posterior.identificador != catalogo) return ResultadoConexionCurso.REVISION

        return database.withTransaction {
            val actual = database.cursoDao().listarPorDocente(docenteId)
            val cursoActual = actual.singleOrNull { it.id == cursoId }
            if (cursoActual == null || PropuestaVinculacionCursos.seccion(cursoActual.nombre) != seccion ||
                actual.count { PropuestaVinculacionCursos.seccion(it.nombre) == seccion } != 1)
                return@withTransaction ResultadoConexionCurso.REVISION
            val vinculo = database.vinculoCursoDao().buscar(cursoId)
            val nuevo = VinculoCursoEntity(cursoId, catalogo, remoto.id)
            if (vinculo != null && vinculo != nuevo) return@withTransaction ResultadoConexionCurso.REVISION
            if (vinculo == null) database.vinculoCursoDao().insertar(nuevo)
            ResultadoConexionCurso.CONECTADO
        }
    }
}
