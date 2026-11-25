package com.example.mpazpro.backend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.HashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Prototipo local. La limitación en memoria no sirve para producción distribuida. */
@Profile("sesion-prueba")
@RestController
@RequestMapping("/api/sesion-estudiante-prueba")
class SesionEstudiantePruebaController {
    private static final int MAX_INTENTOS = 5;
    private static final int MAX_ORIGENES = 1000;
    private final AccesoEstudianteAulaService acceso;
    private final Map<String, Deque<Instant>> intentos = new HashMap<>();
    private final Clock reloj = Clock.systemUTC();

    SesionEstudiantePruebaController(AccesoEstudianteAulaService acceso) {
        this.acceso = acceso;
    }

    record Solicitud(@NotBlank @Size(max = 32) String rut, @NotBlank @Size(max = 4) String pin) {}
    record Estado(boolean autenticado) {}

    @PostMapping("/login")
    Estado login(@Valid @RequestBody Solicitud solicitud, HttpServletRequest request) {
        var anterior = request.getSession(false);
        if (anterior != null) anterior.invalidate();
        limitar(request.getRemoteAddr());
        char[] pin = solicitud.pin().toCharArray();
        try {
            var identidad = acceso.verificar(solicitud.rut(), pin)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));
            var sesionPrevia = request.getSession(false);
            if (sesionPrevia != null) sesionPrevia.invalidate();
            var sesion = request.getSession(true);
            sesion.setMaxInactiveInterval(900);
            sesion.setAttribute("estudianteId", identidad.toString());
            return new Estado(true);
        } finally {
            Arrays.fill(pin, '\0');
        }
    }

    @GetMapping
    Estado estado(HttpServletRequest request) {
        var sesion = request.getSession(false);
        if (sesion == null || !(sesion.getAttribute("estudianteId") instanceof String))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return new Estado(true);
    }

    @PostMapping("/logout")
    void logout(HttpServletRequest request) {
        var sesion = request.getSession(false);
        if (sesion != null) sesion.invalidate();
    }

    private synchronized void limitar(String origen) {
        Instant limite = reloj.instant().minus(Duration.ofMinutes(1));
        intentos.values().forEach(lista -> {
            while (!lista.isEmpty() && !lista.peekFirst().isAfter(limite)) lista.removeFirst();
        });
        intentos.entrySet().removeIf(e -> e.getValue().isEmpty());
        if (!intentos.containsKey(origen) && intentos.size() >= MAX_ORIGENES)
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        Deque<Instant> lista = intentos.computeIfAbsent(origen, k -> new ArrayDeque<>());
        if (lista.size() >= MAX_INTENTOS) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        lista.addLast(reloj.instant());
    }
}
