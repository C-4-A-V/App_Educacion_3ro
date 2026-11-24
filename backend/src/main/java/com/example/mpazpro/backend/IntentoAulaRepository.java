package com.example.mpazpro.backend;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntentoAulaRepository extends JpaRepository<IntentoAula, Long> {
    boolean existsByActividadId(Long actividadId);
    Optional<IntentoAula> findByEstudianteTokenAndActividadIdAndNumero(String token, Long actividadId, int numero);
    Optional<IntentoAula> findTopByEstudianteTokenAndActividadIdOrderByNumeroDesc(String token, Long actividadId);
}
