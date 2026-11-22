package com.example.mpazpro.backend;

import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroInstitucionService {
    private final InstitucionRepository instituciones;
    private final MiembroInstitucionRepository miembros;

    public RegistroInstitucionService(InstitucionRepository instituciones, MiembroInstitucionRepository miembros) {
        this.instituciones = instituciones;
        this.miembros = miembros;
    }

    // Preparación interna: no autentica usuarios ni debe exponerse todavía mediante HTTP.
    @Transactional
    public MiembroInstitucion registrarPendiente(Long institucionId, String correo) {
        if (correo == null || correo.isBlank() || correo.trim().length() > 254)
            throw new IllegalArgumentException("Correo requerido");
        String normalizado = correo.trim().toLowerCase(Locale.ROOT);
        Institucion institucion = instituciones.findById(institucionId).orElseThrow();
        if (miembros.existsByInstitucionIdAndCorreo(institucionId, normalizado))
            throw new IllegalArgumentException("El correo ya está registrado en esta institución");
        return miembros.save(new MiembroInstitucion(institucion, normalizado,
            MiembroInstitucion.Rol.DOCENTE, MiembroInstitucion.Estado.PENDIENTE));
    }

    // El identificador deberá provenir de una sesión autenticada, nunca de una petición sin verificar.
    @Transactional
    public void aprobarPendiente(Long administradorId, Long docenteId) {
        MiembroInstitucion administrador = miembros.findById(administradorId).orElseThrow();
        MiembroInstitucion docente = miembros.findById(docenteId).orElseThrow();
        if (administrador.getRol() != MiembroInstitucion.Rol.ADMINISTRADOR
            || administrador.getEstado() != MiembroInstitucion.Estado.ACTIVO
            || !administrador.getInstitucion().getId().equals(docente.getInstitucion().getId())) {
            throw new IllegalArgumentException("No se puede aprobar esta cuenta");
        }
        docente.activarDocente();
    }
}
