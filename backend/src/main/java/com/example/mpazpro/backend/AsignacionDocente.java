package com.example.mpazpro.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "asignaciones_docente", uniqueConstraints =
    @UniqueConstraint(columnNames = {"miembro_id", "curso_id", "asignatura"}))
public class AsignacionDocente {
    public enum Asignatura { LENGUAJE, MATEMATICA, CIENCIAS, HISTORIA }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "miembro_id", nullable = false)
    private MiembroInstitucion docente;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private CursoInstitucion curso;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Asignatura asignatura;

    protected AsignacionDocente() {}
    AsignacionDocente(MiembroInstitucion docente, CursoInstitucion curso, Asignatura asignatura) {
        this.docente = docente;
        this.curso = curso;
        this.asignatura = asignatura;
    }
    public Long getId() { return id; }
}
