package com.example.mpazpro.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "miembros_institucion", uniqueConstraints =
    @UniqueConstraint(columnNames = {"institucion_id", "correo"}))
public class MiembroInstitucion {
    public enum Rol { ADMINISTRADOR, DOCENTE }
    public enum Estado { PENDIENTE, ACTIVO }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "institucion_id", nullable = false)
    private Institucion institucion;
    @Column(nullable = false, length = 254)
    private String correo;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Rol rol;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Estado estado;

    protected MiembroInstitucion() {}
    public MiembroInstitucion(Institucion institucion, String correo, Rol rol, Estado estado) {
        if (institucion == null || correo == null || correo.isBlank() || rol == null || estado == null)
            throw new IllegalArgumentException("Datos del miembro requeridos");
        this.institucion = institucion;
        this.correo = correo.trim().toLowerCase(java.util.Locale.ROOT);
        this.rol = rol;
        this.estado = estado;
    }
    void activarDocente() {
        if (rol != Rol.DOCENTE || estado != Estado.PENDIENTE)
            throw new IllegalStateException("El docente no está pendiente");
        estado = Estado.ACTIVO;
    }

    public Long getId() { return id; }
    public String getCorreo() { return correo; }
    public Institucion getInstitucion() { return institucion; }
    public Rol getRol() { return rol; }
    public Estado getEstado() { return estado; }
}
