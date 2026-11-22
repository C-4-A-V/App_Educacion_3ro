package com.example.mpazpro.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Conserva la clave para impedir que un reintento vuelva a publicar una pregunta retirada. */
@Entity
@Table(name = "publicaciones_retiradas")
public class PublicacionRetirada {
    @Id @Column(length = 36)
    private String clave;
    private Long cursoId;

    protected PublicacionRetirada() {}
    public PublicacionRetirada(String clave, Long cursoId) {
        this.clave = clave;
        this.cursoId = cursoId;
    }
    public Long getCursoId() { return cursoId; }
}
