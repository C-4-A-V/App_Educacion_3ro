package com.example.mpazpro.backend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Operaciones de prueba: identidad y curso siempre proceden de la sesión. */
@Profile("sesion-prueba")
@RestController
@RequestMapping("/api/sesion-estudiante-prueba/respuestas")
class RespuestasSesionPruebaController {
    private final IdentidadEstudianteAulaRepository estudiantes;
    private final RespuestaAulaService respuestas;

    RespuestasSesionPruebaController(IdentidadEstudianteAulaRepository estudiantes, RespuestaAulaService respuestas) {
        this.estudiantes = estudiantes;
        this.respuestas = respuestas;
    }

    record Solicitud(@NotNull @Min(1) Long actividadId, @NotNull @Min(1) @Max(3) Integer numero,
                     @NotNull @Min(0) @Max(3) Integer respuestaElegida) {}

    @PostMapping
    @Transactional
    RespuestaAulaService.Resultado responder(@Valid @RequestBody Solicitud datos, HttpServletRequest solicitud) {
        var estudiante = estudiante(solicitud);
        return respuestas.responder(new RespuestaAulaRequest(datos.actividadId(), estudiante.getCurso().getId(),
            estudiante.getIdentificador(), datos.numero(), datos.respuestaElegida()));
    }

    @GetMapping("/{actividadId}")
    @Transactional(readOnly = true)
    ResponseEntity<RespuestaAulaService.Resultado> estado(@PathVariable Long actividadId, HttpServletRequest solicitud) {
        var estudiante = estudiante(solicitud);
        return respuestas.estado(new EstadoRespuestaAulaRequest(actividadId, estudiante.getCurso().getId(),
            estudiante.getIdentificador())).map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private IdentidadEstudianteAula estudiante(HttpServletRequest solicitud) {
        var sesion = solicitud.getSession(false);
        if (sesion == null || !(sesion.getAttribute("estudianteId") instanceof String id))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return estudiantes.findByIdentificador(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
