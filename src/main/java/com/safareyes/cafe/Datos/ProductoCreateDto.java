package com.safareyes.cafe.Datos;

import com.safareyes.cafe.modelo.Alergeno;
import com.safareyes.cafe.modelo.Categoria;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoCreateDto{

    private Integer id;

    private String nombre;

    private String descripcion;

    private BigDecimal precio;

    private Integer categoriaId;

    private String categoriaNombre;

    private List<String> alergenos;

    private Set<Integer> alergenosIds;

    public ProductoCreateDto(Integer id, String nombre, String descripcion, BigDecimal precio, Boolean disponible, Integer id1, String nombre1, List<String> list) {
        
    }

    public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {}
    public interface AlergenoRepository extends JpaRepository<Alergeno, Integer> {}
}