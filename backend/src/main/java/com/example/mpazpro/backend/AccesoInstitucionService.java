package com.example.mpazpro.backend;

import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccesoInstitucionService {
    private final MiembroInstitucionRepository miembros;
    private final CredencialesService credenciales;

    public record Identidad(Long miembroId, Long institucionId, MiembroInstitucion.Rol rol) {}

    public AccesoInstitucionService(MiembroInstitucionRepository miembros, CredencialesService credenciales) {
        this.miembros = miembros;
        this.credenciales = credenciales;
    }

    // Primitiva interna: no constituye una sesión ni protege los endpoints actuales.
    @Transactional(readOnly = true)
    public Optional<Identidad> verificar(Long institucionId, String correo, String clave) {
        if (institucionId == null || correo == null || correo.isBlank() || clave == null) return Optional.empty();
        return miembros.findByInstitucionIdAndCorreo(institucionId, correo.trim().toLowerCase(Locale.ROOT))
            .filter(miembro -> credenciales.verificar(miembro.getId(), clave))
            .map(miembro -> new Identidad(miembro.getId(), miembro.getInstitucion().getId(), miembro.getRol()));
    }
}
