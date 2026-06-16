package com.emarket.strategy;

public class TarjetaCredito implements MetodoPago {

    private final String numero;
    private final String cvv;

    public TarjetaCredito(String numero, String cvv) {
        this.numero = numero;
        this.cvv = cvv;
    }

    @Override
    public boolean pagar(double monto) {
        if (cvv == null || cvv.length() < 3) return false;
        System.out.println("[Tarjeta Crédito *" + numero.substring(numero.length() - 4)
                + "] Pago de $" + monto + " procesado.");
        return true;
    }

    @Override
    public String getNombre() { return "Tarjeta de Crédito"; }

    @Override
    public TipoPago getTipo() { return TipoPago.TARJETA_CREDITO; }

    @Override
    public String getIdentificador() { return "*" + numero.substring(numero.length() - 4); }
}
