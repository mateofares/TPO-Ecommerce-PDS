package model.observadoresNotificaciones;

public class Email implements ObservadorPedido {
    private String email;

    public Email(String email) {
        this.email = email;
    }

    @Override
    public void actualizar(String msg) {
        System.out.println("[EMAIL -> " + email + "] " + msg);
    }
}
