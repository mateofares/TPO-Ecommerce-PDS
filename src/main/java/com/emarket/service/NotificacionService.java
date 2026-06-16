package com.emarket.service;

import com.emarket.observer.EmailObservador;
import com.emarket.observer.NotifPushObservador;
import com.emarket.observer.ObservadorPedido;
import com.emarket.observer.SmsObservador;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificacionService {

    // mapa de pedidoId a lista de observadores registrados (se guarda en memoria)
    private final Map<Long, List<ObservadorPedido>> observadoresPorPedido = new HashMap<>();

    public void suscribir(Long pedidoId, String tipo, String destino) {
        ObservadorPedido observador = switch (tipo.toUpperCase()) {
            case "EMAIL" -> new EmailObservador(destino);
            case "SMS"   -> new SmsObservador(destino);
            case "PUSH"  -> new NotifPushObservador(destino);
            default      -> throw new IllegalArgumentException("Tipo de observador desconocido: " + tipo);
        };
        observadoresPorPedido.computeIfAbsent(pedidoId, k -> new ArrayList<>()).add(observador);
        System.out.println("[Observer] Suscripción " + tipo + " registrada para pedido #" + pedidoId);
    }

    public List<ObservadorPedido> getObservadores(Long pedidoId) {
        return observadoresPorPedido.getOrDefault(pedidoId, new ArrayList<>());
    }

    public void notificar(Long pedidoId, String mensaje) {
        getObservadores(pedidoId).forEach(o -> o.actualizar(mensaje));
    }

    public void removerObservadores(Long pedidoId) {
        observadoresPorPedido.remove(pedidoId);
    }
}
