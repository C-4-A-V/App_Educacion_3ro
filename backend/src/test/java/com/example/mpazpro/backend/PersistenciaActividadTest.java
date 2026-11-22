package com.example.mpazpro.backend;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class PersistenciaActividadTest {
    @TempDir
    Path temporal;

    @Test
    void conservaLaActividadAlReiniciarElBackend() {
        String ruta = temporal.resolve("base-prueba").toAbsolutePath().toString().replace('\\', '/');
        String url = "--spring.datasource.url=jdbc:h2:file:" + ruta;

        try (ConfigurableApplicationContext primero = iniciar(url)) {
            ActividadRepository repo = primero.getBean(ActividadRepository.class);
            repo.save(new Actividad("ciencias", "Prueba de persistencia", "¿Qué necesita una planta?"));
        }

        try (ConfigurableApplicationContext segundo = iniciar(url)) {
            ActividadRepository repo = segundo.getBean(ActividadRepository.class);
            assertEquals(1, repo.count());
            assertEquals("Prueba de persistencia", repo.findAllByOrderByIdAsc().getFirst().getTitulo());
        }
    }

    private ConfigurableApplicationContext iniciar(String url) {
        return new SpringApplicationBuilder(MpazBackendApplication.class)
            .web(WebApplicationType.NONE)
            .run(url, "--spring.jpa.hibernate.ddl-auto=update", "--spring.main.banner-mode=off");
    }
}
