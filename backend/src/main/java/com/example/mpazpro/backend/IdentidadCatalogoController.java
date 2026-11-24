package com.example.mpazpro.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdentidadCatalogoController {
    private final IdentidadCatalogoRepository repositorio;
    private final String urlBase;
    public record Respuesta(String identificador, boolean persistente) {}

    public IdentidadCatalogoController(IdentidadCatalogoRepository repositorio,
                                        @Value("${spring.datasource.url}") String urlBase) {
        this.repositorio = repositorio;
        this.urlBase = urlBase;
    }

    @GetMapping("/api/catalogo/identidad")
    public Respuesta identidad() {
        boolean persistente = urlBase.startsWith("jdbc:h2:file:") || urlBase.startsWith("jdbc:postgresql:") || urlBase.startsWith("jdbc:mysql:");
        return new Respuesta(repositorio.findById(1L).orElseThrow().getIdentificador(), persistente);
    }
}
