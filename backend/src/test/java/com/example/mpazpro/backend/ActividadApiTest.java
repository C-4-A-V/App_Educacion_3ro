package com.example.mpazpro.backend;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:actividades_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
class ActividadApiTest {
    @LocalServerPort
    int port;

    @Autowired
    ActividadRepository repository;

    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void preparar() {
        repository.deleteAll();
        repository.save(new Actividad("matematica", "Suma de prueba", "¿Cuánto es 2 + 2?"));
        repository.save(new Actividad("lenguaje", "Lectura de prueba", "¿De qué trata el cuento?"));
    }

    @Test
    void listaActividadesYFiltraPorAsignatura() throws Exception {
        HttpResponse<String> todas = consultar("/api/actividades");
        assertEquals(200, todas.statusCode());
        assertTrue(todas.body().contains("Suma de prueba"));
        assertTrue(todas.body().contains("Lectura de prueba"));

        HttpResponse<String> matematicas = consultar("/api/actividades?asignaturaId=matematica");
        assertEquals(200, matematicas.statusCode());
        assertTrue(matematicas.body().contains("Suma de prueba"));
        assertFalse(matematicas.body().contains("Lectura de prueba"));

        HttpResponse<String> vacias = consultar("/api/actividades?asignaturaId=ciencias");
        assertEquals(200, vacias.statusCode());
        assertEquals("[]", vacias.body());
    }

    @Test
    void creaUnaActividadYPermiteConsultarlaPorId() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
            URI.create("http://127.0.0.1:" + port + "/api/actividades")
        ).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("""
                {"asignaturaId":"ciencias","titulo":"Los seres vivos","pregunta":"¿Qué necesitan las plantas?"}
                """)).build();
        HttpResponse<String> creada = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, creada.statusCode());
        assertTrue(creada.body().contains("Los seres vivos"));
        String ubicacion = creada.headers().firstValue("Location").orElseThrow();
        HttpResponse<String> consultada = client.send(
            HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port).resolve(ubicacion)).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, consultada.statusCode());
        assertTrue(consultada.body().contains("Los seres vivos"));
    }

    @Test
    void rechazaDatosInvalidosSinGuardar() throws Exception {
        long antes = repository.count();
        HttpRequest request = HttpRequest.newBuilder(
            URI.create("http://127.0.0.1:" + port + "/api/actividades")
        ).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("""
                {"asignaturaId":"otra","titulo":"   ","pregunta":""}
                """)).build();
        HttpResponse<String> respuesta = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, respuesta.statusCode());
        assertEquals(antes, repository.count());
        assertEquals(404, consultar("/api/actividades/999999").statusCode());
    }

    @Test
    void actualizaYEliminaSinAfectarOtrasActividades() throws Exception {
        long id = repository.findByAsignaturaIdOrderByIdAsc("matematica").getFirst().getId();
        long otraId = repository.findByAsignaturaIdOrderByIdAsc("lenguaje").getFirst().getId();
        String url = "http://127.0.0.1:" + port + "/api/actividades/" + id;
        HttpRequest actualizar = HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString("""
                {"asignaturaId":"matematica","titulo":"Suma actualizada","pregunta":"Cuanto es 3 + 3?"}
                """)).build();
        HttpResponse<String> actualizada = client.send(actualizar, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, actualizada.statusCode());
        assertTrue(actualizada.body().contains("Suma actualizada"));
        assertTrue(consultar("/api/actividades/" + id).body().contains("Suma actualizada"));

        HttpRequest invalida = HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString("""
                {"asignaturaId":"matematica","titulo":"","pregunta":"Pregunta"}
                """)).build();
        assertEquals(400, client.send(invalida, HttpResponse.BodyHandlers.ofString()).statusCode());
        assertTrue(consultar("/api/actividades/" + id).body().contains("Suma actualizada"));

        HttpRequest eliminar = HttpRequest.newBuilder(URI.create(url)).DELETE().build();
        assertEquals(204, client.send(eliminar, HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(404, consultar("/api/actividades/" + id).statusCode());
        assertEquals(200, consultar("/api/actividades/" + otraId).statusCode());
        assertEquals(404, client.send(eliminar, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    private HttpResponse<String> consultar(String ruta) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
            URI.create("http://127.0.0.1:" + port + ruta)
        ).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
