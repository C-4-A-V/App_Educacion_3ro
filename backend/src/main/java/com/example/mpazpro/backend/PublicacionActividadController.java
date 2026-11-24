package com.example.mpazpro.backend;

import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// Acceso limitado a la prueba local; falta autenticación para uso compartido real.
@Profile({"prototipo-inseguro", "aula-local"})
@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionActividadController {
    private final PublicacionActividadService servicio;
    private final IdentidadCatalogoRepository identidades;
    private final PublicacionActividadRepository publicaciones;
    private final PublicacionRetiradaRepository retiradas;

    public PublicacionActividadController(PublicacionActividadService servicio, IdentidadCatalogoRepository identidades,
                                          PublicacionActividadRepository publicaciones, PublicacionRetiradaRepository retiradas) {
        this.servicio = servicio;
        this.identidades = identidades;
        this.publicaciones = publicaciones;
        this.retiradas = retiradas;
    }

    public record EstadoClave(boolean publicada, boolean retirada) {}

    @GetMapping("/{clave}/estado")
    public EstadoClave estado(@RequestHeader("X-Catalogo-Id") String catalogoId, @PathVariable String clave) {
        if (!identidades.findById(1L).orElseThrow().getIdentificador().equals(catalogoId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Catálogo incorrecto");
        try {
            if (!java.util.UUID.fromString(clave).toString().equals(clave)) throw new IllegalArgumentException();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clave inválida");
        }
        return new EstadoClave(publicaciones.existsById(clave), retiradas.existsById(clave));
    }

    public record RetirarSolicitud(Long cursoId, java.util.List<String> claves) {}

    @PostMapping("/retirar")
    public ResponseEntity<Void> retirar(@RequestHeader("X-Catalogo-Id") String catalogoId,
                                         @RequestBody RetirarSolicitud solicitud) {
        String actual = identidades.findById(1L).orElseThrow().getIdentificador();
        if (!actual.equals(catalogoId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Catálogo incorrecto");
        servicio.retirar(solicitud.cursoId(), solicitud.claves());
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<PublicacionActividadService.Resultado> publicar(
        @RequestHeader("Idempotency-Key") String clave,
        @RequestHeader("X-Catalogo-Id") String catalogoId,
        @Valid @RequestBody ActividadCompletaRequest solicitud
    ) {
        String actual = identidades.findById(1L).orElseThrow().getIdentificador();
        if (!actual.equals(catalogoId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Catálogo incorrecto");
        var resultado = servicio.publicar(clave, solicitud);
        return ResponseEntity.status(resultado.creada() ? HttpStatus.CREATED : HttpStatus.OK).body(resultado);
    }
}
