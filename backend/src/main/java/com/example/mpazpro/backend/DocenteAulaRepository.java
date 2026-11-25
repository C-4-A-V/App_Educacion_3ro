package com.example.mpazpro.backend;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
interface DocenteAulaRepository extends JpaRepository<DocenteAula, Long> {
    Optional<DocenteAula> findByCorreo(String correo);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select d from DocenteAula d where d.id = :id")
    Optional<DocenteAula> bloquearPorId(@org.springframework.data.repository.query.Param("id") Long id);
}
