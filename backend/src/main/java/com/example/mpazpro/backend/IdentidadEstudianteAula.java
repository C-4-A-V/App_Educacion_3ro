package com.example.mpazpro.backend;

import jakarta.persistence.*;
import java.util.UUID;

/** Identidad estable del aula; no contiene datos personales ni credenciales. */
@Entity
@Table(name = "identidades_estudiante_aula", uniqueConstraints =
    @UniqueConstraint(columnNames = "identificador"))
public class IdentidadEstudianteAula {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36, updatable = false)
    private String identificador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private CursoCompartido curso;

    protected IdentidadEstudianteAula() {}

    IdentidadEstudianteAula(UUID identificador, CursoCompartido curso) {
        if (identificador == null || curso == null) throw new IllegalArgumentException("Identidad y curso requeridos");
        this.identificador = identificador.toString();
        this.curso = curso;
    }

    public Long getId() { return id; }
    public String getIdentificador() { return identificador; }
    public CursoCompartido getCurso() { return curso; }
}
