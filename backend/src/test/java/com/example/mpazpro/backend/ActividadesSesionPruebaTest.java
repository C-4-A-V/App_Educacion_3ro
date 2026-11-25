package com.example.mpazpro.backend;

import static org.junit.jupiter.api.Assertions.*;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("sesion-prueba")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:actividades_sesion_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ActividadesSesionPruebaTest {
    @LocalServerPort int puerto;
    @Autowired CursoCompartidoRepository cursos;
    @Autowired IdentidadEstudianteAulaService identidades;
    @Autowired AccesoEstudianteAulaService acceso;
    @Autowired PublicacionActividadService publicador;

    @Test void consultaSoloLasPreguntasDelCursoDeLaSesion() throws Exception {
        var a = cursos.findBySeccion("A").orElseThrow();
        var b = cursos.findBySeccion("B").orElseGet(() -> cursos.saveAndFlush(new CursoCompartido("B")));
        var identidad = identidades.crear(a.getId());
        acceso.registrar(identidad, "20983458-8", "1234".toCharArray());
        long idA = publicar(a.getId(), "Pregunta A");
        long idB = publicar(b.getId(), "Pregunta B");
        var cliente = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        String raiz = "http://127.0.0.1:" + puerto + "/api/sesion-estudiante-prueba";
        assertEquals(401, get(cliente, raiz + "/actividades?asignaturaId=ciencias").statusCode());
        var login = HttpRequest.newBuilder(URI.create(raiz + "/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"rut\":\"20983458-8\",\"pin\":\"1234\"}")).build();
        assertEquals(200, cliente.send(login, HttpResponse.BodyHandlers.discarding()).statusCode());
        var lista = get(cliente, raiz + "/actividades?asignaturaId=ciencias&cursoId=" + b.getId());
        assertEquals(200, lista.statusCode());
        assertTrue(lista.body().contains("Pregunta A"));
        assertFalse(lista.body().contains("Pregunta B"));
        assertFalse(lista.body().contains("respuestaCorrecta"));
        assertEquals(200, get(cliente, raiz + "/actividades/" + idA).statusCode());
        assertEquals(404, get(cliente, raiz + "/actividades/" + idB).statusCode());
        String propio = "{\"actividadId\":" + idA + ",\"numero\":1,\"respuestaElegida\":0}";
        String ajeno = "{\"actividadId\":" + idB + ",\"numero\":1,\"respuestaElegida\":0}";
        assertEquals(404, post(cliente, raiz + "/respuestas", ajeno).statusCode());
        assertEquals(200, post(cliente, raiz + "/respuestas", propio).statusCode());
        assertEquals(200, post(cliente, raiz + "/respuestas", propio).statusCode());
        assertEquals(200, get(cliente, raiz + "/respuestas/" + idA).statusCode());
        assertEquals(404, get(cliente, raiz + "/respuestas/" + idB).statusCode());
        assertTrue(get(cliente, raiz + "/actividades?asignaturaId=ciencias").body().contains("\"yaRespondida\":true"));
        var segundaIdentidad = identidades.crear(a.getId());
        acceso.registrar(segundaIdentidad, "11111111-1", "4321".toCharArray());
        var segundo = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        assertEquals(200, post(segundo, raiz + "/login", "{\"rut\":\"11111111-1\",\"pin\":\"4321\"}").statusCode());
        assertEquals(204, get(segundo, raiz + "/respuestas/" + idA).statusCode());
        String progresoPrimero = get(cliente, raiz + "/actividades/progreso?asignaturaId=ciencias").body();
        assertTrue(progresoPrimero.contains("\"total\":1"));
        assertTrue(progresoPrimero.contains("\"completadas\":1"));
        assertTrue(progresoPrimero.contains("\"correctas\":1"));
        String progresoSegundo = get(segundo, raiz + "/actividades/progreso?asignaturaId=ciencias").body();
        assertTrue(progresoSegundo.contains("\"total\":1"));
        assertTrue(progresoSegundo.contains("\"completadas\":0"));
        assertTrue(progresoSegundo.contains("\"correctas\":0"));
        assertTrue(get(segundo, raiz + "/actividades?asignaturaId=ciencias").body().contains("\"yaRespondida\":false"));
        assertEquals(200, post(segundo, raiz + "/respuestas", propio).statusCode());
        assertEquals(200, get(cliente, raiz + "/respuestas/" + idA).statusCode());
        post(cliente, raiz + "/logout", "");
        assertEquals(401, post(cliente, raiz + "/respuestas", propio).statusCode());
        assertEquals(401, get(cliente, raiz + "/respuestas/" + idA).statusCode());
    }

    private long publicar(Long curso, String titulo) {
        var datos = new ActividadCompletaRequest(curso, "ciencias", titulo, "Lee", "¿Qué?",
            "Uno", "Dos", "Tres", "Cuatro", 0, 3, "Explicación", "");
        return publicador.publicar(UUID.randomUUID().toString(), datos).actividadId();
    }

    private HttpResponse<String> post(HttpClient cliente, String url, String cuerpo) throws Exception {
        var solicitud = HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(cuerpo)).build();
        return cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(HttpClient cliente, String url) throws Exception {
        return cliente.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
}
