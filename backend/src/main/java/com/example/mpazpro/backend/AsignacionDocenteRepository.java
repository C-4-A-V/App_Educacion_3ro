package com.example.mpazpro.backend;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AsignacionDocenteRepository extends JpaRepository<AsignacionDocente, Long> {
    boolean existsByDocenteIdAndCursoIdAndAsignatura(Long docenteId, Long cursoId, AsignacionDocente.Asignatura asignatura);
}
