package com.example.mpazpro.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "claves_estudiante_aula")
class ClaveEstudianteAula {
    @Id
    @Column(length = 36)
    private String identidad;

    @Column(nullable = false, unique = true, length = 12)
    private String rut;

    @Column(nullable = false, length = 128)
    private String hash;

    @Column(nullable = false, length = 64)
    private String salt;

    protected ClaveEstudianteAula() {}

    ClaveEstudianteAula(String identidad, String rut, String hash, String salt) {
        this.identidad = identidad;
        this.rut = rut;
        this.hash = hash;
        this.salt = salt;
    }

    String getIdentidad() { return identidad; }
    String getHash() { return hash; }
    String getSalt() { return salt; }
}
