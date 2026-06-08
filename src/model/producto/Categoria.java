package model.producto;

import java.util.ArrayList;
import java.util.List;

public class Categoria extends ComponenteCatalogo {
    private Categoria padre;
    private List<Categoria> hijos;

    public Categoria(String nombre) {
        this.nombre = nombre;
        this.hijos = new ArrayList<>();
    }

    public Categoria(String nombre, Categoria padre) {
        this(nombre);
        this.padre = padre;
    }

    public void agregar(Categoria subcategoria) {
        hijos.add(subcategoria);
    }

    public List<Categoria> getHijos() {
        return hijos;
    }

    public Categoria getPadre() {
        return padre;
    }

    @Override
    public void mostrar() {
        System.out.println("[Categoría: " + nombre + "]");
        for (Categoria h : hijos) {
            h.mostrar();
        }
    }
}
