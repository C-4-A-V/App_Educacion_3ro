package com.example.mpazpro.backend;

import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

/** Alta explícita para el catálogo local; no crea cuentas al arrancar el servidor. */
public final class AltaEstudianteLocalCli {
    private AltaEstudianteLocalCli() {}

    public static void main(String[] args) {
        String rut = requerido("MPAZ_ALTA_RUT");
        char[] pin = requerido("MPAZ_ALTA_PIN").toCharArray();
        String seccion = requerido("MPAZ_ALTA_SECCION");
        if (!seccion.matches("[A-Z]")) throw new IllegalArgumentException("Sección inválida");
        var app = new SpringApplication(MpazBackendApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setAdditionalProfiles("mysql");
        try (var contexto = app.run()) {
            var cursos = contexto.getBean(CursoCompartidoRepository.class);
            var identidades = contexto.getBean(IdentidadEstudianteAulaService.class);
            var acceso = contexto.getBean(AccesoEstudianteAulaService.class);
            var curso = cursos.findBySeccion(seccion).orElseThrow();
            var nueva = acceso.crearEstudiante(curso.getId(), rut, pin, identidades);
            if (!acceso.verificar(rut, pin).filter(nueva::equals).isPresent())
                throw new IllegalStateException("No se pudo verificar el acceso creado");
            System.out.println("Cuenta creada y credencial verificada para el curso solicitado.");
        } finally {
            Arrays.fill(pin, '\0');
        }
    }

    private static String requerido(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Falta variable " + nombre);
        return valor;
    }
}
