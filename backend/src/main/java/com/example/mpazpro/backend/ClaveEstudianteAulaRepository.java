package com.example.mpazpro.backend;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface ClaveEstudianteAulaRepository extends JpaRepository<ClaveEstudianteAula, String> {
    Optional<ClaveEstudianteAula> findByRut(String rut);
}
