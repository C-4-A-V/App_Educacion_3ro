package com.example.mpazpro.backend;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.context.annotation.Profile("!aula-autenticada")
@RestController
@RequestMapping("/api/cursos-compartidos")
public class CursoCompartidoController {
    private final CursoCompartidoRepository cursos;
    private final IdentidadCatalogoRepository identidades;

    public CursoCompartidoController(CursoCompartidoRepository cursos, IdentidadCatalogoRepository identidades) {
        this.cursos = cursos;
        this.identidades = identidades;
    }

    @GetMapping
    public List<CursoCompartido> listar() {
        return cursos.findAllByOrderBySeccionAsc();
    }

    /** Idempotente: si ya existe la sección devuelve el mismo curso. */
    @PostMapping("/{seccion}/asegurar")
    public synchronized ResponseEntity<CursoCompartido> asegurar(
        @PathVariable("seccion") String seccion, @RequestHeader("X-Catalogo-Id") String catalogoId
    ) {
        if (seccion == null || !seccion.matches("[A-Z]"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sección inválida");
        if (!identidades.findById(1L).orElseThrow().getIdentificador().equals(catalogoId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Catálogo incorrecto");
        var existente = cursos.findBySeccion(seccion);
        if (existente.isPresent()) return ResponseEntity.ok(existente.get());
        var creado = cursos.saveAndFlush(new CursoCompartido(seccion));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/siguiente")
    public synchronized ResponseEntity<CursoCompartido> crearSiguiente(@RequestParam(name = "seccionEsperada", required = false) String seccionEsperada) {
        var existentes = cursos.findAllByOrderBySeccionAsc();
        for (char letra = 'A'; letra <= 'Z'; letra++) {
            String seccion = Character.toString(letra);
            if (existentes.stream().noneMatch(curso -> curso.getSeccion().equals(seccion))) {
                if (seccionEsperada != null && !seccion.equals(seccionEsperada))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El siguiente curso cambió; revisa la lista");
                CursoCompartido guardado = cursos.saveAndFlush(new CursoCompartido(seccion));
                return ResponseEntity.created(URI.create("/api/cursos-compartidos")).body(guardado);
            }
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "No quedan secciones disponibles");
    }
}
