package com.example.mpazpro.backend;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:vincular_intentos_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class VinculacionIntentosAnterioresTest {
    @Autowired CursoCompartidoRepository cursos;
    @Autowired IdentidadEstudianteAulaService identidades;
    @Autowired PublicacionActividadService publicaciones;
    @Autowired RespuestaAulaService respuestas;
    @Autowired VinculacionIntentosAnterioresService vinculacion;
    @Autowired IntentoAulaRepository intentos;

    @Test void conservaIntentoYRechazaMezclarCursosODestinosConDatos() {
        Long a = cursos.findBySeccion("A").orElseThrow().getId();
        Long b = cursos.findBySeccion("B").orElseGet(() -> cursos.saveAndFlush(new CursoCompartido("B"))).getId();
        UUID destino = identidades.crear(a);
        UUID anterior = UUID.randomUUID();
        long actividadA = publicar(a);
        respuestas.responder(new RespuestaAulaRequest(actividadA, a, anterior.toString(), 1, 0));
        assertEquals(1, vinculacion.vincular(destino, anterior));
        assertTrue(intentos.existsByEstudianteTokenAndActividadId(destino.toString(), actividadA));
        assertFalse(intentos.existsByEstudianteToken(anterior.toString()));
        assertTrue(respuestas.estado(new EstadoRespuestaAulaRequest(actividadA, a, destino.toString())).orElseThrow().correcta());

        UUID otro = UUID.randomUUID();
        long actividadB = publicar(b);
        respuestas.responder(new RespuestaAulaRequest(actividadB, b, otro.toString(), 1, 0));
        assertThrows(IllegalStateException.class, () -> vinculacion.vincular(destino, otro));
        assertTrue(intentos.existsByEstudianteTokenAndActividadId(otro.toString(), actividadB));

        UUID tercero = UUID.randomUUID();
        respuestas.responder(new RespuestaAulaRequest(actividadA, a, tercero.toString(), 1, 0));
        assertThrows(IllegalStateException.class, () -> vinculacion.vincular(destino, tercero));
        assertTrue(intentos.existsByEstudianteTokenAndActividadId(tercero.toString(), actividadA));
    }

    private long publicar(Long curso) {
        return publicaciones.publicar(UUID.randomUUID().toString(), new ActividadCompletaRequest(
            curso, "ciencias", "Pregunta", "Lee", "¿Qué?", "Uno", "Dos", "Tres", "Cuatro",
            0, 3, "Explicación", "")).actividadId();
    }
}
