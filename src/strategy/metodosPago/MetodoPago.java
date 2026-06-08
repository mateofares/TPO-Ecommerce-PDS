package strategy.metodosPago;

public interface MetodoPago {
    boolean pagar(double monto);
    String getNombre();
}
