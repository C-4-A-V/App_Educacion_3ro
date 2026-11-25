package com.example.mpazpro.backend;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
interface CursoDocenteAulaRepository extends JpaRepository<CursoDocenteAula, Long> {
    boolean existsByDocente_IdAndCurso_Id(Long docenteId, Long cursoId);
    List<CursoDocenteAula> findByDocente_IdOrderByCurso_SeccionAsc(Long docenteId);
}
