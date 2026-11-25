package com.example.mpazpro.backend;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntentoAulaRepository extends JpaRepository<IntentoAula, Long> {
    boolean existsByActividadId(Long actividadId);
    boolean existsByEstudianteToken(String estudianteToken);
    long countByEstudianteTokenAndActividad_CursoIdNot(String estudianteToken, Long cursoId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("update IntentoAula i set i.estudianteToken = :destino where i.estudianteToken = :origen")
    int reasignarIdentidad(@org.springframework.data.repository.query.Param("origen") String origen,
                           @org.springframework.data.repository.query.Param("destino") String destino);
    boolean existsByEstudianteTokenAndActividadId(String token, Long actividadId);
    Optional<IntentoAula> findByEstudianteTokenAndActividadIdAndNumero(String token, Long actividadId, int numero);
    Optional<IntentoAula> findTopByEstudianteTokenAndActividadIdOrderByNumeroDesc(String token, Long actividadId);
}
