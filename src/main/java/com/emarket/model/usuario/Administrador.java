package com.emarket.model.usuario;

import com.emarket.model.pedido.Pedido;
import com.emarket.state.EstadoPedidoNombre;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("ADMINISTRADOR")
@Getter
@Setter
@NoArgsConstructor
public class Administrador extends Usuario {

    private int legajo;

    public Administrador(String nombre, String apellido, String email, String contrasenia, int legajo) {
        super(nombre, apellido, email, contrasenia);
        this.legajo = legajo;
    }

    public void actualizarEstado(Pedido pedido, EstadoPedidoNombre nuevoEstado) {
        pedido.cambiarEstado(nuevoEstado);
    }
}
