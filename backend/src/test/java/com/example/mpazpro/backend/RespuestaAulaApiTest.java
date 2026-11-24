package com.example.mpazpro.backend;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("aula-local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:respuestas_aula_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RespuestaAulaApiTest {
    @LocalServerPort int puerto;
    @Autowired CursoCompartidoRepository cursos;
    @Autowired ActividadCompletaRepository actividades;
    @Autowired IntentoAulaRepository intentos;
    @Autowired PublicacionActividadRepository publicaciones;
    @Autowired IdentidadCatalogoRepository identidad;
    private final HttpClient cliente = HttpClient.newHttpClient();

    @Test
    void noPermiteResponderNiConsultarUnaActividadSinPublicar() throws Exception {
        Long curso = cursos.findBySeccion("A").orElseThrow().getId();
        var actividad = actividades.save(new ActividadCompleta(new ActividadCompletaRequest(
            curso, "lenguaje", "Borrador no publicado", "Lee", "Pregunta",
            "Uno", "Dos", "Tres", "Cuatro", 0, 3, "Explicación", "")));
        String token = "11111111-1111-1111-1111-111111111111";
        assertEquals(404, estado(curso, actividad.getId(), token).statusCode());
        assertEquals(404, responder(curso, actividad.getId(), token, 1, 0).statusCode());
        String raiz = "http://127.0.0.1:" + puerto + "/api/aula/actividades";
        String catalogo = identidad.findById(1L).orElseThrow().getIdentificador();
        var lista = HttpRequest.newBuilder(URI.create(raiz + "?cursoId=" + curso + "&asignaturaId=lenguaje"))
            .header("X-Catalogo-Id", catalogo).GET().build();
        assertFalse(cliente.send(lista, HttpResponse.BodyHandlers.ofString()).body().contains("Borrador no publicado"));
        var detalle = HttpRequest.newBuilder(URI.create(raiz + "/" + actividad.getId() + "?cursoId=" + curso))
            .header("X-Catalogo-Id", catalogo).GET().build();
        assertEquals(404, cliente.send(detalle, HttpResponse.BodyHandlers.discarding()).statusCode());
    }

    @Test
    void controlaIntentosRepetidosYSeparaEstudiantes() throws Exception {
        Long curso = cursos.findBySeccion("A").orElseThrow().getId();
        var actividad = actividades.save(new ActividadCompleta(new ActividadCompletaRequest(
            curso, "lenguaje", "Lectura", "Lee", "¿Qué es un cuento?",
            "Historia", "Número", "Planta", "Suma", 0, 3, "Es una historia", "Piensa en un relato")));
        publicaciones.save(new PublicacionActividad(java.util.UUID.randomUUID().toString(), actividad));
        long antes = intentos.count();
        String catalogo = identidad.findById(1L).orElseThrow().getIdentificador();
        var detalle = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto +
            "/api/aula/actividades/" + actividad.getId() + "?cursoId=" + curso))
            .header("X-Catalogo-Id", catalogo).GET().build();
        assertTrue(cliente.send(detalle, HttpResponse.BodyHandlers.ofString()).body().contains("\"yaRespondida\":false"));
        String token = "11111111-1111-1111-1111-111111111111";
        assertEquals(204, estado(curso, actividad.getId(), token).statusCode());
        var primero = responder(curso, actividad.getId(), token, 1, 1);
        assertEquals(200, primero.statusCode());
        assertTrue(primero.body().contains("\"correcta\":false"));
        assertTrue(cliente.send(detalle, HttpResponse.BodyHandlers.ofString()).body().contains("\"yaRespondida\":true"));
        assertTrue(primero.body().contains("\"finalizada\":false"));
        assertFalse(primero.body().contains("Es una historia"));
        var recuperado = estado(curso, actividad.getId(), token);
        assertEquals(200, recuperado.statusCode());
        assertTrue(recuperado.body().contains("\"numero\":1"));
        assertTrue(recuperado.body().contains("\"respuestaElegida\":1"));
        assertFalse(primero.body().contains("\"respuestaCorrecta\":0"));

        assertEquals(200, responder(curso, actividad.getId(), token, 1, 1).statusCode());
        assertEquals(antes + 1, intentos.count());
        assertEquals(409, responder(curso, actividad.getId(), token, 1, 2).statusCode());
        assertEquals(409, responder(curso, actividad.getId(), token, 3, 0).statusCode());
        assertEquals(404, responder(curso + 999, actividad.getId(), token, 2, 0).statusCode());
        assertEquals(200, responder(curso, actividad.getId(), token, 2, 2).statusCode());
        var finalizado = responder(curso, actividad.getId(), token, 3, 0);
        assertEquals(200, finalizado.statusCode());
        assertTrue(finalizado.body().contains("\"correcta\":true"));
        assertTrue(finalizado.body().contains("\"finalizada\":true"));
        assertTrue(finalizado.body().contains("Es una historia"));
        assertEquals(200, responder(curso, actividad.getId(), token, 3, 0).statusCode());
        assertEquals(antes + 3, intentos.count());
        assertTrue(estado(curso, actividad.getId(), token).body().contains("\"numero\":3"));

        String otro = "22222222-2222-2222-2222-222222222222";
        assertEquals(200, responder(curso, actividad.getId(), otro, 1, 0).statusCode());
        assertEquals(antes + 4, intentos.count());
    }

    @Test
    void evaluacionDeUnIntentoFinalizaAlResponder() throws Exception {
        Long curso = cursos.findBySeccion("A").orElseThrow().getId();
        var actividad = actividades.save(new ActividadCompleta(new ActividadCompletaRequest(
            curso, "ciencias", "Plantas", "Lee", "¿Qué necesitan?",
            "Agua", "Metal", "Vidrio", "Papel", 0, 1, "Necesitan agua", "")));
        publicaciones.save(new PublicacionActividad(java.util.UUID.randomUUID().toString(), actividad));
        var respuesta = responder(curso, actividad.getId(), "33333333-3333-3333-3333-333333333333", 1, 2);
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"finalizada\":true"));
        assertTrue(respuesta.body().contains("Necesitan agua"));
        assertEquals(409, responder(curso, actividad.getId(), "33333333-3333-3333-3333-333333333333", 2, 0).statusCode());
    }

    @Test
    void dosReintentosSimultaneosRegistranUnSoloIntento() throws Exception {
        Long curso = cursos.findBySeccion("A").orElseThrow().getId();
        var actividad = actividades.save(new ActividadCompleta(new ActividadCompletaRequest(
            curso, "matematica", "Sumas", "Lee", "¿Cuánto es dos más dos?",
            "Cuatro", "Tres", "Dos", "Uno", 0, 3, "Dos más dos es cuatro", "")));
        String token = "44444444-4444-4444-4444-444444444444";
        publicaciones.save(new PublicacionActividad(java.util.UUID.randomUUID().toString(), actividad));
        long antes = intentos.count();
        var inicio = new java.util.concurrent.CountDownLatch(1);
        try (var ejecutor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var primero = ejecutor.submit(() -> { inicio.await(); return responder(curso, actividad.getId(), token, 1, 1); });
            var segundo = ejecutor.submit(() -> { inicio.await(); return responder(curso, actividad.getId(), token, 1, 1); });
            inicio.countDown();
            assertEquals(200, primero.get().statusCode());
            assertEquals(200, segundo.get().statusCode());
        }
        assertEquals(antes + 1, intentos.count());
    }

    private HttpResponse<String> estado(Long curso, Long actividad, String token) throws Exception {
        String datos = """
            {"cursoId":%d,"actividadId":%d,"estudianteToken":"%s"}
            """.formatted(curso, actividad, token);
        var solicitud = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + "/api/aula/respuestas/estado"))
            .header("Content-Type", "application/json")
            .header("X-Catalogo-Id", identidad.findById(1L).orElseThrow().getIdentificador())
            .POST(HttpRequest.BodyPublishers.ofString(datos)).build();
        return cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> responder(Long curso, Long actividad, String token, int numero, int respuesta) throws Exception {
        String datos = """
            {"cursoId":%d,"actividadId":%d,"estudianteToken":"%s","numero":%d,"respuestaElegida":%d}
            """.formatted(curso, actividad, token, numero, respuesta);
        var solicitud = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + "/api/aula/respuestas"))
            .header("Content-Type", "application/json")
            .header("X-Catalogo-Id", identidad.findById(1L).orElseThrow().getIdentificador())
            .POST(HttpRequest.BodyPublishers.ofString(datos)).build();
        return cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
    }
}
