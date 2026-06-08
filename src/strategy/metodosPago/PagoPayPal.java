package strategy.metodosPago;

public class PagoPayPal implements MetodoPago {
    private String cuenta;
    private String token;

    public PagoPayPal(String cuenta, String token) {
        this.cuenta = cuenta;
        this.token = token;
    }

    @Override
    public boolean pagar(double monto) {
        if (token == null || token.isEmpty()) return false;
        System.out.println("[PayPal -> " + cuenta + "] Pago de $" + monto + " procesado.");
        return true;
    }

    @Override
    public String getNombre() {
        return "PayPal";
    }
}
