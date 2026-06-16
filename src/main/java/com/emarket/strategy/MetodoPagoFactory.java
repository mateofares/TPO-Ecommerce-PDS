package com.emarket.strategy;

import com.emarket.dto.ConfirmarCompraRequest;

// crea el método de pago correcto según lo que eligió el usuario
public class MetodoPagoFactory {

    private MetodoPagoFactory() {}
    // simple factory
    public static MetodoPago crear(ConfirmarCompraRequest req) {
        return switch (req.tipoPago()) {
            case TARJETA_CREDITO -> new TarjetaCredito(req.numero(), req.cvv());
            case TARJETA_DEBITO  -> new PagoTarjetaDebito(req.numero(), req.cvv());
            case PAYPAL          -> new PagoPayPal(req.cuenta(), req.token());
            case TRANSFERENCIA   -> new PagoTransferencia(req.cbu(), req.banco());
        };
    }
}
