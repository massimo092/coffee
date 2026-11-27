package com.safareyes.cafe.repository;

import com.safareyes.cafe.modelo.Cupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface cuponRepository extends JpaRepository<Cupon, Integer> {

    Optional<Cupon> findByCodigo(String codigo);

    Optional<Cupon> findByCodigoIgnoreCase(String codigo);

    @Modifying
    @Query("UPDATE Cupon c SET c.usosActuales = COALESCE(c.usosActuales, 0) + 1 " +
            "WHERE c.id = :id AND (c.usosMaximos IS NULL OR " +
            "COALESCE(c.usosActuales, 0) < c.usosMaximos)")
    int incrementarUsoSiDisponible(@Param("id") Integer id);

    @Modifying
    @Query("UPDATE Cupon c SET c.usosActuales = " +
            "CASE WHEN COALESCE(c.usosActuales, 0) > 0 " +
            "THEN COALESCE(c.usosActuales, 0) - 1 ELSE 0 END " +
            "WHERE c.id = :id")
    int devolverUso(@Param("id") Integer id);
}
