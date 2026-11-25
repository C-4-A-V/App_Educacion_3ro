package com.example.mpazpro.backend;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:acceso_estudiante_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AccesoEstudianteAulaServiceTest {
    @Autowired AccesoEstudianteAulaService acceso;
    @Autowired IdentidadEstudianteAulaService identidades;
    @Autowired CursoCompartidoRepository cursos;
    @Autowired ClaveEstudianteAulaRepository claves;

    @Test void registraYVerificaSinGuardarElPinEnClaro() {
        var curso = cursos.findBySeccion("A").orElseThrow();
        UUID id = identidades.crear(curso.getId());
        acceso.registrar(id, "20.983.458-8", "1234".toCharArray());
        assertEquals(id, acceso.verificar("20983458-8", "1234".toCharArray()).orElseThrow());
        assertTrue(acceso.verificar("20983458-8", "9999".toCharArray()).isEmpty());
        assertTrue(acceso.verificar("12345678-9", "1234".toCharArray()).isEmpty());
        assertNotEquals("1234", claves.findById(id.toString()).orElseThrow().getHash());
        assertThrows(IllegalArgumentException.class,
            () -> acceso.registrar(id, "20983458-8", "1234".toCharArray()));
    }

    @Test void altaInternaCreaUnaIdentidadYNoSobrescribeDuplicados() {
        var curso = cursos.findBySeccion("A").orElseThrow();
        UUID id = acceso.crearEstudiante(curso.getId(), "11111111-1", "9876".toCharArray(), identidades);
        assertEquals(id, acceso.verificar("11111111-1", "9876".toCharArray()).orElseThrow());
        assertThrows(IllegalArgumentException.class,
            () -> acceso.crearEstudiante(curso.getId(), "11111111-1", "0000".toCharArray(), identidades));
        assertEquals(id, acceso.verificar("11111111-1", "9876".toCharArray()).orElseThrow());
    }

    @Test void rechazaIdentidadInexistenteYFormatoIncorrecto() {
        assertThrows(IllegalArgumentException.class,
            () -> acceso.registrar(UUID.randomUUID(), "20983458-8", "1234".toCharArray()));
        assertTrue(acceso.verificar("invalido", "1234".toCharArray()).isEmpty());
    }
}
