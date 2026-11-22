package com.example.mpazpro.backend;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:proteccion_actividades_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ActividadCompletaProteccionTest {
    @LocalServerPort int puerto;

    @Test
    void rutaCompletaNoEstaDisponiblePorDefecto() throws Exception {
        var cliente = HttpClient.newHttpClient();
        var url = URI.create("http://127.0.0.1:" + puerto + "/api/actividades-completas?cursoId=1&asignaturaId=lenguaje");
        var get = HttpRequest.newBuilder(url).GET().build();
        var post = HttpRequest.newBuilder(url).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{}")).build();
        assertEquals(404, cliente.send(get, HttpResponse.BodyHandlers.discarding()).statusCode());
        assertEquals(404, cliente.send(post, HttpResponse.BodyHandlers.discarding()).statusCode());
        var publicacion = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + "/api/publicaciones"))
            .POST(HttpRequest.BodyPublishers.ofString("{}")).build();
        var aula = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + "/api/aula/actividades?cursoId=1&asignaturaId=lenguaje"))
            .GET().build();
        assertEquals(404, cliente.send(publicacion, HttpResponse.BodyHandlers.discarding()).statusCode());
        assertEquals(404, cliente.send(aula, HttpResponse.BodyHandlers.discarding()).statusCode());
        var respuesta = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + "/api/aula/respuestas"))
            .POST(HttpRequest.BodyPublishers.ofString("{}")).build();
        assertEquals(404, cliente.send(respuesta, HttpResponse.BodyHandlers.discarding()).statusCode());
    }
}
