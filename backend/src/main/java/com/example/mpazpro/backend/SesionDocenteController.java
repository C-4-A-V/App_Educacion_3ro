package com.example.mpazpro.backend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Profile("aula-autenticada")
@RestController
@RequestMapping("/api/docentes/sesion")
class SesionDocenteController {
    private final AccesoDocenteAulaService acceso;
    private final DocenteAulaRepository docentes;
    private final ConcurrentHashMap<String, Ventana> intentos = new ConcurrentHashMap<>();
    private record Ventana(long inicio, AtomicInteger cantidad) {}

    SesionDocenteController(AccesoDocenteAulaService acceso, DocenteAulaRepository docentes) {
        this.acceso = acceso; this.docentes = docentes;
    }

    record Credenciales(@NotBlank @Email @Size(max = 254) String correo,
                        @NotBlank @Size(min = 10, max = 128) String clave) {}
    record Perfil(Long id, String correo) {}

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    Perfil registrar(@Valid @RequestBody Credenciales datos, HttpServletRequest request) {
        limitar(request.getRemoteAddr());
        char[] clave = datos.clave().toCharArray();
        try {
            var docente = acceso.registrar(datos.correo(), clave);
            return new Perfil(docente.getId(), docente.getCorreo());
        }
        finally { Arrays.fill(clave, '\0'); }
    }

    @PostMapping("/login")
    Perfil login(@Valid @RequestBody Credenciales datos, HttpServletRequest request) {
        var anterior = request.getSession(false);
        if (anterior != null) anterior.invalidate();
        limitar(request.getRemoteAddr());
        char[] clave = datos.clave().toCharArray();
        try {
            var docente = acceso.autenticar(datos.correo(), clave);
            var sesion = request.getSession(true);
            sesion.setMaxInactiveInterval(900);
            sesion.setAttribute("docenteAulaId", docente.getId());
            return new Perfil(docente.getId(), docente.getCorreo());
        } finally { Arrays.fill(clave, '\0'); }
    }

    @GetMapping
    Perfil actual(HttpServletRequest request) {
        var docente = docentes.findById(docenteId(request))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return new Perfil(docente.getId(), docente.getCorreo());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void salir(HttpServletRequest request) {
        docenteId(request);
        request.getSession(false).invalidate();
    }

    static Long docenteId(HttpServletRequest request) {
        var sesion = request.getSession(false);
        if (sesion == null || !(sesion.getAttribute("docenteAulaId") instanceof Long id))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return id;
    }

    private synchronized void limitar(String origen) {
        long ahora = System.currentTimeMillis();
        intentos.entrySet().removeIf(e -> ahora - e.getValue().inicio() >= 60_000);
        if (!intentos.containsKey(origen) && intentos.size() >= 1000)
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        var ventana = intentos.computeIfAbsent(origen, k -> new Ventana(ahora, new AtomicInteger()));
        if (ventana.cantidad().incrementAndGet() > 10)
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
    }
}
