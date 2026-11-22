package com.example.mpazpro.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "credenciales_institucion")
public class CredencialInstitucion {
    @Id
    private Long miembroId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "miembro_id")
    private MiembroInstitucion miembro;

    @Column(nullable = false, length = 100)
    private String claveProtegida;

    protected CredencialInstitucion() {}

    CredencialInstitucion(MiembroInstitucion miembro, String claveProtegida) {
        this.miembro = miembro;
        this.claveProtegida = claveProtegida;
    }

    String getClaveProtegida() { return claveProtegida; }
}
