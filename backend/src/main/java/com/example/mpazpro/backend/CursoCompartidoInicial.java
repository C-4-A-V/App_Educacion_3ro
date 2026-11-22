package com.example.mpazpro.backend;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CursoCompartidoInicial implements ApplicationRunner {
    private final CursoCompartidoRepository cursos;
    private final IdentidadCatalogoRepository identidades;

    public CursoCompartidoInicial(CursoCompartidoRepository cursos, IdentidadCatalogoRepository identidades) {
        this.cursos = cursos;
        this.identidades = identidades;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!identidades.existsById(1L)) identidades.save(IdentidadCatalogo.crear());
        if (cursos.findBySeccion("A").isEmpty()) cursos.save(new CursoCompartido("A"));
    }
}
