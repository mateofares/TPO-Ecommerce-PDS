package com.emarket.observer;

public class EmailObservador implements ObservadorPedido {

    private final String email;

    public EmailObservador(String email) {
        this.email = email;
    }

    @Override
    public void actualizar(String mensaje) {
        System.out.println("[EMAIL -> " + email + "] " + mensaje);
    }
}
