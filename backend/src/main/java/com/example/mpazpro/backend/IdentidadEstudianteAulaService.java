package com.example.mpazpro.backend;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Operaciones internas; aún no exponen un registro o acceso por HTTP. */
@Service
public class IdentidadEstudianteAulaService {
    private final IdentidadEstudianteAulaRepository identidades;
    private final CursoCompartidoRepository cursos;

    public IdentidadEstudianteAulaService(IdentidadEstudianteAulaRepository identidades, CursoCompartidoRepository cursos) {
        this.identidades = identidades;
        this.cursos = cursos;
    }

    @Transactional
    public UUID crear(Long cursoId) {
        if (cursoId == null) throw new IllegalArgumentException("Curso requerido");
        CursoCompartido curso = cursos.findById(cursoId).orElseThrow();
        UUID identificador = UUID.randomUUID();
        identidades.saveAndFlush(new IdentidadEstudianteAula(identificador, curso));
        return identificador;
    }

    @Transactional(readOnly = true)
    public boolean perteneceAlCurso(UUID identificador, Long cursoId) {
        if (identificador == null || cursoId == null) return false;
        return identidades.findByIdentificador(identificador.toString())
            .map(estudiante -> estudiante.getCurso().getId().equals(cursoId))
            .orElse(false);
    }
}
