package com.emarket.model.producto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
    private Categoria padre;

    public Long getPadreId() {
        return padre != null ? padre.getId() : null;
    }

    // serializa los hijos sin volver al padre (evita recursión infinita)
    @JsonManagedReference
    @OneToMany(mappedBy = "padre", cascade = CascadeType.ALL)
    private List<Categoria> hijos = new ArrayList<>();

    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    public Categoria(String nombre, Categoria padre) {
        this.nombre = nombre;
        this.padre = padre;
    }

    public void agregar(Categoria subcategoria) {
        subcategoria.setPadre(this);
        hijos.add(subcategoria);
    }

}
