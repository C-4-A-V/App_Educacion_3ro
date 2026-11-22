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
    "spring.datasource.url=jdbc:h2:mem:aula_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AulaActividadesApiTest {
    @LocalServerPort int puerto;
    @Autowired CursoCompartidoRepository cursos;
    @Autowired ActividadCompletaRepository actividades;
    @Autowired IdentidadCatalogoRepository identidades;
    @Autowired PublicacionActividadService publicador;
    @Autowired RespuestaAulaService respuestas;
    @Autowired IntentoAulaRepository intentos;
    private final HttpClient cliente = HttpClient.newHttpClient();

    @Test
    void retirarVariasConservaIntentosYNoPermiteRepublicar() throws Exception {
        Long curso = cursos.findBySeccion("A").orElseThrow().getId();
        String catalogo = identidades.findById(1L).orElseThrow().getIdentificador();
        String clave = java.util.UUID.randomUUID().toString();
        String pendiente = java.util.UUID.randomUUID().toString();
        var datos = new ActividadCompletaRequest(curso, "ciencias", "Pregunta retirada", "Lee", "¿Qué?",
            "Uno", "Dos", "Tres", "Cuatro", 0, 3, "Explicación", "");
        long id = publicador.publicar(clave, datos).actividadId();
        String token = java.util.UUID.randomUUID().toString();
        respuestas.responder(new RespuestaAulaRequest(id, curso, token, 1, 0));
        long total = intentos.count();
        String json = "{\"cursoId\":" + curso + ",\"claves\":[\"" + clave + "\",\"" + pendiente + "\"]}";
        var solicitud = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + "/api/publicaciones/retirar"))
            .header("Content-Type", "application/json").header("X-Catalogo-Id", catalogo)
            .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        assertEquals(204, cliente.send(solicitud, HttpResponse.BodyHandlers.discarding()).statusCode());
        assertEquals(204, cliente.send(solicitud, HttpResponse.BodyHandlers.discarding()).statusCode());
        assertEquals(total, intentos.count());
        assertTrue(actividades.findById(id).isPresent());
        var lista = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto +
            "/api/aula/actividades?cursoId=" + curso + "&asignaturaId=ciencias"))
            .header("X-Catalogo-Id", catalogo).GET().build();
        assertFalse(cliente.send(lista, HttpResponse.BodyHandlers.ofString()).body().contains("Pregunta retirada"));
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> publicador.publicar(clave, datos));
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> publicador.publicar(pendiente, datos));
    }

    @Test
    void publicaYListaSinExponerRespuestasNiRutaCompleta() throws Exception {
        Long curso = cursos.findBySeccion("A").orElseThrow().getId();
        String datos = """
            {"cursoId":%d,"asignaturaId":"lenguaje","titulo":"Lectura",
             "instrucciones":"Elige una","pregunta":"¿Qué es un cuento?",
             "alternativaA":"Una historia","alternativaB":"Un número",
             "alternativaC":"Una planta","alternativaD":"Una suma",
             "respuestaCorrecta":0,"maxIntentos":3,
             "explicacion":"Un cuento es una historia","pista":"Piensa en narraciones"}
            """.formatted(curso);
        String raiz = "http://127.0.0.1:" + puerto;
        var post = HttpRequest.newBuilder(URI.create(raiz + "/api/publicaciones"))
            .header("Content-Type", "application/json")
            .header("Idempotency-Key", "4abf76c2-347b-4b76-9910-fcb0aff16774")
            .header("X-Catalogo-Id", identidades.findById(1L).orElseThrow().getIdentificador())
            .POST(HttpRequest.BodyPublishers.ofString(datos)).build();
        assertEquals(201, cliente.send(post, HttpResponse.BodyHandlers.ofString()).statusCode());
        var get = HttpRequest.newBuilder(URI.create(raiz + "/api/aula/actividades?cursoId=" + curso + "&asignaturaId=lenguaje"))
            .header("X-Catalogo-Id", identidades.findById(1L).orElseThrow().getIdentificador()).GET().build();
        var respuesta = cliente.send(get, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("Lectura"));
        assertFalse(respuesta.body().contains("respuestaCorrecta"));
        assertFalse(respuesta.body().contains("explicacion"));
        Long actividadId = actividades.findByCursoIdAndAsignaturaIdOrderByIdAsc(curso, "lenguaje").getFirst().getId();
        String catalogo = identidades.findById(1L).orElseThrow().getIdentificador();
        var detalle = HttpRequest.newBuilder(URI.create(raiz + "/api/aula/actividades/" + actividadId + "?cursoId=" + curso))
            .header("X-Catalogo-Id", catalogo).GET().build();
        var detalleIncorrecto = HttpRequest.newBuilder(URI.create(raiz + "/api/aula/actividades/" + actividadId + "?cursoId=" + (curso + 100)))
            .header("X-Catalogo-Id", catalogo).GET().build();
        var detalleRespuesta = cliente.send(detalle, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, detalleRespuesta.statusCode());
        assertFalse(detalleRespuesta.body().contains("respuestaCorrecta"));
        assertFalse(detalleRespuesta.body().contains("explicacion"));
        assertEquals(404, cliente.send(detalleIncorrecto, HttpResponse.BodyHandlers.discarding()).statusCode());
        var completa = HttpRequest.newBuilder(URI.create(raiz + "/api/actividades-completas/1")).GET().build();
        assertEquals(404, cliente.send(completa, HttpResponse.BodyHandlers.discarding()).statusCode());
    }
}
