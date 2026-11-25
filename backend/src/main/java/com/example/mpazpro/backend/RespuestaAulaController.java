package com.example.mpazpro.backend;

import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// Solo para el aula local de prueba: el token no sustituye una autenticación real.
@Profile("aula-local & !aula-autenticada")
@RestController
@RequestMapping("/api/aula/respuestas")
public class RespuestaAulaController {
    private final RespuestaAulaService servicio;
    private final IdentidadCatalogoRepository identidades;

    public RespuestaAulaController(RespuestaAulaService servicio, IdentidadCatalogoRepository identidades) {
        this.servicio = servicio;
        this.identidades = identidades;
    }

    @PostMapping
    public RespuestaAulaService.Resultado responder(@RequestHeader("X-Catalogo-Id") String catalogoId,
                                                     @Valid @RequestBody RespuestaAulaRequest solicitud) {
        validarCatalogo(catalogoId);
        return servicio.responder(solicitud);
    }

    @PostMapping("/estado")
    public ResponseEntity<RespuestaAulaService.Resultado> estado(@RequestHeader("X-Catalogo-Id") String catalogoId,
                                                                 @Valid @RequestBody EstadoRespuestaAulaRequest solicitud) {
        validarCatalogo(catalogoId);
        return servicio.estado(solicitud).map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private void validarCatalogo(String catalogoId) {
        if (!identidades.findById(1L).orElseThrow().getIdentificador().equals(catalogoId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Catálogo incorrecto");
    }
}
