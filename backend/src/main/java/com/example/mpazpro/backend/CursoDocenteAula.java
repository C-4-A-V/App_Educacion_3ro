package com.example.mpazpro.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "cursos_docentes_aula", uniqueConstraints = @UniqueConstraint(columnNames = {"docente_id", "curso_id"}))
class CursoDocenteAula {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private DocenteAula docente;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private CursoCompartido curso;
    protected CursoDocenteAula() {}
    CursoDocenteAula(DocenteAula docente, CursoCompartido curso) {
        this.docente = docente; this.curso = curso;
    }
    CursoCompartido getCurso() { return curso; }
}
