package com.example.mpazpro.backend;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActividadController {
    private final ActividadRepository repository;

    public ActividadController(ActividadRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/api/actividades")
    public ResponseEntity<Actividad> crear(@Valid @RequestBody CrearActividadRequest solicitud) {
        Actividad guardada = repository.save(new Actividad(
            solicitud.asignaturaId(), solicitud.titulo().trim(), solicitud.pregunta().trim()
        ));
        return ResponseEntity.created(URI.create("/api/actividades/" + guardada.getId()))
            .body(guardada);
    }

    @PutMapping("/api/actividades/{id}")
    public Actividad actualizar(@PathVariable Long id, @Valid @RequestBody CrearActividadRequest solicitud) {
        Actividad actividad = obtener(id);
        actividad.actualizar(solicitud.asignaturaId(), solicitud.titulo().trim(), solicitud.pregunta().trim());
        return repository.save(actividad);
    }

    @DeleteMapping("/api/actividades/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Actividad actividad = obtener(id);
        repository.delete(actividad);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/actividades/{id}")
    public Actividad obtener(@PathVariable Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/api/actividades")
    public List<Actividad> listar(@RequestParam(required = false) String asignaturaId) {
        if (asignaturaId == null || asignaturaId.isBlank()) {
            return repository.findAllByOrderByIdAsc();
        }
        return repository.findByAsignaturaIdOrderByIdAsc(asignaturaId.trim());
    }
}
