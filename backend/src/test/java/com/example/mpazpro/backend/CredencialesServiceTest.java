package com.example.mpazpro.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:credenciales_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class CredencialesServiceTest {
    @Autowired InstitucionRepository instituciones;
    @Autowired MiembroInstitucionRepository miembros;
    @Autowired CredencialInstitucionRepository credenciales;
    @Autowired CredencialesService servicio;

    @Test
    void protegeLaClaveYRechazaAccesosIncorrectosOPendientes() {
        Institucion institucion = instituciones.save(new Institucion("Escuela de prueba"));
        MiembroInstitucion activo = miembros.save(new MiembroInstitucion(institucion, "docente@example.com",
            MiembroInstitucion.Rol.DOCENTE, MiembroInstitucion.Estado.ACTIVO));
        MiembroInstitucion pendiente = miembros.save(new MiembroInstitucion(institucion, "pendiente@example.com",
            MiembroInstitucion.Rol.DOCENTE, MiembroInstitucion.Estado.PENDIENTE));

        servicio.crear(activo.getId(), "clave-de-prueba");
        servicio.crear(pendiente.getId(), "clave-de-prueba");

        assertNotEquals("clave-de-prueba", credenciales.findById(activo.getId()).orElseThrow().getClaveProtegida());
        assertTrue(servicio.verificar(activo.getId(), "clave-de-prueba"));
        assertFalse(servicio.verificar(activo.getId(), "clave-incorrecta"));
        assertFalse(servicio.verificar(pendiente.getId(), "clave-de-prueba"));
        assertFalse(servicio.verificar(99999L, "clave-de-prueba"));
    }
}
