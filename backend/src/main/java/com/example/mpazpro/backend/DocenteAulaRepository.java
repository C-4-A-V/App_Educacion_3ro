package com.example.mpazpro.backend;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
interface DocenteAulaRepository extends JpaRepository<DocenteAula, Long> {
    Optional<DocenteAula> findByCorreo(String correo);
}
