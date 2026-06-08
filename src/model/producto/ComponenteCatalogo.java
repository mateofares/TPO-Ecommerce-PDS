package model.producto;

public abstract class ComponenteCatalogo {
    protected String nombre;

    public String getNombre() {
        return nombre;
    }

    public abstract void mostrar();
}
