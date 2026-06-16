package api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

import repository.*;
import service.*;
import util.JsonUtils;
import strategy.metodosPago.*;
import state.estadosPedidos.*;

public class ApiServer {
  private HttpServer server;
  private static ApiServer instance;
  
  private UsuarioRepository usuarioRepo;
  private ProductoRepository productoRepo;
  private PedidoRepository pedidoRepo;
  private CarritoService carritoService;

  private long generateUserId() {
    return System.nanoTime() / 1000000; // Genera ID basado en nanotime
  }

  private ApiServer() {}

  public static ApiServer getInstance() {
    if (instance == null) {
      instance = new ApiServer();
    }
    return instance;
  }

  public void start(UsuarioRepository uRepo, ProductoRepository pRepo, PedidoRepository pedRepo, CarritoService cart) throws Exception {
    this.usuarioRepo = uRepo;
    this.productoRepo = pRepo;
    this.pedidoRepo = pedRepo;
    this.carritoService = cart;

    server = HttpServer.create(new InetSocketAddress(8080), 0);
    
    // Rutas
    server.createContext("/api/usuarios/registro", new RegistroHandler());
    server.createContext("/api/usuarios/login", new LoginHandler());
    server.createContext("/api/productos", new ProductosHandler());
    server.createContext("/api/categorias", new CategoriasHandler());
    server.createContext("/api/pedidos", new PedidosHandler());
    server.createContext("/", new CorsHandler());

    server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
    server.start();
    System.out.println("[API] Servidor REST iniciado en http://localhost:8080");
  }

  public void stop() {
    if (server != null) {
      server.stop(0);
    }
  }

  // ──── CORS HANDLER ──────────────────────────────────────────────────────────
  private class CorsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {
      exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
      exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
      exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
      
      if ("OPTIONS".equals(exchange.getRequestMethod())) {
        exchange.sendResponseHeaders(204, -1);
      } else {
        respond(exchange, 404, "{\"error\":\"No encontrado\"}");
      }
    }
  }

  // ──── REGISTRO ──────────────────────────────────────────────────────────────
  private class RegistroHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {
      enableCors(exchange);
      
      if ("OPTIONS".equals(exchange.getRequestMethod())) {
        exchange.sendResponseHeaders(204, -1);
        return;
      }

      if (!"POST".equals(exchange.getRequestMethod())) {
        respond(exchange, 405, JsonUtils.createErrorResponse("Método no permitido"));
        return;
      }

      try {
        Map<String, Object> data = parseJson(exchange);
        String nombre = JsonUtils.getString(data, "nombre");
        String apellido = JsonUtils.getString(data, "apellido");
        String email = JsonUtils.getString(data, "email");
        String pass = JsonUtils.getString(data, "pass");
        String rol = JsonUtils.getString(data, "rol").toLowerCase();

        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
          respond(exchange, 400, JsonUtils.createErrorResponse("Faltan campos requeridos"));
          return;
        }

        if (usuarioRepo.existeEmail(email)) {
          respond(exchange, 409, JsonUtils.createErrorResponse("Email ya registrado"));
          return;
        }

        model.usuario.Usuario nuevoUsuario;
        if ("admin".equals(rol)) {
          int legajo = JsonUtils.getInt(data, "legajo");
          nuevoUsuario = new model.usuario.Administrador(generateUserId(), nombre, apellido, email, pass, legajo);
        } else {
          nuevoUsuario = new model.usuario.Cliente(generateUserId(), nombre, apellido, email, pass);
        }

        usuarioRepo.save(nuevoUsuario);
        respond(exchange, 201, "{\"success\":true,\"usuario\":" + JsonUtils.toJson(nuevoUsuario) + "}");

      } catch (Exception e) {
        respond(exchange, 500, JsonUtils.createErrorResponse(e.getMessage()));
      }
    }
  }

  // ──── LOGIN ─────────────────────────────────────────────────────────────────
  private class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {
      enableCors(exchange);
      
      if ("OPTIONS".equals(exchange.getRequestMethod())) {
        exchange.sendResponseHeaders(204, -1);
        return;
      }

      if (!"POST".equals(exchange.getRequestMethod())) {
        respond(exchange, 405, JsonUtils.createErrorResponse("Método no permitido"));
        return;
      }

      try {
        Map<String, Object> data = parseJson(exchange);
        String email = JsonUtils.getString(data, "email");
        String pass = JsonUtils.getString(data, "pass");

        if (email.isEmpty() || pass.isEmpty()) {
          respond(exchange, 400, JsonUtils.createErrorResponse("Email y contraseña requeridos"));
          return;
        }

        model.usuario.Usuario usuario = usuarioRepo.findByEmail(email);
        if (usuario == null || !usuario.getContrasenia().equals(pass)) {
          respond(exchange, 401, JsonUtils.createErrorResponse("Email o contraseña incorrectos"));
          return;
        }

        respond(exchange, 200, "{\"success\":true,\"usuario\":" + JsonUtils.toJson(usuario) + "}");

      } catch (Exception e) {
        respond(exchange, 500, JsonUtils.createErrorResponse(e.getMessage()));
      }
    }
  }

  // ──── PRODUCTOS ─────────────────────────────────────────────────────────────
  private class ProductosHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {
      enableCors(exchange);
      
      if ("OPTIONS".equals(exchange.getRequestMethod())) {
        exchange.sendResponseHeaders(204, -1);
        return;
      }

      if (!"GET".equals(exchange.getRequestMethod())) {
        respond(exchange, 405, JsonUtils.createErrorResponse("Método no permitido"));
        return;
      }

      try {
        String query = exchange.getRequestURI().getRawQuery();
        List<model.producto.Producto> productos = null;

        if (query == null || query.isEmpty()) {
          // GET /api/productos → todos
          productos = productoRepo.findAll();
        } else {
          // Parsear parámetros
          Map<String, String> params = parseQueryString(query);
          
          String nombre = params.get("nombre");
          String categoria = params.get("categoria");
          String precioMin = params.get("precioMin");
          String precioMax = params.get("precioMax");

          if (nombre != null && !nombre.isEmpty()) {
            productos = productoRepo.findByNombre(nombre);
          } else if (categoria != null && !categoria.isEmpty()) {
            productos = productoRepo.findByCategoria(categoria);
          } else if (precioMin != null || precioMax != null) {
            double min = precioMin != null ? Double.parseDouble(precioMin) : 0;
            double max = precioMax != null ? Double.parseDouble(precioMax) : Double.MAX_VALUE;
            productos = productoRepo.findByRangoPrecio(min, max);
          } else {
            productos = productoRepo.findAll();
          }
        }

        // Serializar a JSON
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < productos.size(); i++) {
          if (i > 0) json.append(",");
          json.append(JsonUtils.toJson(productos.get(i)));
        }
        json.append("]");

        respond(exchange, 200, json.toString());

      } catch (Exception e) {
        respond(exchange, 500, JsonUtils.createErrorResponse(e.getMessage()));
      }
    }
  }

  // ──── CATEGORÍAS ────────────────────────────────────────────────────────────
  private class CategoriasHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {
      enableCors(exchange);
      
      if ("OPTIONS".equals(exchange.getRequestMethod())) {
        exchange.sendResponseHeaders(204, -1);
        return;
      }

      if (!"GET".equals(exchange.getRequestMethod())) {
        respond(exchange, 405, JsonUtils.createErrorResponse("Método no permitido"));
        return;
      }

      try {
        // Obtener todas las categorías del repositorio de productos
        List<model.producto.Producto> todos = productoRepo.findAll();
        Set<model.producto.Categoria> cats = new HashSet<>();
        
        for (model.producto.Producto p : todos) {
          if (p.getCategoria() != null) {
            cats.add(p.getCategoria());
          }
        }

        StringBuilder json = new StringBuilder("[");
        int count = 0;
        for (model.producto.Categoria c : cats) {
          if (count > 0) json.append(",");
          json.append(JsonUtils.toJson(c));
          count++;
        }
        json.append("]");

        respond(exchange, 200, json.toString());

      } catch (Exception e) {
        respond(exchange, 500, JsonUtils.createErrorResponse(e.getMessage()));
      }
    }
  }

  // ──── PEDIDOS ───────────────────────────────────────────────────────────────
  private class PedidosHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {
      enableCors(exchange);
      
      if ("OPTIONS".equals(exchange.getRequestMethod())) {
        exchange.sendResponseHeaders(204, -1);
        return;
      }

      String method = exchange.getRequestMethod();

      try {
        if ("GET".equals(method)) {
          // GET /api/pedidos → todos los pedidos
          List<model.pedido.Pedido> pedidos = pedidoRepo.findAll();
          
          StringBuilder json = new StringBuilder("[");
          for (int i = 0; i < pedidos.size(); i++) {
            if (i > 0) json.append(",");
            json.append(JsonUtils.toJson(pedidos.get(i)));
          }
          json.append("]");

          respond(exchange, 200, json.toString());

        } else if ("POST".equals(method)) {
          // POST /api/pedidos → crear pedido
          respond(exchange, 501, JsonUtils.createErrorResponse("Crear pedidos desde API no está implementado aún"));

        } else {
          respond(exchange, 405, JsonUtils.createErrorResponse("Método no permitido"));
        }

      } catch (Exception e) {
        respond(exchange, 500, JsonUtils.createErrorResponse(e.getMessage()));
      }
    }
  }

  // ──── HELPERS ───────────────────────────────────────────────────────────────

  private void enableCors(HttpExchange exchange) {
    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
    exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
    exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    exchange.getResponseHeaders().set("Content-Type", "application/json");
  }

  private void respond(HttpExchange exchange, int code, String body) throws java.io.IOException {
    exchange.sendResponseHeaders(code, body.getBytes(StandardCharsets.UTF_8).length);
    OutputStream os = exchange.getResponseBody();
    os.write(body.getBytes(StandardCharsets.UTF_8));
    os.close();
  }

  private Map<String, Object> parseJson(HttpExchange exchange) throws Exception {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
    StringBuilder body = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      body.append(line);
    }
    reader.close();

    Object parsed = JsonUtils.parseJson(body.toString());
    if (parsed instanceof Map) {
      return (Map<String, Object>) parsed;
    }
    return new HashMap<>();
  }

  private Map<String, String> parseQueryString(String query) {
    Map<String, String> params = new HashMap<>();
    if (query == null || query.isEmpty()) return params;

    for (String pair : query.split("&")) {
      int eq = pair.indexOf('=');
      if (eq > 0) {
        String key = pair.substring(0, eq);
        String val = pair.substring(eq + 1);
        try {
          val = java.net.URLDecoder.decode(val, "UTF-8");
        } catch (Exception e) {}
        params.put(key, val);
      }
    }
    return params;
  }

  private MetodoPago construirMetodoPago(String tipo) {
    switch (tipo.toLowerCase()) {
      case "paypal":
        return new PagoPayPal("test@paypal.com", "tok_secure_xyz");
      case "transferencia":
        return new PagoTransferencia("1234567890", "Banco Test");
      case "tarjeta":
      default:
        return new TarjetaCredito("4532015112830366", "123");
    }
  }
}
