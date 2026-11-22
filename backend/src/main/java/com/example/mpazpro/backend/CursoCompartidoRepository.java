package com.example.mpazpro.backend;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoCompartidoRepository extends JpaRepository<CursoCompartido, Long> {
    List<CursoCompartido> findAllByOrderBySeccionAsc();
    Optional<CursoCompartido> findBySeccion(String seccion);
}
