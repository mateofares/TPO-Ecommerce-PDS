import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import model.observadoresNotificaciones.Email;
import model.observadoresNotificaciones.NotifPush;
import model.observadoresNotificaciones.Sms;
import model.pedido.Pedido;
import model.producto.Categoria;
import model.producto.Producto;
import model.usuario.Administrador;
import model.usuario.Cliente;
import repository.PedidoRepository;
import repository.ProductoRepository;
import repository.UsuarioRepository;
import service.CarritoService;
import service.PedidoService;
import service.UsuarioService;
import state.estadosPedidos.Enviado;
import strategy.metodosPago.PagoPayPal;
import strategy.metodosPago.TarjetaCredito;

public class Main {

    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        // ── Repositorios ────────────────────────────────────────────────────
        UsuarioRepository usuarioRepo   = new UsuarioRepository();
        ProductoRepository productoRepo = new ProductoRepository();
        PedidoRepository pedidoRepo     = new PedidoRepository();

        // ── Servicios ────────────────────────────────────────────────────────
        UsuarioService usuarioService = new UsuarioService(usuarioRepo);
        PedidoService  pedidoService  = new PedidoService(pedidoRepo);

        separador("REGISTRO DE USUARIOS");
        usuarioService.registrarCliente("Juan", "Pérez", "juan@mail.com", "pass123");
        usuarioService.registrarAdministrador("Ana", "García", "ana@mail.com", "admin456", 1001);

        // ── Login ────────────────────────────────────────────────────────────
        separador("LOGIN");
        Cliente     cliente = (Cliente)     usuarioService.login("juan@mail.com", "pass123");
        Administrador admin = (Administrador) usuarioService.login("ana@mail.com", "admin456");

        // ── Catálogo de productos (Patrón Composite) ─────────────────────────
        separador("CATÁLOGO DE PRODUCTOS");
        Categoria electronica    = new Categoria("Electrónica");
        Categoria celulares      = new Categoria("Celulares", electronica);
        Categoria laptops        = new Categoria("Laptops",   electronica);
        electronica.agregar(celulares);
        electronica.agregar(laptops);

        Producto iphone   = new Producto(1, "iPhone 15",       999.99,  10, celulares);
        Producto samsung  = new Producto(2, "Samsung Galaxy",  799.99,   5, celulares);
        Producto macbook  = new Producto(3, "MacBook Air M2", 1299.99,   3, laptops);

        productoRepo.save(iphone);
        productoRepo.save(samsung);
        productoRepo.save(macbook);

        electronica.mostrar();
        System.out.println();
        for (Producto p : productoRepo.findAll()) p.mostrar();

        // ── CU-05 Buscar producto ────────────────────────────────────────────
        separador("BÚSQUEDA DE PRODUCTOS");
        System.out.println("Por nombre 'samsung':");
        productoRepo.findByNombre("samsung").forEach(Producto::mostrar);
        System.out.println("Por rango $800-$1500:");
        productoRepo.findByRangoPrecio(800, 1500).forEach(Producto::mostrar);

        // ── Carrito (CU-07 a CU-10) ──────────────────────────────────────────
        separador("CARRITO DE COMPRAS");
        CarritoService carritoService = new CarritoService(cliente.getCarrito());
        carritoService.agregarProducto(iphone, 1);
        carritoService.agregarProducto(samsung, 2);
        carritoService.verResumen();

        System.out.println("\nModificando cantidad de Samsung a 1...");
        carritoService.modificarCantidad(samsung.getId(), 1);
        carritoService.verResumen();

        System.out.println("\nEliminando Samsung del carrito...");
        carritoService.eliminarItem(samsung.getId());
        carritoService.verResumen();

        // ── CU-11/12 Selección de pago y confirmación de compra ──────────────
        separador("CONFIRMACIÓN DE COMPRA (Patrón Strategy)");
        TarjetaCredito tarjeta = new TarjetaCredito("4111111111111234", "123");

        // Suscribir observadores al pedido (Patrón Observer)
        // Se agregan antes de confirmar la compra
        Pedido pedido = pedidoService.confirmarCompra(cliente, tarjeta);

        // Suscribir notificaciones post-creación
        pedido.suscribir(new Email(cliente.getEmail()));
        pedido.suscribir(new Sms("+5491112345678"));
        pedido.suscribir(new NotifPush("device-abc-123"));

        // ── CU-17 Actualizar estado de pedido (Patrón State + Observer) ──────
        separador("GESTIÓN DE PEDIDOS (Patrón State + Observer)");
        System.out.println("Estado inicial: " + pedido.getEstadoNombre()); // Pagado (pago ya procesado)

        admin.actualizarEstado(pedido, new Enviado());
        pedido.avanzarEstado(); // Enviado → Entregado (también notifica)

        // ── CU-13/16 Ver pedidos ─────────────────────────────────────────────
        separador("VER PEDIDOS");
        cliente.verPedidos();
        System.out.println();
        admin.verTodosPedidos(pedidoService.verTodosPedidos());

        // ── Demo segundo cliente con PayPal ───────────────────────────────────
        separador("SEGUNDO CLIENTE - PAGO CON PAYPAL");
        usuarioService.registrarCliente("María", "López", "maria@mail.com", "abc123");
        Cliente cliente2 = (Cliente) usuarioService.login("maria@mail.com", "abc123");

        CarritoService carrito2 = new CarritoService(cliente2.getCarrito());
        carrito2.agregarProducto(macbook, 1);
        carrito2.verResumen();

        PagoPayPal paypal = new PagoPayPal("maria@paypal.com", "tok_secure_xyz");
        Pedido pedido2 = pedidoService.confirmarCompra(cliente2, paypal);
        pedido2.suscribir(new Email(cliente2.getEmail()));

        separador("TODOS LOS PEDIDOS FINALES");
        admin.verTodosPedidos(pedidoService.verTodosPedidos());
    }

    private static void separador(String titulo) {
        System.out.println("\n══════════════════════════════════════");
        System.out.println("  " + titulo);
        System.out.println("══════════════════════════════════════");
    }
}
