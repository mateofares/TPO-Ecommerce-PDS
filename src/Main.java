import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import model.producto.Categoria;
import model.producto.Producto;
import repository.PedidoRepository;
import repository.ProductoRepository;
import repository.UsuarioRepository;
import api.ApiServer;

public class Main {

    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        UsuarioRepository usuarioRepo   = new UsuarioRepository();
        ProductoRepository productoRepo = new ProductoRepository();
        PedidoRepository pedidoRepo     = new PedidoRepository();

        seedProductosIfEmpty(productoRepo);

        ApiServer apiServer = ApiServer.getInstance();
        apiServer.start(usuarioRepo, productoRepo, pedidoRepo);
        System.out.println("[STARTUP] Servidor listo. Abrí frontend/index.html en el navegador.");
    }

    private static void seedProductosIfEmpty(ProductoRepository productoRepo) {
        if (!productoRepo.findAll().isEmpty()) return;

        System.out.println("[STARTUP] Base de datos vacía. Cargando productos iniciales...");

        Categoria celulares = new Categoria("Celulares");
        Categoria laptops   = new Categoria("Laptops");
        Categoria ropa      = new Categoria("Ropa");
        Categoria hogar     = new Categoria("Hogar");

        productoRepo.save(new Producto(1, "iPhone 15",           999.99,  10, celulares));
        productoRepo.save(new Producto(2, "Samsung Galaxy S24",  799.99,   8, celulares));
        productoRepo.save(new Producto(3, "MacBook Air M2",     1299.99,   5, laptops));
        productoRepo.save(new Producto(4, "Dell XPS 13",         999.00,   6, laptops));
        productoRepo.save(new Producto(5, "Remera básica",        19.99,  50, ropa));
        productoRepo.save(new Producto(6, "Campera invierno",     89.99,  20, ropa));
        productoRepo.save(new Producto(7, "Silla ergonómica",    249.99,  15, hogar));
        productoRepo.save(new Producto(8, "Lámpara LED",          29.99,  30, hogar));

        System.out.println("[STARTUP] Productos cargados.");
    }
}
