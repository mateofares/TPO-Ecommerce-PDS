package com.emarket.controller;

import com.emarket.model.producto.Categoria;
import com.emarket.model.producto.Producto;
import com.emarket.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(defaultValue = "false") boolean soloConStock) {

        if (nombre != null && !nombre.isBlank())
            return productoService.buscarPorNombre(nombre);
        if (categoria != null && !categoria.isBlank())
            return productoService.buscarPorCategoria(categoria);
        if (precioMin != null && precioMax != null)
            return productoService.buscarPorRangoPrecio(precioMin, precioMax);
        if (soloConStock)
            return productoService.listarConStock();

        return productoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productoService.buscarPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/categorias")
    public List<Categoria> listarCategorias(
            @RequestParam(defaultValue = "false") boolean soloRaiz) {
        return soloRaiz
                ? productoService.listarCategoriesRaiz()
                : productoService.listarCategorias();
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Producto producto) {
        try {
            Producto guardado = productoService.guardar(producto);
            return ResponseEntity.status(201).body(guardado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
