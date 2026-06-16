package com.emarket.observer;

public class NotifPushObservador implements ObservadorPedido {

    private final String deviceId;

    public NotifPushObservador(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void actualizar(String mensaje) {
        System.out.println("[PUSH -> " + deviceId + "] " + mensaje);
    }
}
