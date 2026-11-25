package com.example.mpazpro.backend;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Base interna de credenciales; todavía no expone un acceso por HTTP. */
@Service
class AccesoEstudianteAulaService {
    private static final int ITERACIONES = 600_000;
    private final ClaveEstudianteAulaRepository claves;
    private final IdentidadEstudianteAulaRepository identidades;
    private final SecureRandom aleatorio = new SecureRandom();

    AccesoEstudianteAulaService(ClaveEstudianteAulaRepository claves, IdentidadEstudianteAulaRepository identidades) {
        this.claves = claves;
        this.identidades = identidades;
    }

    /** Alta interna: no modifica cuentas existentes ni se expone por HTTP. */
    @Transactional
    UUID crearEstudiante(Long cursoId, String rut, char[] pin, IdentidadEstudianteAulaService creador) {
        String normalizado = normalizarRut(rut);
        validarPin(pin);
        if (claves.findByRut(normalizado).isPresent())
            throw new IllegalArgumentException("Ya existe una cuenta con ese RUT");
        UUID identidad = creador.crear(cursoId);
        registrar(identidad, normalizado, pin);
        return identidad;
    }

    @Transactional
    void registrar(UUID identidad, String rut, char[] pin) {
        String normalizado = normalizarRut(rut);
        validarPin(pin);
        if (identidad == null || identidades.findByIdentificador(identidad.toString()).isEmpty())
            throw new IllegalArgumentException("Identidad inexistente");
        if (claves.existsById(identidad.toString()) || claves.findByRut(normalizado).isPresent())
            throw new IllegalArgumentException("Credencial ya registrada");
        byte[] salt = new byte[16];
        aleatorio.nextBytes(salt);
        byte[] hash = derivar(pin, salt);
        claves.saveAndFlush(new ClaveEstudianteAula(identidad.toString(), normalizado,
            HexFormat.of().formatHex(hash), HexFormat.of().formatHex(salt)));
    }

    @Transactional(readOnly = true)
    Optional<UUID> verificar(String rut, char[] pin) {
        String normalizado;
        try {
            normalizado = normalizarRut(rut);
            validarPin(pin);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
        return claves.findByRut(normalizado).filter(clave ->
            MessageDigest.isEqual(HexFormat.of().parseHex(clave.getHash()),
                derivar(pin, HexFormat.of().parseHex(clave.getSalt()))))
            .map(clave -> UUID.fromString(clave.getIdentidad()));
    }

    private static String normalizarRut(String rut) {
        if (rut == null) throw new IllegalArgumentException("RUT requerido");
        String valor = rut.trim().toUpperCase(Locale.ROOT).replace(".", "");
        if (!valor.matches("[0-9]{7,8}-[0-9K]")) throw new IllegalArgumentException("RUT inválido");
        return valor;
    }

    private static void validarPin(char[] pin) {
        if (pin == null || pin.length != 4) throw new IllegalArgumentException("PIN inválido");
        for (char c : pin) if (c < '0' || c > '9') throw new IllegalArgumentException("PIN inválido");
    }

    private static byte[] derivar(char[] pin, byte[] salt) {
        PBEKeySpec especificacion = new PBEKeySpec(pin, salt, ITERACIONES, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo verificar la credencial", ex);
        } finally {
            especificacion.clearPassword();
        }
    }
}
