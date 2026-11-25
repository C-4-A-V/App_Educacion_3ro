package com.example.mpazpro.backend;

import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Operación administrativa interna, sin endpoint público. */
@Service
class AsignacionCursoDocenteService {
    private final DocenteAulaRepository docentes;
    private final CursoCompartidoRepository cursos;
    private final CursoDocenteAulaRepository permisos;
    AsignacionCursoDocenteService(DocenteAulaRepository docentes, CursoCompartidoRepository cursos,
                                  CursoDocenteAulaRepository permisos) {
        this.docentes = docentes; this.cursos = cursos; this.permisos = permisos;
    }

    @Transactional
    void asignarExistente(String correo, String seccion) {
        if (correo == null || seccion == null || !seccion.matches("[A-Z]"))
            throw new IllegalArgumentException("Datos inválidos");
        var docente = docentes.findByCorreo(correo.trim().toLowerCase(Locale.ROOT)).orElseThrow();
        var curso = cursos.findBySeccion(seccion).orElseThrow();
        if (!permisos.existsByDocente_IdAndCurso_Id(docente.getId(), curso.getId()))
            permisos.saveAndFlush(new CursoDocenteAula(docente, curso));
    }
}
