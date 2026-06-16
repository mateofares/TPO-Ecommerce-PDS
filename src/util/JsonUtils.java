package util;

import model.usuario.Usuario;
import model.usuario.Cliente;
import model.usuario.Administrador;
import model.producto.Producto;
import model.producto.Categoria;
import model.pedido.Pedido;
import model.carrito.ItemCarrito;

public class JsonUtils {

  // ──── Objeto a JSON ──────────────────────────────────────────────────────────

  public static String toJson(Object obj) {
    if (obj == null) return "null";
    
    if (obj instanceof String)    return "\"" + escapeJson((String)obj) + "\"";
    if (obj instanceof Integer)   return obj.toString();
    if (obj instanceof Double)    return obj.toString();
    if (obj instanceof Boolean)   return obj.toString();
    if (obj instanceof Usuario)   return usuarioToJson((Usuario)obj);
    if (obj instanceof Producto)  return productoToJson((Producto)obj);
    if (obj instanceof Categoria) return categoriaToJson((Categoria)obj);
    if (obj instanceof Pedido)    return pedidoToJson((Pedido)obj);
    if (obj instanceof ItemCarrito) return itemCarritoToJson((ItemCarrito)obj);
    
    return "{}";
  }

  public static String arrayToJson(Object[] arr) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < arr.length; i++) {
      if (i > 0) sb.append(",");
      sb.append(toJson(arr[i]));
    }
    sb.append("]");
    return sb.toString();
  }

  public static String usuarioToJson(Usuario u) {
    StringBuilder sb = new StringBuilder("{");
    sb.append("\"id\":").append(u.getId()).append(",");
    sb.append("\"nombre\":\"").append(escapeJson(u.getNombre())).append("\",");
    sb.append("\"apellido\":\"").append(escapeJson(u.getApellido())).append("\",");
    sb.append("\"email\":\"").append(escapeJson(u.getEmail())).append("\",");
    String rol = u instanceof Administrador ? "admin" : "cliente";
    sb.append("\"rol\":\"").append(rol).append("\"");
    
    if (u instanceof Administrador) {
      Administrador admin = (Administrador) u;
      sb.append(",\"legajo\":").append(admin.getLegajo());
    }
    
    sb.append("}");
    return sb.toString();
  }

  public static String productoToJson(Producto p) {
    StringBuilder sb = new StringBuilder("{");
    sb.append("\"id\":").append(p.getId()).append(",");
    sb.append("\"nombre\":\"").append(escapeJson(p.getNombre())).append("\",");
    sb.append("\"precio\":").append(p.getPrecio()).append(",");
    sb.append("\"stock\":").append(p.getStock()).append(",");
    sb.append("\"categoria\":").append(categoriaToJson(p.getCategoria()));
    sb.append("}");
    return sb.toString();
  }

  public static String categoriaToJson(Categoria c) {
    if (c == null) return "null";
    StringBuilder sb = new StringBuilder("{");
    sb.append("\"nombre\":\"").append(escapeJson(c.getNombre())).append("\"");
    sb.append("}");
    return sb.toString();
  }

  public static String pedidoToJson(Pedido p) {
    StringBuilder sb = new StringBuilder("{");
    sb.append("\"id\":").append(p.getId()).append(",");
    sb.append("\"usuarioId\":").append(p.getUsuarioId()).append(",");
    sb.append("\"fecha\":\"").append(p.getFecha()).append("\",");
    sb.append("\"estado\":\"").append(p.getEstadoNombre()).append("\",");
    sb.append("\"metodoPago\":\"").append(p.getMetodoPago().getNombre()).append("\",");
    sb.append("\"total\":").append(p.calcularTotal()).append(",");
    boolean puedeAvanzar = !(p.getEstado() instanceof state.estadosPedidos.Entregado);
    sb.append("\"puedeAvanzar\":").append(puedeAvanzar).append(",");
    sb.append("\"items\":[");

    java.util.List<ItemCarrito> items = p.getItems();
    for (int i = 0; i < items.size(); i++) {
      if (i > 0) sb.append(",");
      sb.append(itemCarritoToJson(items.get(i)));
    }

    sb.append("]");
    sb.append("}");
    return sb.toString();
  }

  public static String itemCarritoToJson(ItemCarrito ic) {
    StringBuilder sb = new StringBuilder("{");
    sb.append("\"producto\":").append(productoToJson(ic.getProducto())).append(",");
    sb.append("\"cantidad\":").append(ic.getCantidad());
    sb.append("}");
    return sb.toString();
  }

  // ──── JSON a Objeto ──────────────────────────────────────────────────────────

  public static Object parseJson(String json) {
    json = json.trim();
    if (json.startsWith("{")) return parseObject(json);
    if (json.startsWith("[")) return parseArray(json);
    if (json.equals("null")) return null;
    if (json.equals("true")) return true;
    if (json.equals("false")) return false;
    if (json.startsWith("\"")) return json.substring(1, json.length() - 1);
    try {
      if (json.contains(".")) return Double.parseDouble(json);
      return Integer.parseInt(json);
    } catch (Exception e) {
      return json;
    }
  }

  public static java.util.Map<String, Object> parseObject(String json) {
    java.util.Map<String, Object> map = new java.util.HashMap<>();
    json = json.substring(1, json.length() - 1).trim();
    if (json.isEmpty()) return map;

    int depth = 0;
    int start = 0;
    String key = null;

    for (int i = 0; i < json.length(); i++) {
      char c = json.charAt(i);

      if (c == '{' || c == '[') depth++;
      else if (c == '}' || c == ']') {
        depth--;
        // Último valor termina en } o ] — guardarlo ahora que depth vuelve a 0
        if (depth == 0 && i == json.length() - 1 && key != null) {
          String value = json.substring(start).trim();
          map.put(key, parseJson(value));
        }
      } else if (c == ':' && depth == 0) {
        key = json.substring(start, i).trim();
        if (key.startsWith("\"")) key = key.substring(1, key.length() - 1);
        start = i + 1;
      } else if ((c == ',' || i == json.length() - 1) && depth == 0) {
        if (i == json.length() - 1 && c != ',') {
          String value = json.substring(start).trim();
          if (key != null) map.put(key, parseJson(value));
        } else if (c == ',') {
          String value = json.substring(start, i).trim();
          if (key != null) map.put(key, parseJson(value));
          start = i + 1;
        }
      }
    }

    return map;
  }

  public static java.util.List<Object> parseArray(String json) {
    java.util.List<Object> list = new java.util.ArrayList<>();
    json = json.substring(1, json.length() - 1).trim();
    if (json.isEmpty()) return list;

    int depth = 0;
    int start = 0;

    for (int i = 0; i < json.length(); i++) {
      char c = json.charAt(i);

      if (c == '{' || c == '[') depth++;
      else if (c == '}' || c == ']') {
        depth--;
        // Último elemento termina en } o ] — agregarlo ahora que depth vuelve a 0
        if (depth == 0 && i == json.length() - 1) {
          list.add(parseJson(json.substring(start).trim()));
        }
      } else if ((c == ',' || i == json.length() - 1) && depth == 0) {
        if (i == json.length() - 1 && c != ',') {
          String value = json.substring(start).trim();
          list.add(parseJson(value));
        } else if (c == ',') {
          String value = json.substring(start, i).trim();
          list.add(parseJson(value));
          start = i + 1;
        }
      }
    }

    return list;
  }

  public static String getString(java.util.Map<String, Object> map, String key) {
    Object val = map.get(key);
    return val != null ? val.toString() : "";
  }

  public static double getDouble(java.util.Map<String, Object> map, String key) {
    Object val = map.get(key);
    if (val instanceof Double) return (Double) val;
    if (val instanceof Integer) return ((Integer) val).doubleValue();
    try {
      return Double.parseDouble(val.toString());
    } catch (Exception e) {
      return 0;
    }
  }

  public static int getInt(java.util.Map<String, Object> map, String key) {
    Object val = map.get(key);
    if (val instanceof Integer) return (Integer) val;
    try {
      return Integer.parseInt(val.toString());
    } catch (Exception e) {
      return 0;
    }
  }

  // ──── Helpers ────────────────────────────────────────────────────────────────

  private static String escapeJson(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
  }

  public static String createErrorResponse(String message) {
    return "{\"error\":\"" + escapeJson(message) + "\"}";
  }

  public static String createSuccessResponse(String message) {
    return "{\"success\":true,\"message\":\"" + escapeJson(message) + "\"}";
  }
}
