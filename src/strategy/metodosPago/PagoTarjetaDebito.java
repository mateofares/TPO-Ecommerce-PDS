package strategy.metodosPago;

public class PagoTarjetaDebito implements MetodoPago {
    private String numero;
    private String cvv;

    public PagoTarjetaDebito(String numero, String cvv) {
        this.numero = numero;
        this.cvv = cvv;
    }

    @Override
    public boolean pagar(double monto) {
        System.out.println("[Tarjeta Débito *" + numero.substring(numero.length() - 4) + "] Pago de $" + monto + " procesado.");
        return true;
    }

    @Override
    public String getNombre() {
        return "Tarjeta de Débito";
    }
}
