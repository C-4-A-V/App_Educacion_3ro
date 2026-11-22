package com.example.mpazpro.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "actividades_completas")
public class ActividadCompleta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long cursoId;
    private String asignaturaId;
    private String titulo;
    @Column(length = 1000)
    private String instrucciones;
    @Column(length = 500)
    private String pregunta;
    private String alternativaA;
    private String alternativaB;
    private String alternativaC;
    private String alternativaD;
    private Integer respuestaCorrecta;
    private Integer maxIntentos;
    @Column(length = 1000)
    private String explicacion;
    @Column(length = 500)
    private String pista;

    protected ActividadCompleta() {}

    public ActividadCompleta(ActividadCompletaRequest datos) {
        cursoId = datos.cursoId();
        asignaturaId = datos.asignaturaId();
        titulo = datos.titulo().trim();
        instrucciones = datos.instrucciones().trim();
        pregunta = datos.pregunta().trim();
        alternativaA = datos.alternativaA().trim();
        alternativaB = datos.alternativaB().trim();
        alternativaC = datos.alternativaC().trim();
        alternativaD = datos.alternativaD().trim();
        respuestaCorrecta = datos.respuestaCorrecta();
        maxIntentos = datos.maxIntentos();
        explicacion = datos.explicacion().trim();
        pista = datos.pista().trim();
    }

    boolean mismoContenido(ActividadCompleta otra) {
        return java.util.Objects.equals(cursoId, otra.cursoId)
            && java.util.Objects.equals(asignaturaId, otra.asignaturaId)
            && java.util.Objects.equals(titulo, otra.titulo)
            && java.util.Objects.equals(instrucciones, otra.instrucciones)
            && java.util.Objects.equals(pregunta, otra.pregunta)
            && java.util.Objects.equals(alternativaA, otra.alternativaA)
            && java.util.Objects.equals(alternativaB, otra.alternativaB)
            && java.util.Objects.equals(alternativaC, otra.alternativaC)
            && java.util.Objects.equals(alternativaD, otra.alternativaD)
            && java.util.Objects.equals(respuestaCorrecta, otra.respuestaCorrecta)
            && java.util.Objects.equals(maxIntentos, otra.maxIntentos)
            && java.util.Objects.equals(explicacion, otra.explicacion)
            && java.util.Objects.equals(pista, otra.pista);
    }

    public Long getId() { return id; }
    public Long getCursoId() { return cursoId; }
    public String getAsignaturaId() { return asignaturaId; }
    public String getTitulo() { return titulo; }
    public String getInstrucciones() { return instrucciones; }
    public String getPregunta() { return pregunta; }
    public String getAlternativaA() { return alternativaA; }
    public String getAlternativaB() { return alternativaB; }
    public String getAlternativaC() { return alternativaC; }
    public String getAlternativaD() { return alternativaD; }
    public Integer getRespuestaCorrecta() { return respuestaCorrecta; }
    public Integer getMaxIntentos() { return maxIntentos; }
    public String getExplicacion() { return explicacion; }
    public String getPista() { return pista; }
}
