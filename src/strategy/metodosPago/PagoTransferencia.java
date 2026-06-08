package strategy.metodosPago;

public class PagoTransferencia implements MetodoPago {
    private String cbu;
    private String banco;

    public PagoTransferencia(String cbu, String banco) {
        this.cbu = cbu;
        this.banco = banco;
    }

    @Override
    public boolean pagar(double monto) {
        System.out.println("[Transferencia - " + banco + " | CBU: " + cbu + "] Pago de $" + monto + " procesado.");
        return true;
    }

    @Override
    public String getNombre() {
        return "Transferencia Bancaria";
    }
}
