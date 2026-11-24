package com.example.mpazpro.backend;

public record ActividadParaEstudiante(
    Long id,
    String asignaturaId,
    String titulo,
    String instrucciones,
    String pregunta,
    String alternativaA,
    String alternativaB,
    String alternativaC,
    String alternativaD,
    Integer maxIntentos,
    String pista,
    boolean yaRespondida
) {
    public static ActividadParaEstudiante desde(ActividadCompleta actividad) {
        return desde(actividad, false);
    }

    public static ActividadParaEstudiante desde(ActividadCompleta actividad, boolean yaRespondida) {
        return new ActividadParaEstudiante(
            actividad.getId(), actividad.getAsignaturaId(), actividad.getTitulo(),
            actividad.getInstrucciones(), actividad.getPregunta(),
            actividad.getAlternativaA(), actividad.getAlternativaB(),
            actividad.getAlternativaC(), actividad.getAlternativaD(),
            actividad.getMaxIntentos(),
            actividad.getMaxIntentos() == 3 ? actividad.getPista() : "", yaRespondida
        );
    }
}
