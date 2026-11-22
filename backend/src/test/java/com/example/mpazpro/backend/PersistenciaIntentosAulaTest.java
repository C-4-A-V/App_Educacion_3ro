package com.example.mpazpro.backend;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

class PersistenciaIntentosAulaTest {
    @TempDir Path temporal;

    @Test
    void conservaIntentosAlReiniciarSinDatosPersonales() {
        String ruta = temporal.resolve("intentos-aislados").toAbsolutePath().toString().replace('\\', '/');
        String url = "--spring.datasource.url=jdbc:h2:file:" + ruta;
        String token = "55555555-5555-5555-5555-555555555555";
        long cursoId;
        long actividadId;

        try (ConfigurableApplicationContext primero = iniciar(url)) {
            cursoId = primero.getBean(CursoCompartidoRepository.class).findBySeccion("A").orElseThrow().getId();
            actividadId = primero.getBean(ActividadCompletaRepository.class).save(new ActividadCompleta(
                new ActividadCompletaRequest(cursoId, "lenguaje", "Lectura", "Lee", "¿Qué es un cuento?",
                    "Historia", "Número", "Planta", "Suma", 0, 3, "Es una historia", "Piensa en un relato")
            )).getId();
            var guardada = primero.getBean(ActividadCompletaRepository.class).findById(actividadId).orElseThrow();
            primero.getBean(PublicacionActividadRepository.class).save(new PublicacionActividad(java.util.UUID.randomUUID().toString(), guardada));
            var primerIntento = primero.getBean(RespuestaAulaService.class)
                .responder(new RespuestaAulaRequest(actividadId, cursoId, token, 1, 1));
            assertFalse(primerIntento.finalizada());
        }
        try (ConfigurableApplicationContext segundo = iniciar(url)) {
            var resultado = segundo.getBean(RespuestaAulaService.class)
                .responder(new RespuestaAulaRequest(actividadId, cursoId, token, 2, 0));
            assertTrue(resultado.correcta());
            assertTrue(resultado.finalizada());
            assertEquals(2, segundo.getBean(IntentoAulaRepository.class).count());
        }
    }

    private ConfigurableApplicationContext iniciar(String url) {
        return new SpringApplicationBuilder(MpazBackendApplication.class)
            .web(WebApplicationType.NONE)
            .run(url, "--spring.profiles.active=aula-local", "--spring.jpa.hibernate.ddl-auto=update",
                 "--spring.main.banner-mode=off");
    }
}
