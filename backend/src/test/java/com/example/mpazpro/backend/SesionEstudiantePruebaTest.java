package com.example.mpazpro.backend;

import static org.junit.jupiter.api.Assertions.*;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("sesion-prueba")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:sesion_estudiante_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SesionEstudiantePruebaTest {
    @LocalServerPort int puerto;
    @Autowired CursoCompartidoRepository cursos;
    @Autowired IdentidadEstudianteAulaService identidades;
    @Autowired AccesoEstudianteAulaService acceso;

    @Test void sesionYLimiteDeIntentos() throws Exception {
        var identidad = identidades.crear(cursos.findBySeccion("A").orElseThrow().getId());
        acceso.registrar(identidad, "20983458-8", "1234".toCharArray());
        var cliente = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
        String raiz = "http://127.0.0.1:" + puerto + "/api/sesion-estudiante-prueba";
        assertEquals(401, enviar(cliente, raiz, "GET", null));
        assertEquals(401, enviar(cliente, raiz + "/login", "POST", "{\"rut\":\"20983458-8\",\"pin\":\"0000\"}"));
        assertEquals(200, enviar(cliente, raiz + "/login", "POST", "{\"rut\":\"20983458-8\",\"pin\":\"1234\"}"));
        assertEquals(200, enviar(cliente, raiz, "GET", null));
        assertEquals(401, enviar(cliente, raiz + "/login", "POST", "{\"rut\":\"20983458-8\",\"pin\":\"0000\"}"));
        assertEquals(401, enviar(cliente, raiz, "GET", null));
        assertEquals(200, enviar(cliente, raiz + "/login", "POST", "{\"rut\":\"20983458-8\",\"pin\":\"1234\"}"));
        assertEquals(200, enviar(cliente, raiz + "/logout", "POST", null));
        assertEquals(401, enviar(cliente, raiz, "GET", null));
        for (int i = 0; i < 1; i++)
            assertEquals(401, enviar(cliente, raiz + "/login", "POST", "{\"rut\":\"20983458-8\",\"pin\":\"0000\"}"));
        assertEquals(429, enviar(cliente, raiz + "/login", "POST", "{\"rut\":\"20983458-8\",\"pin\":\"1234\"}"));
    }

    private int enviar(HttpClient cliente, String url, String metodo, String cuerpo) throws Exception {
        var solicitud = HttpRequest.newBuilder(URI.create(url));
        if (metodo.equals("GET")) solicitud.GET();
        else solicitud.header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(cuerpo == null ? "" : cuerpo));
        return cliente.send(solicitud.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
    }
}
