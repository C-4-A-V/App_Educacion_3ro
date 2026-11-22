package com.example.mpazpro.backend;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsignacionDocenteService {
    private final MiembroInstitucionRepository miembros;
    private final CursoInstitucionRepository cursos;
    private final AsignacionDocenteRepository asignaciones;

    public AsignacionDocenteService(MiembroInstitucionRepository miembros, CursoInstitucionRepository cursos,
                                     AsignacionDocenteRepository asignaciones) {
        this.miembros = miembros;
        this.cursos = cursos;
        this.asignaciones = asignaciones;
    }

    // Operación interna: la autorización del administrador se implementará junto con el inicio de sesión del servidor.
    @Transactional
    public AsignacionDocente asignar(Long docenteId, Long cursoId, AsignacionDocente.Asignatura asignatura) {
        MiembroInstitucion docente = miembros.findById(docenteId).orElseThrow();
        CursoInstitucion curso = cursos.findById(cursoId).orElseThrow();
        if (asignatura == null || docente.getRol() != MiembroInstitucion.Rol.DOCENTE
            || docente.getEstado() != MiembroInstitucion.Estado.ACTIVO
            || !docente.getInstitucion().getId().equals(curso.getInstitucion().getId())) {
            throw new IllegalArgumentException("Asignación inválida");
        }
        if (asignaciones.existsByDocenteIdAndCursoIdAndAsignatura(docenteId, cursoId, asignatura)) {
            throw new IllegalArgumentException("La asignación ya existe");
        }
        return asignaciones.save(new AsignacionDocente(docente, curso, asignatura));
    }
}
