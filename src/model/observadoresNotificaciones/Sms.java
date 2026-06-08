package model.observadoresNotificaciones;

public class Sms implements ObservadorPedido {
    private String telefono;

    public Sms(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public void actualizar(String msg) {
        System.out.println("[SMS -> " + telefono + "] " + msg);
    }
}
