package com.example.mpazpro.backend;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:cursos_compartidos_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CursoCompartidoApiTest {
    @LocalServerPort int puerto;
    @Autowired IdentidadCatalogoRepository identidades;

    @Test
    void cursoInicialYCreacionSecuencialSonVisiblesEnLaMismaLista() throws Exception {
        HttpClient cliente = HttpClient.newHttpClient();
        URI lista = URI.create("http://127.0.0.1:" + puerto + "/api/cursos-compartidos");
        URI crear = URI.create(lista + "/siguiente");
        HttpRequest get = HttpRequest.newBuilder(lista).GET().build();
        HttpRequest post = HttpRequest.newBuilder(crear).POST(HttpRequest.BodyPublishers.noBody()).build();

        HttpRequest identidad = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + "/api/catalogo/identidad")).GET().build();
        String respuestaIdentidad = cliente.send(identidad, HttpResponse.BodyHandlers.ofString()).body();
        assertTrue(respuestaIdentidad.contains("\"persistente\":false"));
        assertTrue(respuestaIdentidad.contains("\"identificador\":"));
        String inicial = cliente.send(get, HttpResponse.BodyHandlers.ofString()).body();
        assertTrue(inicial.contains("\"seccion\":\"A\""));
        HttpRequest equivocado = HttpRequest.newBuilder(URI.create(crear + "?seccionEsperada=C"))
            .POST(HttpRequest.BodyPublishers.noBody()).build();
        assertEquals(409, cliente.send(equivocado, HttpResponse.BodyHandlers.ofString()).statusCode());
        HttpRequest confirmado = HttpRequest.newBuilder(URI.create(crear + "?seccionEsperada=B"))
            .POST(HttpRequest.BodyPublishers.noBody()).build();
        assertEquals(201, cliente.send(confirmado, HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(409, cliente.send(confirmado, HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(201, cliente.send(post, HttpResponse.BodyHandlers.ofString()).statusCode());
        String actualizado = cliente.send(get, HttpResponse.BodyHandlers.ofString()).body();
        assertTrue(actualizado.contains("\"seccion\":\"A\""));
        assertTrue(actualizado.contains("\"seccion\":\"B\""));
        assertTrue(actualizado.contains("\"seccion\":\"C\""));
        String catalogo = identidades.findById(1L).orElseThrow().getIdentificador();
        URI asegurar = URI.create(lista + "/D/asegurar");
        HttpRequest erroneo = HttpRequest.newBuilder(asegurar).header("X-Catalogo-Id", "otro")
            .POST(HttpRequest.BodyPublishers.noBody()).build();
        assertEquals(409, cliente.send(erroneo, HttpResponse.BodyHandlers.ofString()).statusCode());
        HttpRequest seguro = HttpRequest.newBuilder(asegurar).header("X-Catalogo-Id", catalogo)
            .POST(HttpRequest.BodyPublishers.noBody()).build();
        var creado = cliente.send(seguro, HttpResponse.BodyHandlers.ofString());
        var repetido = cliente.send(seguro, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, creado.statusCode());
        assertEquals(200, repetido.statusCode());
        assertEquals(creado.body(), repetido.body());
    }
}
