package model.pedido;

import strategy.metodosPago.MetodoPago;

public class Pago {
    private MetodoPago metodoPago;

    public Pago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public boolean pagar(double monto) {
        return metodoPago.pagar(monto);
    }

    public void cambiarMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getNombreMetodo() {
        return metodoPago.getNombre();
    }
}
