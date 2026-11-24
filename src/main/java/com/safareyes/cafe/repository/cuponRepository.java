package com.safareyes.cafe.repository;

import com.safareyes.cafe.modelo.Cupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface cuponRepository extends JpaRepository<Cupon, Integer> {

    Optional<Cupon> findByCodigo(String codigo);

    Optional<Cupon> findByCodigoIgnoreCase(String codigo);
}
