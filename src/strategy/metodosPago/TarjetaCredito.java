package strategy.metodosPago;

public class TarjetaCredito implements MetodoPago {
    private String numero;
    private String cvv;

    public TarjetaCredito(String numero, String cvv) {
        this.numero = numero;
        this.cvv = cvv;
    }

    @Override
    public boolean pagar(double monto) {
        if (cvv == null || cvv.length() < 3) return false;
        System.out.println("[Tarjeta Crédito *" + numero.substring(numero.length() - 4) + "] Pago de $" + monto + " procesado.");
        return true;
    }

    @Override
    public String getNombre() {
        return "Tarjeta de Crédito";
    }
}
