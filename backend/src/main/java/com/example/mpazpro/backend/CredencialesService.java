package com.example.mpazpro.backend;

import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CredencialesService {
    private final MiembroInstitucionRepository miembros;
    private final CredencialInstitucionRepository credenciales;
    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

    CredencialesService(MiembroInstitucionRepository miembros, CredencialInstitucionRepository credenciales) {
        this.miembros = miembros;
        this.credenciales = credenciales;
    }

    // Uso interno: todavía no hay registro ni autenticación expuestos por HTTP.
    @Transactional
    public void crear(Long miembroId, String clave) {
        if (clave == null || clave.length() < 8 || clave.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("La clave debe tener al menos 8 caracteres y hasta 72 bytes");
        MiembroInstitucion miembro = miembros.findById(miembroId).orElseThrow();
        if (credenciales.existsById(miembroId)) throw new IllegalStateException("La credencial ya existe");
        credenciales.save(new CredencialInstitucion(miembro, codificador.encode(clave)));
    }

    @Transactional(readOnly = true)
    public boolean verificar(Long miembroId, String clave) {
        if (miembroId == null || clave == null || clave.getBytes(StandardCharsets.UTF_8).length > 72) return false;
        MiembroInstitucion miembro = miembros.findById(miembroId).orElse(null);
        if (miembro == null || miembro.getEstado() != MiembroInstitucion.Estado.ACTIVO) return false;
        return credenciales.findById(miembroId)
            .map(credencial -> codificador.matches(clave, credencial.getClaveProtegida()))
            .orElse(false);
    }
}
