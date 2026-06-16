package com.emarket.observer;

public class SmsObservador implements ObservadorPedido {

    private final String telefono;

    public SmsObservador(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public void actualizar(String mensaje) {
        System.out.println("[SMS -> " + telefono + "] " + mensaje);
    }
}
