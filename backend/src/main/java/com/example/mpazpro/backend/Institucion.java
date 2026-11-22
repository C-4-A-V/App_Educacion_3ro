package com.example.mpazpro.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "instituciones")
public class Institucion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String nombre;

    protected Institucion() {}
    public Institucion(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        this.nombre = nombre.trim();
    }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}
