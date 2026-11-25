package com.example.mpazpro.backend;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
class AccesoDocenteAulaService {
    private final DocenteAulaRepository docentes;
    private final SecureRandom random = new SecureRandom();

    AccesoDocenteAulaService(DocenteAulaRepository docentes) { this.docentes = docentes; }

    @Transactional
    DocenteAula registrar(String correo, char[] clave) {
        String normal = correo(correo);
        if (clave == null || clave.length < 10 || clave.length > 128)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contraseña inválida");
        if (docentes.findByCorreo(normal).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cuenta ya registrada");
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        try {
            return docentes.saveAndFlush(new DocenteAula(normal, HexFormat.of().formatHex(derivar(clave, salt)),
                HexFormat.of().formatHex(salt)));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cuenta ya registrada");
        }
    }

    @Transactional(readOnly = true)
    DocenteAula autenticar(String correo, char[] clave) {
        String normal = correo(correo);
        var docente = docentes.findByCorreo(normal)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));
        if (clave == null || !MessageDigest.isEqual(HexFormat.of().parseHex(docente.getHash()),
                derivar(clave, HexFormat.of().parseHex(docente.getSalt()))))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        return docente;
    }

    private static String correo(String valor) {
        if (valor == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo inválido");
        String normal = valor.trim().toLowerCase(Locale.ROOT);
        if (normal.length() > 254 || !normal.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo inválido");
        return normal;
    }

    private static byte[] derivar(char[] clave, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(clave, salt, 600_000, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (GeneralSecurityException ex) { throw new IllegalStateException(ex); }
        finally { spec.clearPassword(); }
    }
}
