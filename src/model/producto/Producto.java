package model.producto;

public class Producto {
    private long id;
    private String nombre;
    private double precio;
    private int stock;
    private Categoria categoria;

    public Producto(long id, String nombre, double precio, int stock, Categoria categoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
    }

    public boolean hayStock() { return stock > 0; }

    public boolean hayStock(int cantidad) { return stock >= cantidad; }

    public void reducirStock(int cantidad) {
        if (!hayStock(cantidad)) throw new IllegalStateException("Stock insuficiente para: " + nombre);
        stock -= cantidad;
    }

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public Categoria getCategoria() { return categoria; }
}
