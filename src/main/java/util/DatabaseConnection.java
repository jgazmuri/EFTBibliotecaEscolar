package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String ARCHIVO_CONFIG = "config.properties";

    private static DatabaseConnection instancia;

    private final String url;
    private final String usuario;
    private final String clave;

    private DatabaseConnection() {
        Properties config = new Properties();
        try (Reader lector = new InputStreamReader(
                new FileInputStream(ARCHIVO_CONFIG), StandardCharsets.UTF_8)) {
            config.load(lector);
        } catch (IOException e) {
            System.out.println("No se pudo leer " + ARCHIVO_CONFIG
                    + ", se usarán los valores por defecto.");
        }

        url = config.getProperty("db.url",
                "jdbc:mysql://localhost:3306/biblioteca?useSSL=false&allowPublicKeyRetrieval=true");
        usuario = config.getProperty("db.usuario", "root");
        clave = config.getProperty("db.clave", "");
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instancia == null) {
            instancia = new DatabaseConnection();
        }
        return instancia;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }
}