package com.example.mpazpro.backend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Profile("aula-autenticada")
@RestController
@RequestMapping("/api/docentes/cursos")
class CursosDocenteAutenticadoController {
    private final DocenteAulaRepository docentes;
    private final CreacionCursoDocenteService creacion;
    private final CursoDocenteAulaRepository permisos;
    private final PublicacionActividadService publicador;
    private final ActividadCompletaRepository actividades;
    private final IntentoAulaRepository intentos;

    CursosDocenteAutenticadoController(DocenteAulaRepository docentes, CreacionCursoDocenteService creacion,
            CursoDocenteAulaRepository permisos, PublicacionActividadService publicador,
            ActividadCompletaRepository actividades, IntentoAulaRepository intentos) {
        this.docentes = docentes; this.creacion = creacion; this.permisos = permisos;
        this.publicador = publicador; this.actividades = actividades; this.intentos = intentos;
    }

    record CrearCurso(@jakarta.validation.constraints.NotNull @Pattern(regexp = "[A-Z]") String seccion) {}
    record Curso(Long id, String nombre) {}
    record Resumen(Long actividadId, long finalizadas, long correctas) {}

    @GetMapping
    @Transactional(readOnly = true)
    List<Curso> listar(HttpServletRequest request) {
        Long id = docente(request);
        return permisos.findByDocente_IdOrderByCurso_SeccionAsc(id).stream()
            .map(p -> new Curso(p.getCurso().getId(), p.getCurso().getNombre())).toList();
    }

    @PostMapping
    ResponseEntity<Curso> crear(@Valid @RequestBody CrearCurso datos,
            @RequestHeader(value = "Idempotency-Key", required = false) String clave, HttpServletRequest request) {
        try {
            var resultado = creacion.crear(docente(request), datos.seccion(), clave);
            return ResponseEntity.status(resultado.creada() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(new Curso(resultado.id(), resultado.nombre()));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Otra cuenta pudo crear simultáneamente la misma sección.
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sección ya existe");
        }
    }

    @PostMapping("/{cursoId}/publicaciones")
    ResponseEntity<PublicacionActividadService.Resultado> publicar(@PathVariable Long cursoId,
            @RequestHeader("Idempotency-Key") String clave, @Valid @RequestBody ActividadCompletaRequest datos,
            HttpServletRequest request) {
        autorizar(request, cursoId);
        if (!cursoId.equals(datos.cursoId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        var resultado = publicador.publicar(clave, datos);
        return ResponseEntity.status(resultado.creada() ? HttpStatus.CREATED : HttpStatus.OK).body(resultado);
    }

    @PostMapping("/{cursoId}/publicaciones/retirar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void retirar(@PathVariable Long cursoId, @RequestBody List<String> claves, HttpServletRequest request) {
        autorizar(request, cursoId);
        publicador.retirar(cursoId, claves);
    }

    @GetMapping("/{cursoId}/actividades/{actividadId}/resultados")
    @Transactional(readOnly = true)
    Resumen resultados(@PathVariable Long cursoId, @PathVariable Long actividadId, HttpServletRequest request) {
        autorizar(request, cursoId);
        var actividad = actividades.findById(actividadId)
            .filter(a -> a.getCursoId().equals(cursoId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return new Resumen(actividad.getId(), intentos.countByActividad_IdAndFinalizadaTrue(actividadId),
            intentos.countByActividad_IdAndFinalizadaTrueAndCorrectaTrue(actividadId));
    }

    private Long docente(HttpServletRequest request) {
        Long id = SesionDocenteController.docenteId(request);
        if (!docentes.existsById(id)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return id;
    }

    private void autorizar(HttpServletRequest request, Long cursoId) {
        if (!permisos.existsByDocente_IdAndCurso_Id(docente(request), cursoId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
