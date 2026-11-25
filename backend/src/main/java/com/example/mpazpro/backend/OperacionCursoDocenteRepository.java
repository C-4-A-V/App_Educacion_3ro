package com.example.mpazpro.backend;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface OperacionCursoDocenteRepository extends JpaRepository<OperacionCursoDocente, Long> {
    Optional<OperacionCursoDocente> findByDocente_IdAndClave(Long docenteId, String clave);
}
