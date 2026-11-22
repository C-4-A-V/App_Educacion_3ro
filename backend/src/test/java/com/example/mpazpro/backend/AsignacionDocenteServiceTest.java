package com.example.mpazpro.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:instituciones_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class AsignacionDocenteServiceTest {
    @Autowired InstitucionRepository instituciones;
    @Autowired CursoInstitucionRepository cursos;
    @Autowired MiembroInstitucionRepository miembros;
    @Autowired AsignacionDocenteService servicio;

    @Test
    void asignaDosCursosYRechazaDuplicadosOInstitucionDiferente() {
        Institucion colegio = instituciones.save(new Institucion("Colegio de prueba"));
        Institucion otro = instituciones.save(new Institucion("Otra institución"));
        CursoInstitucion cursoA = cursos.save(new CursoInstitucion(colegio, "3 A"));
        CursoInstitucion cursoC = cursos.save(new CursoInstitucion(colegio, "3 C"));
        CursoInstitucion ajeno = cursos.save(new CursoInstitucion(otro, "3 A"));
        MiembroInstitucion docente = miembros.save(new MiembroInstitucion(colegio, "profe@example.com",
            MiembroInstitucion.Rol.DOCENTE, MiembroInstitucion.Estado.ACTIVO));
        MiembroInstitucion pendiente = miembros.save(new MiembroInstitucion(colegio, "pendiente@example.com",
            MiembroInstitucion.Rol.DOCENTE, MiembroInstitucion.Estado.PENDIENTE));

        assertNotNull(servicio.asignar(docente.getId(), cursoA.getId(), AsignacionDocente.Asignatura.LENGUAJE).getId());
        assertNotNull(servicio.asignar(docente.getId(), cursoC.getId(), AsignacionDocente.Asignatura.LENGUAJE).getId());
        assertThrows(IllegalArgumentException.class, () -> servicio.asignar(docente.getId(), cursoA.getId(), AsignacionDocente.Asignatura.LENGUAJE));
        assertThrows(IllegalArgumentException.class, () -> servicio.asignar(docente.getId(), ajeno.getId(), AsignacionDocente.Asignatura.LENGUAJE));
        assertThrows(IllegalArgumentException.class, () -> servicio.asignar(pendiente.getId(), cursoA.getId(), AsignacionDocente.Asignatura.LENGUAJE));
    }
}
