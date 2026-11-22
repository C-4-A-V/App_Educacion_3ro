package com.example.mpazpro.backend;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("prototipo-inseguro")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:completas_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ActividadCompletaApiTest {
    @LocalServerPort int port;
    private final HttpClient cliente = HttpClient.newHttpClient();

    @Test
    void guardaYFiltraUnaActividadCompletaYRechazaRespuestaInvalida() throws Exception {
        String datos = """
            {"cursoId":1,"asignaturaId":"ciencias","titulo":"Plantas",
             "instrucciones":"Elige una","pregunta":"¿Qué necesitan?",
             "alternativaA":"Luz","alternativaB":"Plástico",
             "alternativaC":"Piedras","alternativaD":"Metal",
             "respuestaCorrecta":0,"maxIntentos":3,
             "explicacion":"Necesitan luz","pista":"Mira hacia el sol"}
            """;
        HttpResponse<String> creada = enviar("POST", "/api/actividades-completas", datos);
        assertEquals(201, creada.statusCode());
        assertTrue(creada.body().contains("Mira hacia el sol"));
        String ubicacion = creada.headers().firstValue("Location").orElseThrow();
        assertEquals(200, enviar("GET", ubicacion, "").statusCode());
        assertTrue(enviar("GET", "/api/actividades-completas?cursoId=1&asignaturaId=ciencias", "").body().contains("Plantas"));
        assertFalse(enviar("GET", "/api/actividades-completas?cursoId=2&asignaturaId=ciencias", "").body().contains("Plantas"));
        assertEquals(400, enviar("POST", "/api/actividades-completas", datos.replace("\"respuestaCorrecta\":0", "\"respuestaCorrecta\":4")).statusCode());
    }

    @Test
    void aceptaTextosLargosDelFormularioYRechazaModalidadInvalida() throws Exception {
        String datos = """
            {"cursoId":1,"asignaturaId":"lenguaje","titulo":"Lectura",
             "instrucciones":"%s","pregunta":"%s",
             "alternativaA":"Primera","alternativaB":"Segunda",
             "alternativaC":"Tercera","alternativaD":"Cuarta",
             "respuestaCorrecta":0,"maxIntentos":3,
             "explicacion":"%s","pista":"%s"}
            """.formatted("i".repeat(1000), "p".repeat(500), "e".repeat(1000), "s".repeat(500));
        assertEquals(201, enviar("POST", "/api/actividades-completas", datos).statusCode());
        assertEquals(400, enviar("POST", "/api/actividades-completas",
            datos.replace("\"maxIntentos\":3", "\"maxIntentos\":2")).statusCode());
        assertEquals(400, enviar("POST", "/api/actividades-completas",
            datos.replace("Segunda", "primera")).statusCode());
    }

    @Test
    void estudianteRecibePreguntaSinRespuestaNiExplicacionYLaPistaSoloEnPractica() throws Exception {
        String datos = """
            {"cursoId":51,"asignaturaId":"historia","titulo":"Chile",
             "instrucciones":"Elige una","pregunta":"¿Qué es una isla?",
             "alternativaA":"Tierra rodeada por agua","alternativaB":"Un valle",
             "alternativaC":"Un bosque","alternativaD":"Una montaña",
             "respuestaCorrecta":0,"maxIntentos":3,
             "explicacion":"Una isla está rodeada por agua","pista":"Piensa en el mar"}
            """;
        assertEquals(201, enviar("POST", "/api/actividades-completas", datos).statusCode());
        String practica = enviar("GET", "/api/actividades-completas/para-estudiante?cursoId=51&asignaturaId=historia", "").body();
        assertTrue(practica.contains("Piensa en el mar"));
        assertFalse(practica.contains("respuestaCorrecta"));
        assertFalse(practica.contains("explicacion"));
        String evaluacion = datos.replace("\"cursoId\":51", "\"cursoId\":52")
            .replace("\"maxIntentos\":3", "\"maxIntentos\":1");
        assertEquals(201, enviar("POST", "/api/actividades-completas", evaluacion).statusCode());
        String respuesta = enviar("GET", "/api/actividades-completas/para-estudiante?cursoId=52&asignaturaId=historia", "").body();
        assertFalse(respuesta.contains("Piensa en el mar"));
        assertFalse(respuesta.contains("respuestaCorrecta"));
        assertFalse(respuesta.contains("explicacion"));
    }

    private HttpResponse<String> enviar(String metodo, String ruta, String cuerpo) throws Exception {
        HttpRequest.Builder solicitud = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + ruta));
        if (metodo.equals("POST")) solicitud.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(cuerpo));
        else solicitud.GET();
        return cliente.send(solicitud.build(), HttpResponse.BodyHandlers.ofString());
    }
}
