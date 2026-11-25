package com.example.mpazpro.backend;

import static org.junit.jupiter.api.Assertions.*;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"aula-autenticada", "aula-local"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:docentes_api_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DocenteAutenticadoApiTest {
    @LocalServerPort int puerto;
    @org.springframework.beans.factory.annotation.Autowired AsignacionCursoDocenteService asignaciones;

    @Test void accesoCursosPublicacionYResultadosAislados() throws Exception {
        var a = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        var b = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        String raiz = "http://127.0.0.1:" + puerto;
        String credA = "{\"correo\":\"a@ejemplo.cl\",\"clave\":\"clave-segura-a\"}";
        String credB = "{\"correo\":\"b@ejemplo.cl\",\"clave\":\"clave-segura-b\"}";
        assertEquals(401, enviar(a, raiz + "/api/docentes/cursos", null, null).statusCode());
        assertEquals(404, enviar(a, raiz + "/api/cursos-compartidos", null, null).statusCode());
        assertEquals(404, enviar(a, raiz + "/api/publicaciones", "{}", UUID.randomUUID().toString()).statusCode());
        assertEquals(201, enviar(a, raiz + "/api/docentes/sesion/registro", credA, null).statusCode());
        assertEquals(201, enviar(b, raiz + "/api/docentes/sesion/registro", credB, null).statusCode());
        assertEquals(409, enviar(a, raiz + "/api/docentes/sesion/registro", credA, null).statusCode());
        assertEquals(401, enviar(a, raiz + "/api/docentes/sesion/login",
            "{\"correo\":\"a@ejemplo.cl\",\"clave\":\"clave-incorrecta\"}", null).statusCode());
        var ingresoA = enviar(a, raiz + "/api/docentes/sesion/login", credA, null);
        var ingresoB = enviar(b, raiz + "/api/docentes/sesion/login", credB, null);
        assertEquals(200, ingresoA.statusCode());
        assertEquals(200, ingresoB.statusCode());
        var lector = new tools.jackson.databind.ObjectMapper();
        long idA = lector.readTree(ingresoA.body()).get("id").asLong();
        long idB = lector.readTree(ingresoB.body()).get("id").asLong();
        assertTrue(idA > 0);
        assertNotEquals(idA, idB);
        var sesionA = enviar(a, raiz + "/api/docentes/sesion", null, null);
        assertEquals(200, sesionA.statusCode());
        assertEquals(idA, lector.readTree(sesionA.body()).get("id").asLong());
        var nuevo = enviar(a, raiz + "/api/docentes/cursos", "{\"seccion\":\"Z\"}", null);
        assertEquals(201, nuevo.statusCode());
        var json = new tools.jackson.databind.ObjectMapper().readTree(nuevo.body());
        long curso = json.get("id").asLong();
        assertTrue(enviar(a, raiz + "/api/docentes/cursos", null, null).body().contains("3° Z"));
        assertFalse(enviar(b, raiz + "/api/docentes/cursos", null, null).body().contains("3° Z"));
        assertEquals(409, enviar(b, raiz + "/api/docentes/cursos", "{\"seccion\":\"A\"}", null).statusCode());
        asignaciones.asignarExistente("b@ejemplo.cl", "A");
        asignaciones.asignarExistente("b@ejemplo.cl", "A");
        assertTrue(enviar(b, raiz + "/api/docentes/cursos", null, null).body().contains("3° A"));
        String actividad = "{\"cursoId\":" + curso + ",\"asignaturaId\":\"ciencias\",\"titulo\":\"Pregunta\","
            + "\"instrucciones\":\"Lee\",\"pregunta\":\"¿Qué?\",\"alternativaA\":\"Uno\",\"alternativaB\":\"Dos\","
            + "\"alternativaC\":\"Tres\",\"alternativaD\":\"Cuatro\",\"respuestaCorrecta\":0,"
            + "\"maxIntentos\":3,\"explicacion\":\"Porque\",\"pista\":\"\"}";
        String ruta = raiz + "/api/docentes/cursos/" + curso;
        String clave = UUID.randomUUID().toString();
        assertEquals(404, enviar(b, ruta + "/publicaciones", actividad, clave).statusCode());
        var creada = enviar(a, ruta + "/publicaciones", actividad, clave);
        assertEquals(201, creada.statusCode());
        long actividadId = new tools.jackson.databind.ObjectMapper().readTree(creada.body()).get("actividadId").asLong();
        assertEquals(200, enviar(a, ruta + "/publicaciones", actividad, clave).statusCode());
        assertEquals(404, enviar(b, ruta + "/actividades/" + actividadId + "/resultados", null, null).statusCode());
        assertEquals(200, enviar(a, ruta + "/actividades/" + actividadId + "/resultados", null, null).statusCode());
        assertEquals(204, enviar(a, raiz + "/api/docentes/sesion/logout", "", null).statusCode());
        assertEquals(401, enviar(a, raiz + "/api/docentes/sesion", null, null).statusCode());
        assertEquals(401, enviar(a, ruta + "/actividades/" + actividadId + "/resultados", null, null).statusCode());
        assertEquals(401, enviar(a, ruta + "/publicaciones", actividad, clave).statusCode());
    }

    @Test void creacionIdempotenteAisladaYConcurrente() throws Exception {
        var a = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        var b = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        String raiz = "http://127.0.0.1:" + puerto;
        String sufijo = UUID.randomUUID().toString();
        String credA = "{\"correo\":\"idem-a-" + sufijo + "@ejemplo.cl\",\"clave\":\"clave-segura-a\"}";
        String credB = "{\"correo\":\"idem-b-" + sufijo + "@ejemplo.cl\",\"clave\":\"clave-segura-b\"}";
        String ruta = raiz + "/api/docentes/cursos";
        String clave = UUID.randomUUID().toString();
        String cuerpo = "{\"seccion\":\"W\"}";
        assertEquals(401, enviar(a, ruta, cuerpo, clave).statusCode());
        assertEquals(201, enviar(a, raiz + "/api/docentes/sesion/registro", credA, null).statusCode());
        assertEquals(201, enviar(b, raiz + "/api/docentes/sesion/registro", credB, null).statusCode());
        assertEquals(200, enviar(a, raiz + "/api/docentes/sesion/login", credA, null).statusCode());
        assertEquals(200, enviar(b, raiz + "/api/docentes/sesion/login", credB, null).statusCode());
        assertEquals(400, enviar(a, ruta, cuerpo, "clave-invalida").statusCode());
        var primera = enviar(a, ruta, cuerpo, clave);
        var reenvio = enviar(a, ruta, cuerpo, clave);
        assertEquals(201, primera.statusCode());
        assertEquals(200, reenvio.statusCode());
        var json = new tools.jackson.databind.ObjectMapper();
        long curso = json.readTree(primera.body()).get("id").asLong();
        assertEquals(curso, json.readTree(reenvio.body()).get("id").asLong());
        assertEquals(409, enviar(a, ruta, "{\"seccion\":\"V\"}", clave).statusCode());
        assertEquals(409, enviar(b, ruta, cuerpo, clave).statusCode());
        assertFalse(enviar(b, ruta, null, null).body().contains("3° W"));

        String otraClave = UUID.randomUUID().toString();
        var peticion = HttpRequest.newBuilder(URI.create(ruta))
            .header("Content-Type", "application/json")
            .header("Idempotency-Key", otraClave)
            .POST(HttpRequest.BodyPublishers.ofString("{\"seccion\":\"V\"}")).build();
        var uno = a.sendAsync(peticion, HttpResponse.BodyHandlers.ofString());
        var dos = a.sendAsync(peticion, HttpResponse.BodyHandlers.ofString());
        var respuestas = java.util.List.of(uno.join(), dos.join());
        assertEquals(java.util.Set.of(200, 201),
            respuestas.stream().map(HttpResponse::statusCode).collect(java.util.stream.Collectors.toSet()));
        assertEquals(json.readTree(respuestas.get(0).body()).get("id").asLong(),
            json.readTree(respuestas.get(1).body()).get("id").asLong());
        assertEquals(204, enviar(a, raiz + "/api/docentes/sesion/logout", "", null).statusCode());
        assertEquals(401, enviar(a, ruta, cuerpo, clave).statusCode());
    }

    private HttpResponse<String> enviar(HttpClient cliente, String url, String cuerpo, String clave) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(url));
        if (cuerpo == null) builder.GET();
        else {
            builder.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(cuerpo));
            if (clave != null) builder.header("Idempotency-Key", clave);
        }
        return cliente.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
