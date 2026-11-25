package com.example.mpazpro.backend;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Datos ficticios y ejecución local únicamente hasta contar con autenticación.
@Profile("aula-local & !aula-autenticada")
@RestController
@RequestMapping("/api/aula/actividades")
public class AulaActividadesController {
    private final ActividadCompletaRepository actividades;
    private final IdentidadCatalogoRepository identidades;
    private final PublicacionActividadRepository publicaciones;
    private final IntentoAulaRepository intentos;

    public AulaActividadesController(ActividadCompletaRepository actividades, IdentidadCatalogoRepository identidades,
                                     PublicacionActividadRepository publicaciones, IntentoAulaRepository intentos) {
        this.actividades = actividades;
        this.identidades = identidades;
        this.publicaciones = publicaciones;
        this.intentos = intentos;
    }

    @GetMapping
    public List<ActividadParaEstudiante> listar(@RequestHeader("X-Catalogo-Id") String catalogoId,
                                               @RequestParam Long cursoId, @RequestParam String asignaturaId) {
        validarCatalogo(catalogoId);
        return actividades.findByCursoIdAndAsignaturaIdOrderByIdAsc(cursoId, asignaturaId).stream()
            .filter(a -> publicaciones.estaActiva(a.getId()))
            .map(a -> ActividadParaEstudiante.desde(a, intentos.existsByActividadId(a.getId()))).toList();
    }

    @GetMapping("/{id}")
    public ActividadParaEstudiante obtener(@RequestHeader("X-Catalogo-Id") String catalogoId,
                                           @PathVariable Long id, @RequestParam Long cursoId) {
        validarCatalogo(catalogoId);
        var actividad = actividades.findById(id)
            .filter(a -> a.getCursoId().equals(cursoId) && publicaciones.estaActiva(a.getId()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ActividadParaEstudiante.desde(actividad, intentos.existsByActividadId(actividad.getId()));
    }

    private void validarCatalogo(String catalogoId) {
        if (!identidades.findById(1L).orElseThrow().getIdentificador().equals(catalogoId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Catálogo incorrecto");
    }
}
