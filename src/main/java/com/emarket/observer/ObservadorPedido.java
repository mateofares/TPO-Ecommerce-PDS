package com.emarket.observer;

/**
 * Interfaz del Patrón Observer.
 * Los observadores reciben notificaciones cuando el estado de un pedido cambia.
 */
public interface ObservadorPedido {
    void actualizar(String mensaje);
}
