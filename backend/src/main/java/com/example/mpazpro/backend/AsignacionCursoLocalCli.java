package com.example.mpazpro.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

/** Herramienta administrativa local explícita; nunca se invoca desde HTTP. */
public final class AsignacionCursoLocalCli {
    private AsignacionCursoLocalCli() {}
    public static void main(String[] args) {
        if (args.length != 2) throw new IllegalArgumentException("Uso: correo sección");
        var app = new SpringApplication(MpazBackendApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setAdditionalProfiles("mysql");
        try (var contexto = app.run()) {
            contexto.getBean(AsignacionCursoDocenteService.class).asignarExistente(args[0], args[1]);
            System.out.println("Asignación registrada.");
        }
    }
}
