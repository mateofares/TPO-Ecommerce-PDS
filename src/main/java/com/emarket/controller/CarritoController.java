package com.emarket.controller;

import com.emarket.dto.AgregarItemRequest;
import com.emarket.dto.ModificarCantidadRequest;
import com.emarket.model.carrito.Carrito;
import com.emarket.service.CarritoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/carrito")
@CrossOrigin(origins = "*")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<?> obtener(@PathVariable Long clienteId) {
        try {
            Carrito carrito = carritoService.obtenerCarrito(clienteId);
            return ResponseEntity.ok(Map.of(
                    "items", carrito.getItems(),
                    "total", carrito.calcularTotal()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{clienteId}/agregar")
    public ResponseEntity<?> agregar(@PathVariable Long clienteId,
                                     @Valid @RequestBody AgregarItemRequest req) {
        try {
            Carrito carrito = carritoService.agregarProducto(clienteId, req.productoId(), req.cantidad());
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Producto agregado al carrito.",
                    "total", carrito.calcularTotal()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{clienteId}/items/{productoId}")
    public ResponseEntity<?> modificarCantidad(@PathVariable Long clienteId,
                                               @PathVariable Long productoId,
                                               @Valid @RequestBody ModificarCantidadRequest req) {
        try {
            Carrito carrito = carritoService.modificarCantidad(clienteId, productoId, req.cantidad());
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Cantidad actualizada.",
                    "total", carrito.calcularTotal()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{clienteId}/items/{productoId}")
    public ResponseEntity<?> eliminar(@PathVariable Long clienteId,
                                      @PathVariable Long productoId) {
        try {
            Carrito carrito = carritoService.eliminarItem(clienteId, productoId);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Ítem eliminado del carrito.",
                    "total", carrito.calcularTotal()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
