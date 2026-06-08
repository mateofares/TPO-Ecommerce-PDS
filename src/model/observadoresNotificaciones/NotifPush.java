package model.observadoresNotificaciones;

public class NotifPush implements ObservadorPedido {
    private String deviceId;

    public NotifPush(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void actualizar(String msg) {
        System.out.println("[PUSH -> " + deviceId + "] " + msg);
    }
}
