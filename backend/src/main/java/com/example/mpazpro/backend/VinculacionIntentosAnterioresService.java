package com.example.mpazpro.backend;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Operación interna; el origen debe verificarse antes de permitir su uso externo. */
@Service
class VinculacionIntentosAnterioresService {
    private final IdentidadEstudianteAulaRepository estudiantes;
    private final IntentoAulaRepository intentos;

    VinculacionIntentosAnterioresService(IdentidadEstudianteAulaRepository estudiantes, IntentoAulaRepository intentos) {
        this.estudiantes = estudiantes;
        this.intentos = intentos;
    }

    @Transactional
    int vincular(UUID identidadServidor, UUID identidadAnterior) {
        if (identidadServidor == null || identidadAnterior == null)
            throw new IllegalArgumentException("Identidades requeridas");
        var estudiante = estudiantes.findByIdentificador(identidadServidor.toString()).orElseThrow();
        if (identidadServidor.equals(identidadAnterior)) return 0;
        String origen = identidadAnterior.toString();
        String destino = identidadServidor.toString();
        if (intentos.countByEstudianteTokenAndActividad_CursoIdNot(origen, estudiante.getCurso().getId()) != 0)
            throw new IllegalStateException("Los intentos pertenecen a otro curso");
        if (intentos.existsByEstudianteToken(destino))
            throw new IllegalStateException("La identidad de destino ya tiene intentos");
        return intentos.reasignarIdentidad(origen, destino);
    }
}
