package com.example.mpazpro.backend;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Serializa los reenvíos de un docente y confirma curso, permiso y operación juntos. */
@Service
class CreacionCursoDocenteService {
    private final DocenteAulaRepository docentes;
    private final CursoCompartidoRepository cursos;
    private final CursoDocenteAulaRepository permisos;
    private final OperacionCursoDocenteRepository operaciones;

    record Resultado(Long id, String nombre, boolean creada) {}

    CreacionCursoDocenteService(DocenteAulaRepository docentes, CursoCompartidoRepository cursos,
                                CursoDocenteAulaRepository permisos, OperacionCursoDocenteRepository operaciones) {
        this.docentes = docentes;
        this.cursos = cursos;
        this.permisos = permisos;
        this.operaciones = operaciones;
    }

    @Transactional
    Resultado crear(Long docenteId, String seccion, String clave) {
        if (clave != null) {
            try {
                if (!UUID.fromString(clave).toString().equals(clave)) throw new IllegalArgumentException();
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clave de idempotencia inválida");
            }
        }
        var docente = docentes.bloquearPorId(docenteId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (clave != null) {
            var previa = operaciones.findByDocente_IdAndClave(docenteId, clave);
            if (previa.isPresent()) {
                var operacion = previa.get();
                if (!operacion.getSeccion().equals(seccion))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "La clave ya corresponde a otra solicitud");
                var curso = operacion.getCurso();
                if (!permisos.existsByDocente_IdAndCurso_Id(docenteId, curso.getId()))
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                return new Resultado(curso.getId(), curso.getNombre(), false);
            }
        }
        if (cursos.findBySeccion(seccion).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sección ya existe y requiere asignación administrativa");
        var curso = cursos.saveAndFlush(new CursoCompartido(seccion));
        permisos.saveAndFlush(new CursoDocenteAula(docente, curso));
        if (clave != null) operaciones.saveAndFlush(new OperacionCursoDocente(docente, clave, seccion, curso));
        return new Resultado(curso.getId(), curso.getNombre(), true);
    }
}
