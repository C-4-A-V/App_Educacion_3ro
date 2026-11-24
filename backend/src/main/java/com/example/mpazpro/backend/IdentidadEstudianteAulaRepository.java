package com.example.mpazpro.backend;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentidadEstudianteAulaRepository extends JpaRepository<IdentidadEstudianteAula, Long> {
    Optional<IdentidadEstudianteAula> findByIdentificador(String identificador);
}
