package com.example.mpazpro.backend;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PublicacionActividadService {
    private final PublicacionActividadRepository publicaciones;
    private final ActividadCompletaRepository actividades;
    private final CursoCompartidoRepository cursos;
    private final PublicacionRetiradaRepository retiradas;

    public record Resultado(Long actividadId, boolean creada) {}

    public PublicacionActividadService(PublicacionActividadRepository publicaciones,
                                        ActividadCompletaRepository actividades,
                                        CursoCompartidoRepository cursos, PublicacionRetiradaRepository retiradas) {
        this.publicaciones = publicaciones;
        this.actividades = actividades;
        this.cursos = cursos;
        this.retiradas = retiradas;
    }

    @Transactional
    public synchronized void retirar(Long cursoId, java.util.List<String> claves) {
        if (cursoId == null || !cursos.existsById(cursoId) || claves == null || claves.isEmpty() ||
            claves.size() > 200 || claves.stream().distinct().count() != claves.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selección inválida");
        for (String clave : claves) {
            try {
                if (clave == null || !UUID.fromString(clave).toString().equals(clave)) throw new IllegalArgumentException();
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clave inválida");
            }
            var retirada = retiradas.findById(clave);
            if (retirada.isPresent() && !retirada.get().getCursoId().equals(cursoId))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La clave pertenece a otro curso");
            var publicacion = publicaciones.findById(clave);
            if (publicacion.isPresent() && !publicacion.get().getActividad().getCursoId().equals(cursoId))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        for (String clave : claves) {
            if (!retiradas.existsById(clave)) retiradas.save(new PublicacionRetirada(clave, cursoId));
        }
    }

    @Transactional
    public synchronized Resultado publicar(String clave, ActividadCompletaRequest solicitud) {
        try {
            if (clave == null || !UUID.fromString(clave).toString().equals(clave)) throw new IllegalArgumentException();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clave de publicación inválida");
        }
        if (retiradas.existsById(clave))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La pregunta fue retirada");
        ActividadCompleta candidata = new ActividadCompleta(solicitud);
        var previa = publicaciones.findById(clave);
        if (previa.isPresent()) {
            ActividadCompleta guardada = previa.get().getActividad();
            if (!guardada.mismoContenido(candidata))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La clave ya corresponde a otra actividad");
            return new Resultado(guardada.getId(), false);
        }
        if (!cursos.existsById(solicitud.cursoId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no disponible");
        ActividadCompleta guardada = actividades.save(candidata);
        publicaciones.saveAndFlush(new PublicacionActividad(clave, guardada));
        return new Resultado(guardada.getId(), true);
    }
}
