package com.example.mpazpro.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "cursos_compartidos")
public class CursoCompartido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 1)
    private String seccion;

    protected CursoCompartido() {}
    public CursoCompartido(String seccion) {
        if (seccion == null || !seccion.matches("[A-Z]")) throw new IllegalArgumentException("Sección inválida");
        this.seccion = seccion;
    }

    public Long getId() { return id; }
    public String getSeccion() { return seccion; }
    public String getNombre() { return "3° " + seccion; }
}
