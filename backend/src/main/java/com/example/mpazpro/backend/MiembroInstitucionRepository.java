package com.example.mpazpro.backend;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MiembroInstitucionRepository extends JpaRepository<MiembroInstitucion, Long> {
    boolean existsByInstitucionIdAndCorreo(Long institucionId, String correo);
    java.util.Optional<MiembroInstitucion> findByInstitucionIdAndCorreo(Long institucionId, String correo);
}
