package com.example.mpazpro.backend;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicacionActividadRepository extends JpaRepository<PublicacionActividad, String> {
    boolean existsByActividadId(Long actividadId);

    @org.springframework.data.jpa.repository.Query("""
        select count(p) > 0 from PublicacionActividad p
        where p.actividad.id = :actividadId
          and not exists (select r.clave from PublicacionRetirada r where r.clave = p.clave)
        """)
    boolean estaActiva(@org.springframework.data.repository.query.Param("actividadId") Long actividadId);
}
