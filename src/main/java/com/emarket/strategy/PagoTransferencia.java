package com.emarket.strategy;

public class PagoTransferencia implements MetodoPago {

    private final String cbu;
    private final String banco;

    public PagoTransferencia(String cbu, String banco) {
        this.cbu = cbu;
        this.banco = banco;
    }

    @Override
    public boolean pagar(double monto) {
        System.out.println("[Transferencia - " + banco + " | CBU: " + cbu
                + "] Pago de $" + monto + " procesado.");
        return true;
    }

    @Override
    public String getNombre() { return "Transferencia Bancaria"; }

    @Override
    public TipoPago getTipo() { return TipoPago.TRANSFERENCIA; }

    @Override
    public String getIdentificador() { return banco + " | CBU: " + cbu; }
}
