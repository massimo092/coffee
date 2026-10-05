package com.safareyes.cafe.repository;

import com.safareyes.cafe.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface productoRepository extends JpaRepository<Producto,Integer> {
}
