package com.example.mpazpro.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "identidad_catalogo")
public class IdentidadCatalogo {
    @Id
    private Long id;
    @Column(nullable = false, unique = true, length = 36, updatable = false)
    private String identificador;

    protected IdentidadCatalogo() {}
    public static IdentidadCatalogo crear() {
        IdentidadCatalogo identidad = new IdentidadCatalogo();
        identidad.id = 1L;
        identidad.identificador = UUID.randomUUID().toString();
        return identidad;
    }
    public String getIdentificador() { return identificador; }
}
