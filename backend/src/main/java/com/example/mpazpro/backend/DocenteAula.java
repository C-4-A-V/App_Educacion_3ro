package com.example.mpazpro.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "docentes_aula")
class DocenteAula {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 254)
    private String correo;
    @Column(nullable = false, length = 128)
    private String hash;
    @Column(nullable = false, length = 64)
    private String salt;
    protected DocenteAula() {}
    DocenteAula(String correo, String hash, String salt) {
        this.correo = correo; this.hash = hash; this.salt = salt;
    }
    Long getId() { return id; }
    String getCorreo() { return correo; }
    String getHash() { return hash; }
    String getSalt() { return salt; }
}
