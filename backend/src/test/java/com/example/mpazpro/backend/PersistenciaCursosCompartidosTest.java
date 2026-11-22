package com.example.mpazpro.backend;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

class PersistenciaCursosCompartidosTest {
    @TempDir Path temporal;

    @Test
    void conservaCursosYActividadesAlReiniciar() {
        String ruta = temporal.resolve("base-aislada").toAbsolutePath().toString().replace('\\', '/');
        String url = "--spring.datasource.url=jdbc:h2:file:" + ruta;

        String identidadOriginal;
        try (ConfigurableApplicationContext primero = iniciar(url)) {
            identidadOriginal = primero.getBean(IdentidadCatalogoRepository.class).findById(1L).orElseThrow().getIdentificador();
            var cursos = primero.getBean(CursoCompartidoRepository.class);
            assertEquals(1, cursos.findAllByOrderBySeccionAsc().size());
            cursos.save(new CursoCompartido("B"));
            primero.getBean(ActividadRepository.class)
                .save(new Actividad("lenguaje", "Actividad existente", "Una pregunta"));
        }

        try (ConfigurableApplicationContext segundo = iniciar(url)) {
            assertEquals(identidadOriginal, segundo.getBean(IdentidadCatalogoRepository.class).findById(1L).orElseThrow().getIdentificador());
            var cursos = segundo.getBean(CursoCompartidoRepository.class).findAllByOrderBySeccionAsc();
            assertEquals(2, cursos.size());
            assertEquals("A", cursos.get(0).getSeccion());
            assertEquals("B", cursos.get(1).getSeccion());
            var actividades = segundo.getBean(ActividadRepository.class).findAllByOrderByIdAsc();
            assertEquals(1, actividades.size());
            assertEquals("Actividad existente", actividades.getFirst().getTitulo());
        }
    }

    private ConfigurableApplicationContext iniciar(String url) {
        return new SpringApplicationBuilder(MpazBackendApplication.class)
            .web(WebApplicationType.NONE)
            .run(url, "--spring.jpa.hibernate.ddl-auto=update", "--spring.main.banner-mode=off");
    }
}
