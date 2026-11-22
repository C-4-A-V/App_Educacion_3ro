package com.example.mpazpro.backend;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntentoAulaRepository extends JpaRepository<IntentoAula, Long> {
    Optional<IntentoAula> findByEstudianteTokenAndActividadIdAndNumero(String token, Long actividadId, int numero);
    Optional<IntentoAula> findTopByEstudianteTokenAndActividadIdOrderByNumeroDesc(String token, Long actividadId);
}
