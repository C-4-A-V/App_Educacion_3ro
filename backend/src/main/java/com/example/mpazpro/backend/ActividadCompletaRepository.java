package com.example.mpazpro.backend;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ActividadCompletaRepository extends JpaRepository<ActividadCompleta, Long> {
    List<ActividadCompleta> findByCursoIdAndAsignaturaIdOrderByIdAsc(Long cursoId, String asignaturaId);
    List<ActividadCompleta> findByCursoIdOrderByIdAsc(Long cursoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from ActividadCompleta a where a.id = :id")
    Optional<ActividadCompleta> buscarParaRespuesta(@Param("id") Long id);
}
