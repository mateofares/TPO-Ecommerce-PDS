package com.emarket.config;

import com.emarket.model.producto.Categoria;
import com.emarket.model.producto.Producto;
import com.emarket.model.usuario.Administrador;
import com.emarket.model.usuario.Cliente;
import com.emarket.repository.CategoriaRepository;
import com.emarket.repository.ProductoRepository;
import com.emarket.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public void run(String... args) {
        cargarUsuarios();
        if (categoriaRepository.count() == 0) {
            cargarCatalogo();
        }
        System.out.println("=== E-Market Ropa iniciado. API disponible en http://localhost:8080 ===");
    }

    private void cargarUsuarios() {
        if (!usuarioRepository.existsByEmailIgnoreCase("admin@emarket.com")) {
            usuarioRepository.save(new Administrador(
                    "Admin", "Sistema", "admin@emarket.com", "admin123", 1001));
        }
        if (!usuarioRepository.existsByEmailIgnoreCase("juan@mail.com")) {
            usuarioRepository.save(new Cliente(
                    "Juan", "Pérez", "juan@mail.com", "pass123"));
        }
    }

    private void cargarCatalogo() {
        // Categorías raíz
        Categoria hombre     = categoriaRepository.save(new Categoria("Hombre"));
        Categoria mujer      = categoriaRepository.save(new Categoria("Mujer"));
        Categoria accesorios = categoriaRepository.save(new Categoria("Accesorios"));

        // Subcategorías
        Categoria remeras    = categoriaRepository.save(new Categoria("Remeras",       hombre));
        Categoria pantalones = categoriaRepository.save(new Categoria("Pantalones",    hombre));
        Categoria buzos      = categoriaRepository.save(new Categoria("Buzos",         hombre));
        Categoria vestidos   = categoriaRepository.save(new Categoria("Vestidos",      mujer));
        Categoria calzadoM   = categoriaRepository.save(new Categoria("Calzado",       mujer));
        Categoria bolsos     = categoriaRepository.save(new Categoria("Bolsos",        accesorios));
        Categoria relojes    = categoriaRepository.save(new Categoria("Relojes",       accesorios));

        // Productos — Hombre
        productoRepository.save(new Producto("Remera Básica Blanca",    12.99, 40, remeras));
        productoRepository.save(new Producto("Remera Oversize Negra",   18.99, 25, remeras));
        productoRepository.save(new Producto("Remera Polo Azul",        22.99, 20, remeras));
        productoRepository.save(new Producto("Jean Slim Fit",           49.99, 15, pantalones));
        productoRepository.save(new Producto("Jogger Deportivo Gris",   34.99, 18, pantalones));
        productoRepository.save(new Producto("Buzo Canguro Azul",       39.99, 12, buzos));
        productoRepository.save(new Producto("Hoodie Premium Negro",    55.99,  8, buzos));

        // Productos — Mujer
        productoRepository.save(new Producto("Vestido Floral Verano",   45.99, 10, vestidos));
        productoRepository.save(new Producto("Vestido Negro Elegante",  65.99,  7, vestidos));
        productoRepository.save(new Producto("Zapatillas Urbanas",      59.99, 14, calzadoM));
        productoRepository.save(new Producto("Sandalias Verano",        29.99, 20, calzadoM));
        productoRepository.save(new Producto("Botas de Cuero",          89.99,  5, calzadoM));

        // Productos — Accesorios
        productoRepository.save(new Producto("Mochila Urbana",          49.99, 10, bolsos));
        productoRepository.save(new Producto("Cartera de Cuero",        75.99,  6, bolsos));
        productoRepository.save(new Producto("Reloj Clásico Plateado",  99.99,  8, relojes));
        productoRepository.save(new Producto("Reloj Deportivo Negro",   79.99, 10, relojes));
    }
}
