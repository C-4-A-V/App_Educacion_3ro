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

@ActiveProfiles("prototipo-inseguro")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:publicacion_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PublicacionActividadApiTest {
    @LocalServerPort int puerto;
    @Autowired CursoCompartidoRepository cursos;
    @Autowired ActividadCompletaRepository actividades;
    @Autowired IdentidadCatalogoRepository identidades;
    private final HttpClient cliente = HttpClient.newHttpClient();

    @Test
    void reintentoNoDuplicaYClaveReutilizadaConOtrosDatosSeRechaza() throws Exception {
        Long curso = cursos.findBySeccion("A").orElseThrow().getId();
        String datos = """
            {"cursoId":%d,"asignaturaId":"lenguaje","titulo":"Lectura",
             "instrucciones":"Elige una","pregunta":"¿Qué es un cuento?",
             "alternativaA":"Una historia","alternativaB":"Un número",
             "alternativaC":"Una planta","alternativaD":"Una suma",
             "respuestaCorrecta":0,"maxIntentos":3,
             "explicacion":"Un cuento es una historia","pista":"Piensa en narraciones"}
            """.formatted(curso);
        String clave = "4abf76c2-347b-4b76-9910-fcb0aff16774";
        long antes = actividades.count();
        var primera = publicar(clave, datos);
        var repetida = publicar(clave, datos);
        assertEquals(201, primera.statusCode());
        assertEquals(200, repetida.statusCode());
        assertEquals(antes + 1, actividades.count());
        assertTrue(primera.body().contains("\"creada\":true"));
        assertTrue(repetida.body().contains("\"creada\":false"));
        assertEquals(409, publicar(clave, datos.replace("Lectura", "Otro título")).statusCode());
        assertEquals(400, publicar("no-es-uuid", datos).statusCode());
        var catalogoErroneo = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + "/api/publicaciones"))
            .header("Content-Type", "application/json")
            .header("Idempotency-Key", "5abf76c2-347b-4b76-9910-fcb0aff16774")
            .header("X-Catalogo-Id", "catalogo-distinto")
            .POST(HttpRequest.BodyPublishers.ofString(datos)).build();
        assertEquals(409, cliente.send(catalogoErroneo, HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(antes + 1, actividades.count());
    }

    private HttpResponse<String> publicar(String clave, String datos) throws Exception {
        var solicitud = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + "/api/publicaciones"))
            .header("Content-Type", "application/json")
            .header("Idempotency-Key", clave)
            .header("X-Catalogo-Id", identidades.findById(1L).orElseThrow().getIdentificador())
            .POST(HttpRequest.BodyPublishers.ofString(datos))
            .build();
        return cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
    }
}
