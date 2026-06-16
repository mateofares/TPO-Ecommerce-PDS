package com.emarket.strategy;

public class PagoPayPal implements MetodoPago {

    private final String cuenta;
    private final String token;

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
    public String getNombre() { return "PayPal"; }

    @Override
    public TipoPago getTipo() { return TipoPago.PAYPAL; }

    @Override
    public String getIdentificador() { return cuenta; }
}
