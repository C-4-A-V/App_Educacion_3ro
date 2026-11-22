package com.example.mpazpro.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "intentos_aula", uniqueConstraints =
    @UniqueConstraint(columnNames = {"estudiante_token", "actividad_id", "numero"}))
public class IntentoAula {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "estudiante_token", nullable = false, length = 36)
    private String estudianteToken;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id", nullable = false)
    private ActividadCompleta actividad;
    @Column(nullable = false)
    private int numero;
    @Column(nullable = false)
    private int respuestaElegida;
    @Column(nullable = false)
    private boolean correcta;
    @Column(nullable = false)
    private boolean finalizada;

    protected IntentoAula() {}

    IntentoAula(String estudianteToken, ActividadCompleta actividad, int numero,
                int respuestaElegida, boolean correcta, boolean finalizada) {
        this.estudianteToken = estudianteToken;
        this.actividad = actividad;
        this.numero = numero;
        this.respuestaElegida = respuestaElegida;
        this.correcta = correcta;
        this.finalizada = finalizada;
    }
    int getNumero() { return numero; }
    int getRespuestaElegida() { return respuestaElegida; }
    boolean isCorrecta() { return correcta; }
    boolean isFinalizada() { return finalizada; }
}
