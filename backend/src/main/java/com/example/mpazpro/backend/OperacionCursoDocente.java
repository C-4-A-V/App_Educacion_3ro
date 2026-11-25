package com.example.mpazpro.backend;

import jakarta.persistence.*;

/** Resultado persistente de una solicitud de creación: nunca almacena contraseñas. */
@Entity
@Table(name = "operaciones_curso_docente",
    uniqueConstraints = @UniqueConstraint(columnNames = {"docente_id", "clave"}))
class OperacionCursoDocente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private DocenteAula docente;

    @Column(nullable = false, length = 36)
    private String clave;

    @Column(nullable = false, length = 1)
    private String seccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private CursoCompartido curso;

    protected OperacionCursoDocente() {}

    OperacionCursoDocente(DocenteAula docente, String clave, String seccion, CursoCompartido curso) {
        this.docente = docente;
        this.clave = clave;
        this.seccion = seccion;
        this.curso = curso;
    }

    String getSeccion() { return seccion; }
    CursoCompartido getCurso() { return curso; }
}
