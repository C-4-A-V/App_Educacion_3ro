package com.example.mpazpro.backend;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

// Solo para pruebas aisladas: aún no existe autorización por curso ni sesión autenticada.
@Profile("prototipo-inseguro & !aula-autenticada")
@RestController
@RequestMapping("/api/actividades-completas")
public class ActividadCompletaController {
    private final ActividadCompletaRepository repository;

    public ActividadCompletaController(ActividadCompletaRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<ActividadCompleta> crear(@Valid @RequestBody ActividadCompletaRequest solicitud) {
        ActividadCompleta guardada = repository.save(new ActividadCompleta(solicitud));
        return ResponseEntity.created(URI.create("/api/actividades-completas/" + guardada.getId())).body(guardada);
    }

    @GetMapping("/para-estudiante")
    public List<ActividadParaEstudiante> paraEstudiante(@RequestParam Long cursoId, @RequestParam String asignaturaId) {
        return repository.findByCursoIdAndAsignaturaIdOrderByIdAsc(cursoId, asignaturaId)
            .stream().map(ActividadParaEstudiante::desde).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActividadCompleta> obtener(@PathVariable Long id) {
        return ResponseEntity.of(repository.findById(id));
    }

    @GetMapping
    public List<ActividadCompleta> listar(@RequestParam Long cursoId, @RequestParam String asignaturaId) {
        return repository.findByCursoIdAndAsignaturaIdOrderByIdAsc(cursoId, asignaturaId);
    }
}
