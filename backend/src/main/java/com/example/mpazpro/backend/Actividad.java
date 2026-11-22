package com.example.mpazpro.backend;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "actividades")
public class Actividad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String asignaturaId;
    private String titulo;
    private String pregunta;

    protected Actividad() {
    }

    public Actividad(String asignaturaId, String titulo, String pregunta) {
        this.asignaturaId = asignaturaId;
        this.titulo = titulo;
        this.pregunta = pregunta;
    }

    public void actualizar(String asignaturaId, String titulo, String pregunta) {
        this.asignaturaId = asignaturaId;
        this.titulo = titulo;
        this.pregunta = pregunta;
    }

    public Long getId() { return id; }
    public String getAsignaturaId() { return asignaturaId; }
    public String getTitulo() { return titulo; }
    public String getPregunta() { return pregunta; }
}
