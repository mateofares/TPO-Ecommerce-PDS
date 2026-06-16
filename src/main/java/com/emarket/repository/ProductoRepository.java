package com.emarket.repository;

import com.emarket.model.producto.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByCategoriaNombreIgnoreCase(String categoria);
    List<Producto> findByPrecioBetween(double min, double max);
    List<Producto> findByStockGreaterThan(int minStock);
}
