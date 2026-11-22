package com.example.mpazpro.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "publicaciones_actividad")
public class PublicacionActividad {
    @Id @Column(length = 36, nullable = false, updatable = false)
    private String clave;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id", nullable = false, unique = true)
    private ActividadCompleta actividad;

    protected PublicacionActividad() {}
    PublicacionActividad(String clave, ActividadCompleta actividad) {
        this.clave = clave;
        this.actividad = actividad;
    }
    public ActividadCompleta getActividad() { return actividad; }
}
