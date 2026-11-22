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

    public PublicacionActividadController(PublicacionActividadService servicio, IdentidadCatalogoRepository identidades) {
        this.servicio = servicio;
        this.identidades = identidades;
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
