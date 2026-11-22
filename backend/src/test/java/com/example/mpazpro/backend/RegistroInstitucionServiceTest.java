package com.example.mpazpro.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:registro_institucion_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class RegistroInstitucionServiceTest {
    @Autowired InstitucionRepository instituciones;
    @Autowired MiembroInstitucionRepository miembros;
    @Autowired RegistroInstitucionService servicio;

    @Test
    void registroQuedaPendienteYAdministradorDeLaInstitucionLoAprueba() {
        Institucion institucion = instituciones.save(new Institucion("Colegio de prueba"));
        MiembroInstitucion admin = miembros.save(new MiembroInstitucion(institucion, "admin@example.com",
            MiembroInstitucion.Rol.ADMINISTRADOR, MiembroInstitucion.Estado.ACTIVO));
        MiembroInstitucion docente = servicio.registrarPendiente(institucion.getId(), " Profe@Example.com ");
        assertEquals("profe@example.com", docente.getCorreo());
        assertEquals(MiembroInstitucion.Rol.DOCENTE, docente.getRol());
        assertEquals(MiembroInstitucion.Estado.PENDIENTE, docente.getEstado());

        servicio.aprobarPendiente(admin.getId(), docente.getId());
        assertEquals(MiembroInstitucion.Estado.ACTIVO, miembros.findById(docente.getId()).orElseThrow().getEstado());
    }

    @Test
    void rechazaAprobacionPorDocenteOAdministradorDeOtraInstitucion() {
        Institucion institucion = instituciones.save(new Institucion("Colegio A"));
        Institucion otra = instituciones.save(new Institucion("Colegio B"));
        MiembroInstitucion pendiente = servicio.registrarPendiente(institucion.getId(), "profe@example.com");
        MiembroInstitucion docente = miembros.save(new MiembroInstitucion(institucion, "otro@example.com",
            MiembroInstitucion.Rol.DOCENTE, MiembroInstitucion.Estado.ACTIVO));
        MiembroInstitucion ajeno = miembros.save(new MiembroInstitucion(otra, "admin@example.com",
            MiembroInstitucion.Rol.ADMINISTRADOR, MiembroInstitucion.Estado.ACTIVO));

        assertThrows(IllegalArgumentException.class, () -> servicio.aprobarPendiente(docente.getId(), pendiente.getId()));
        assertThrows(IllegalArgumentException.class, () -> servicio.aprobarPendiente(ajeno.getId(), pendiente.getId()));
        assertEquals(MiembroInstitucion.Estado.PENDIENTE, pendiente.getEstado());
    }

    @Test
    void rechazaCorreoDuplicadoConDiferenteFormato() {
        Institucion institucion = instituciones.save(new Institucion("Colegio C"));
        servicio.registrarPendiente(institucion.getId(), "PROFE@example.com");
        assertThrows(IllegalArgumentException.class,
            () -> servicio.registrarPendiente(institucion.getId(), " profe@EXAMPLE.com "));
    }
}
