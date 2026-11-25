package com.example.mpazpro.backend;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Consulta aislada para probar la autorización por curso; no habilitar para producción. */
@Profile("sesion-prueba")
@RestController
@RequestMapping("/api/sesion-estudiante-prueba/actividades")
class ActividadesSesionPruebaController {
    private final IdentidadEstudianteAulaRepository estudiantes;
    private final ActividadCompletaRepository actividades;
    private final PublicacionActividadRepository publicaciones;
    private final IntentoAulaRepository intentos;

    ActividadesSesionPruebaController(IdentidadEstudianteAulaRepository estudiantes,
            ActividadCompletaRepository actividades, PublicacionActividadRepository publicaciones,
            IntentoAulaRepository intentos) {
        this.estudiantes = estudiantes;
        this.actividades = actividades;
        this.publicaciones = publicaciones;
        this.intentos = intentos;
    }

    @GetMapping
    @Transactional(readOnly = true)
    List<ActividadParaEstudiante> listar(@RequestParam String asignaturaId, HttpServletRequest solicitud) {
        var estudiante = estudiante(solicitud);
        return actividades.findByCursoIdAndAsignaturaIdOrderByIdAsc(estudiante.getCurso().getId(), asignaturaId)
            .stream().filter(a -> publicaciones.estaActiva(a.getId()))
            .map(a -> convertir(a, estudiante.getIdentificador())).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    ActividadParaEstudiante obtener(@PathVariable Long id, HttpServletRequest solicitud) {
        var estudiante = estudiante(solicitud);
        var actividad = actividades.findById(id)
            .filter(a -> a.getCursoId().equals(estudiante.getCurso().getId()) && publicaciones.estaActiva(id))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return convertir(actividad, estudiante.getIdentificador());
    }

    record Progreso(long total, long completadas, long correctas) {}

    @GetMapping("/progreso")
    @Transactional(readOnly = true)
    Progreso progreso(@RequestParam(required = false) String asignaturaId, HttpServletRequest solicitud) {
        var estudiante = estudiante(solicitud);
        var todas = asignaturaId == null
            ? actividades.findByCursoIdOrderByIdAsc(estudiante.getCurso().getId())
            : actividades.findByCursoIdAndAsignaturaIdOrderByIdAsc(estudiante.getCurso().getId(), asignaturaId);
        var visibles = todas.stream().filter(a -> publicaciones.estaActiva(a.getId())).toList();
        long completadas = 0;
        long correctas = 0;
        for (var actividad : visibles) {
            var ultimo = intentos.findTopByEstudianteTokenAndActividadIdOrderByNumeroDesc(
                estudiante.getIdentificador(), actividad.getId());
            if (ultimo.isPresent() && ultimo.get().isFinalizada()) {
                completadas++;
                if (ultimo.get().isCorrecta()) correctas++;
            }
        }
        return new Progreso(visibles.size(), completadas, correctas);
    }

    private IdentidadEstudianteAula estudiante(HttpServletRequest solicitud) {
        var sesion = solicitud.getSession(false);
        if (sesion == null || !(sesion.getAttribute("estudianteId") instanceof String id))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return estudiantes.findByIdentificador(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private ActividadParaEstudiante convertir(ActividadCompleta actividad, String id) {
        return ActividadParaEstudiante.desde(actividad,
            intentos.existsByEstudianteTokenAndActividadId(id, actividad.getId()));
    }
}
