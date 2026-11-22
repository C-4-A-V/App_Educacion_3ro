package com.example.mpazpro.backend;

import java.nio.file.Path;
import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

class ActualizacionAulaConPreguntasTest {
    @TempDir Path temporal;

    @Test
    void agregaTablaDeIntentosYConservaPreguntasPublicadas() throws Exception {
        String ruta = temporal.resolve("base-aislada").toAbsolutePath().toString().replace('\\', '/');
        String jdbc = "jdbc:h2:file:" + ruta;
        String clave = "4abf76c2-347b-4b76-9910-fcb0aff16774";
        String identidad;
        Long curso;
        Long actividad;
        ActividadCompletaRequest datos;

        try (ConfigurableApplicationContext primero = iniciar(jdbc)) {
            identidad = primero.getBean(IdentidadCatalogoRepository.class).findById(1L).orElseThrow().getIdentificador();
            curso = primero.getBean(CursoCompartidoRepository.class).findBySeccion("A").orElseThrow().getId();
            datos = new ActividadCompletaRequest(curso, "lenguaje", "Lectura", "Lee", "¿Qué es un cuento?",
                "Historia", "Número", "Planta", "Suma", 0, 3, "Es una historia", "Piensa en un relato");
            actividad = primero.getBean(PublicacionActividadService.class).publicar(clave, datos).actividadId();
        }
        // Solo se elimina la tabla nueva dentro de esta base temporal para simular la versión anterior.
        try (var conexion = DriverManager.getConnection(jdbc, "sa", ""); var sql = conexion.createStatement()) {
            sql.execute("DROP TABLE intentos_aula");
        }
        try (ConfigurableApplicationContext segundo = iniciar(jdbc)) {
            assertEquals(identidad, segundo.getBean(IdentidadCatalogoRepository.class).findById(1L).orElseThrow().getIdentificador());
            assertEquals("Lectura", segundo.getBean(ActividadCompletaRepository.class).findById(actividad).orElseThrow().getTitulo());
            var repetida = segundo.getBean(PublicacionActividadService.class).publicar(clave, datos);
            assertFalse(repetida.creada());
            assertEquals(actividad, repetida.actividadId());
            assertEquals(0, segundo.getBean(IntentoAulaRepository.class).count());
        }
    }

    private ConfigurableApplicationContext iniciar(String jdbc) {
        return new SpringApplicationBuilder(MpazBackendApplication.class).web(WebApplicationType.NONE)
            .run("--spring.datasource.url=" + jdbc, "--spring.profiles.active=aula-local",
                 "--spring.jpa.hibernate.ddl-auto=update", "--spring.main.banner-mode=off");
    }
}
