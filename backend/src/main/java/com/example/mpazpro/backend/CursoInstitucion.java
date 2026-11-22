package com.example.mpazpro.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "cursos_institucion", uniqueConstraints =
    @UniqueConstraint(columnNames = {"institucion_id", "nombre"}))
public class CursoInstitucion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "institucion_id", nullable = false)
    private Institucion institucion;
    @Column(nullable = false, length = 80)
    private String nombre;

    protected CursoInstitucion() {}
    public CursoInstitucion(Institucion institucion, String nombre) {
        if (institucion == null || nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Institución y nombre requeridos");
        this.institucion = institucion;
        this.nombre = nombre.trim();
    }
    public Long getId() { return id; }
    public Institucion getInstitucion() { return institucion; }
    public String getNombre() { return nombre; }
}
