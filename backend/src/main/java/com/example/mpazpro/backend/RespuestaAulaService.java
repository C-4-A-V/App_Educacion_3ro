package com.example.mpazpro.backend;

import java.util.UUID;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RespuestaAulaService {
    private final ActividadCompletaRepository actividades;
    private final IntentoAulaRepository intentos;
    private final PublicacionActividadRepository publicaciones;

    public record Resultado(int numero, int respuestaElegida, boolean correcta, boolean finalizada,
                            Integer respuestaCorrecta, String explicacion) {}

    public RespuestaAulaService(ActividadCompletaRepository actividades, IntentoAulaRepository intentos,
                                PublicacionActividadRepository publicaciones) {
        this.actividades = actividades;
        this.intentos = intentos;
        this.publicaciones = publicaciones;
    }

    @Transactional
    public Resultado responder(RespuestaAulaRequest datos) {
        validarToken(datos.estudianteToken());
        ActividadCompleta actividad = actividades.buscarParaRespuesta(datos.actividadId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!actividad.getCursoId().equals(datos.cursoId()) || !publicaciones.estaActiva(actividad.getId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var repetido = intentos.findByEstudianteTokenAndActividadIdAndNumero(
            datos.estudianteToken(), actividad.getId(), datos.numero());
        if (repetido.isPresent()) {
            var anterior = repetido.get();
            if (anterior.getRespuestaElegida() != datos.respuestaElegida())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El intento ya existe");
            return resultado(anterior, actividad);
        }
        var ultimo = intentos.findTopByEstudianteTokenAndActividadIdOrderByNumeroDesc(
            datos.estudianteToken(), actividad.getId());
        int esperado = ultimo.map(i -> i.getNumero() + 1).orElse(1);
        if (ultimo.map(IntentoAula::isFinalizada).orElse(false)
            || datos.numero() != esperado || datos.numero() > actividad.getMaxIntentos())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede registrar este intento");
        boolean correcta = datos.respuestaElegida().equals(actividad.getRespuestaCorrecta());
        boolean finalizada = correcta || datos.numero().equals(actividad.getMaxIntentos());
        var intento = intentos.save(new IntentoAula(datos.estudianteToken(), actividad, datos.numero(),
            datos.respuestaElegida(), correcta, finalizada));
        return resultado(intento, actividad);
    }

    @Transactional(readOnly = true)
    public Optional<Resultado> estado(EstadoRespuestaAulaRequest datos) {
        validarToken(datos.estudianteToken());
        ActividadCompleta actividad = actividades.findById(datos.actividadId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!actividad.getCursoId().equals(datos.cursoId()) || !publicaciones.estaActiva(actividad.getId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return intentos.findTopByEstudianteTokenAndActividadIdOrderByNumeroDesc(
            datos.estudianteToken(), actividad.getId()).map(intento -> resultado(intento, actividad));
    }

    private void validarToken(String token) {
        try {
            if (token == null || !UUID.fromString(token).toString().equals(token)) throw new IllegalArgumentException();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador inválido");
        }
    }

    private Resultado resultado(IntentoAula intento, ActividadCompleta actividad) {
        return new Resultado(intento.getNumero(), intento.getRespuestaElegida(), intento.isCorrecta(), intento.isFinalizada(),
            intento.isFinalizada() ? actividad.getRespuestaCorrecta() : null,
            intento.isFinalizada() ? actividad.getExplicacion() : null);
    }
}
