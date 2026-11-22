package com.example.mpazpro

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mpazpro.data.local.BancoActividades
import com.example.mpazpro.data.local.CursoEntity
import com.example.mpazpro.data.local.DocenteEntity
import com.example.mpazpro.data.local.MpazDatabase
import com.example.mpazpro.data.local.VinculoCursoEntity
import com.example.mpazpro.data.repository.ActividadRepository
import com.example.mpazpro.model.ActividadFormulario
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class PublicacionPendienteTest {
    @Test fun preparaBancoSinDuplicarNiCambiarPreguntas() = runBlocking {
        val base = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, MpazDatabase::class.java
        ).build()
        try {
            val docente = base.docenteDao().insertar(DocenteEntity(
                nombre = "Docente", correo = "banco@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1
            ))
            val curso = base.cursoDao().insertar(CursoEntity(docenteId = docente, nombre = "3 A"))
            base.vinculoCursoDao().insertar(VinculoCursoEntity(curso, "11111111-1111-1111-1111-111111111111", 42))
            val originales = BancoActividades.entidades(curso)
            val ids = originales.map { base.actividadDao().insertar(it) }
            val repo = ActividadRepository(base)
            val yaPublicada = repo.prepararPublicacion(ids.first(), docente)
            base.publicacionPendienteDao().marcarPublicada(ids.first(), 10)
            assertEquals(9, repo.prepararBancoParaPublicacion(curso, docente, "lenguaje"))
            assertEquals(9, base.publicacionPendienteDao().pendientes().size)
            assertEquals(0, repo.prepararBancoParaPublicacion(curso, docente, "lenguaje"))
            assertEquals(30, repo.prepararBancoParaPublicacion(curso, docente))
            assertEquals(0, repo.prepararBancoParaPublicacion(curso, docente))
            assertEquals(39, base.publicacionPendienteDao().pendientes().size)
            assertEquals(yaPublicada.clavePublicacion, base.publicacionPendienteDao().buscar(ids.first())!!.clavePublicacion)
            assertEquals(10L, base.publicacionPendienteDao().buscar(ids.first())!!.actividadServidorId)
            assertEquals(originales[1].copy(id = ids[1]), base.actividadDao().buscarPorId(ids[1]))
        } finally { base.close() }
    }

    @Test fun guardaLaActividadYEncolaSoloSiElCursoEstaVinculado() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val base = Room.inMemoryDatabaseBuilder(contexto, MpazDatabase::class.java).build()
        try {
            val docenteId = base.docenteDao().insertar(DocenteEntity(
                nombre = "Docente", correo = "cola@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1
            ))
            val cursoVinculado = base.cursoDao().insertar(CursoEntity(docenteId = docenteId, nombre = "3 A"))
            val cursoLocal = base.cursoDao().insertar(CursoEntity(docenteId = docenteId, nombre = "3 B"))
            val catalogo = "11111111-1111-1111-1111-111111111111"
            base.vinculoCursoDao().insertar(VinculoCursoEntity(cursoVinculado, catalogo, 42))
            val formulario = ActividadFormulario(
                titulo = "La lectura", instrucciones = "Lee atentamente", pregunta = "¿Qué es un cuento?",
                alternativas = listOf("Una historia", "Un número", "Una planta", "Una suma"),
                respuestaCorrecta = 0, maxIntentos = 3, explicacion = "Un cuento es una historia"
            )
            val repo = ActividadRepository(base)
            val publicadaLuego = repo.crearActividad(cursoVinculado, "lenguaje", docenteId, formulario)
            val soloLocal = repo.crearActividad(cursoLocal, "lenguaje", docenteId, formulario)
            val pendiente = base.publicacionPendienteDao().buscar(publicadaLuego)!!
            assertEquals(catalogo, pendiente.catalogoId)
            assertEquals(42L, pendiente.cursoServidorId)
            assertNotNull(UUID.fromString(pendiente.clavePublicacion))
            assertNull(pendiente.actividadServidorId)
            assertNull(base.publicacionPendienteDao().buscar(soloLocal))
            assertEquals(1, base.publicacionPendienteDao().pendientes().size)

            base.vinculoCursoDao().insertar(VinculoCursoEntity(cursoLocal, catalogo, 43))
            val preparada = repo.prepararPublicacion(soloLocal, docenteId)
            assertEquals(43L, preparada.cursoServidorId)
            assertEquals(preparada.clavePublicacion, repo.prepararPublicacion(soloLocal, docenteId).clavePublicacion)
            val otroDocente = base.docenteDao().insertar(DocenteEntity(
                nombre = "Otro", correo = "otro-cola@example.com", claveHash = "h", claveSalt = "s", claveIteraciones = 1
            ))
            try {
                repo.prepararPublicacion(soloLocal, otroDocente)
                fail("No debe preparar actividades de otra cuenta local")
            } catch (_: IllegalArgumentException) {}
            assertEquals(2, base.publicacionPendienteDao().pendientes().size)
        } finally { base.close() }
    }
}
