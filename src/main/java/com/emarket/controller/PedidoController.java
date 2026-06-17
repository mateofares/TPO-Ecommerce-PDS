package com.emarket.controller;

import com.emarket.dto.ActualizarEstadoRequest;
import com.emarket.dto.ConfirmarCompraRequest;
import com.emarket.dto.SuscribirObservadorRequest;
import com.emarket.model.pedido.Pedido;
import com.emarket.service.NotificacionService;
import com.emarket.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoService pedidoService;
    private final NotificacionService notificacionService;

    public PedidoController(PedidoService pedidoService,
                            NotificacionService notificacionService) {
        this.pedidoService = pedidoService;
        this.notificacionService = notificacionService;
    }

    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmar(@Valid @RequestBody ConfirmarCompraRequest req) {
        try {
            Pedido pedido = pedidoService.confirmarCompra(req);
            return ResponseEntity.status(201).body(Map.of(
                    "id", pedido.getId(),
                    "estado", pedido.getEstadoNombreStr(),
                    "total", pedido.calcularTotal(),
                    "metodoPago", pedido.getTipoPago()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // solo el admin debería usar este endpoint
    @GetMapping
    public List<Pedido> listarTodos() {
        return pedidoService.listarTodos();
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> listarPorCliente(@PathVariable Long clienteId) {
        try {
            return ResponseEntity.ok(pedidoService.listarPorCliente(clienteId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            Pedido p = pedidoService.buscarPorId(id);
            return ResponseEntity.ok(Map.of(
                    "id", p.getId(),
                    "fecha", p.getFecha(),
                    "estado", p.getEstadoNombreStr(),
                    "total", p.calcularTotal(),
                    "metodoPago", p.getTipoPago(),
                    "identificadorPago", p.getIdentificadorPago() != null ? p.getIdentificadorPago() : ""
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/avanzar")
    public ResponseEntity<?> avanzar(@PathVariable Long id) {
        try {
            String estadoAnterior = pedidoService.buscarPorId(id).getEstadoNombreStr();
            Pedido p = pedidoService.avanzarEstado(id);
            return ResponseEntity.ok(Map.of(
                    "id", p.getId(),
                    "estadoAnterior", estadoAnterior,
                    "estadoActual", p.getEstadoNombreStr()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @Valid @RequestBody ActualizarEstadoRequest req) {
        try {
            Pedido p = pedidoService.cambiarEstado(id, req.estado());
            return ResponseEntity.ok(Map.of(
                    "id", p.getId(),
                    "estado", p.getEstadoNombreStr()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/suscribir")
    public ResponseEntity<?> suscribir(@PathVariable Long id,
                                       @Valid @RequestBody SuscribirObservadorRequest req) {
        try {
            pedidoService.buscarPorId(id); // nos aseguramos que el pedido existe antes de suscribir
            notificacionService.suscribir(id, req.tipo(), req.destino());
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Observador " + req.tipo() + " registrado para pedido #" + id
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
