package com.emarket.strategy;

/**
 * Interfaz del Patrón Strategy para métodos de pago.
 * Permite intercambiar el algoritmo de pago en tiempo de ejecución.
 */
public interface MetodoPago {
    boolean pagar(double monto);
    String getNombre();
    TipoPago getTipo();
    String getIdentificador();
}
