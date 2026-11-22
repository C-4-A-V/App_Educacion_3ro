package com.example.mpazpro.backend;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    List<Actividad> findByAsignaturaIdOrderByIdAsc(String asignaturaId);
    List<Actividad> findAllByOrderByIdAsc();
}
