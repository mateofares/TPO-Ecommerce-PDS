package repository;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBConnection {
    private static boolean schemaInitialized = false;

    public static synchronized Connection getConnection() throws Exception {
        if (!schemaInitialized) {
            try (Connection conn = createConnection()) {
                ensureSchema(conn);
            }
            schemaInitialized = true;
        }
        return createConnection();
    }

    public static synchronized void resetDemoData() throws Exception {
        try (Connection conn = createConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            stmt.executeUpdate("TRUNCATE TABLE pedido_items");
            stmt.executeUpdate("TRUNCATE TABLE pedidos");
            stmt.executeUpdate("TRUNCATE TABLE productos");
            stmt.executeUpdate("TRUNCATE TABLE usuarios");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private static Connection createConnection() throws Exception {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("database.properties")) {
            props.load(in);
        }
        String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");

        Class.forName(driver);
        return DriverManager.getConnection(url, user, pass);
    }

    private static void ensureSchema(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS usuarios (" +
                    "id BIGINT PRIMARY KEY, " +
                    "nombre VARCHAR(100), " +
                    "apellido VARCHAR(100), " +
                    "email VARCHAR(150) UNIQUE, " +
                    "contrasenia VARCHAR(255), " +
                    "rol VARCHAR(50), " +
                    "legajo INT NULL) ");
            try {
                stmt.executeUpdate("ALTER TABLE usuarios ADD COLUMN legajo INT NULL");
            } catch (SQLException ignored) {
                // Columna ya existe o no se puede agregar; continuar.
            }

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS productos (" +
                    "id BIGINT PRIMARY KEY, " +
                    "nombre VARCHAR(150), " +
                    "precio DOUBLE, " +
                    "stock INT, " +
                    "categoria_nombre VARCHAR(100)) ");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS pedidos (" +
                    "id BIGINT PRIMARY KEY, " +
                    "fecha DATE, " +
                    "estado VARCHAR(50), " +
                    "metodo_pago VARCHAR(100), " +
                    "total DOUBLE) ");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS pedido_items (" +
                    "pedido_id BIGINT, " +
                    "producto_id BIGINT, " +
                    "cantidad INT, " +
                    "precio_unitario DOUBLE, " +
                    "PRIMARY KEY (pedido_id, producto_id), " +
                    "FOREIGN KEY (pedido_id) REFERENCES pedidos(id), " +
                    "FOREIGN KEY (producto_id) REFERENCES productos(id)) ");
        }
    }

    public static synchronized void close() throws SQLException {
        // No persistent connection is stored here anymore.
    }
}
