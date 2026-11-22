package com.example.mpazpro.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:acceso_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class AccesoInstitucionServiceTest {
    @Autowired InstitucionRepository instituciones;
    @Autowired MiembroInstitucionRepository miembros;
    @Autowired CredencialesService credenciales;
    @Autowired AccesoInstitucionService acceso;

    @Test
    void verificaInstitucionCorreoClaveYEstado() {
        Institucion primera = instituciones.save(new Institucion("Escuela A"));
        Institucion segunda = instituciones.save(new Institucion("Escuela B"));
        MiembroInstitucion docente = miembros.save(new MiembroInstitucion(primera, "profe@example.com",
            MiembroInstitucion.Rol.DOCENTE, MiembroInstitucion.Estado.ACTIVO));
        MiembroInstitucion pendiente = miembros.save(new MiembroInstitucion(primera, "espera@example.com",
            MiembroInstitucion.Rol.DOCENTE, MiembroInstitucion.Estado.PENDIENTE));
        credenciales.crear(docente.getId(), "clave-de-prueba");
        credenciales.crear(pendiente.getId(), "clave-de-prueba");

        var identidad = acceso.verificar(primera.getId(), " PROFE@example.com ", "clave-de-prueba");
        assertTrue(identidad.isPresent());
        assertEquals(docente.getId(), identidad.orElseThrow().miembroId());
        assertTrue(acceso.verificar(primera.getId(), "profe@example.com", "clave-incorrecta").isEmpty());
        assertTrue(acceso.verificar(segunda.getId(), "profe@example.com", "clave-de-prueba").isEmpty());
        assertTrue(acceso.verificar(primera.getId(), "espera@example.com", "clave-de-prueba").isEmpty());
    }
}
