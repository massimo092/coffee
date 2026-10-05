package com.safareyes.cafe.repository;

import com.safareyes.cafe.modelo.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface pedidoRepository extends JpaRepository<Pedido,Integer> {
}
