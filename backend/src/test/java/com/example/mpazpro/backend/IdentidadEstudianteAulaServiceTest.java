package com.example.mpazpro.backend;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:identidad_estudiante_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class IdentidadEstudianteAulaServiceTest {
    @Autowired IdentidadEstudianteAulaService servicio;
    @Autowired CursoCompartidoRepository cursos;
    @Autowired IdentidadEstudianteAulaRepository identidades;

    @Test void asignaIdentidadesDistintasYLasAsociaAlCursoCorrecto() {
        var a = cursos.findBySeccion("A").orElseThrow();
        var b = cursos.findBySeccion("B").orElseGet(() -> cursos.saveAndFlush(new CursoCompartido("B")));
        UUID primero = servicio.crear(a.getId());
        UUID segundo = servicio.crear(a.getId());
        assertNotEquals(primero, segundo);
        assertTrue(servicio.perteneceAlCurso(primero, a.getId()));
        assertFalse(servicio.perteneceAlCurso(primero, b.getId()));
        assertFalse(servicio.perteneceAlCurso(UUID.randomUUID(), a.getId()));
        assertTrue(identidades.findByIdentificador(primero.toString()).isPresent());
    }

    @Test void noCreaIdentidadParaCursoInexistente() {
        long antes = identidades.count();
        assertThrows(java.util.NoSuchElementException.class, () -> servicio.crear(Long.MAX_VALUE));
        assertEquals(antes, identidades.count());
        assertFalse(servicio.perteneceAlCurso(null, 1L));
    }
}
