package com.safareyes.cafe.repository;

import com.safareyes.cafe.modelo.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface categoriaRepository extends JpaRepository<Categoria,Integer> {
}
