package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/biblioteca?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USUARIO = "root";
    private static final String CLAVE = "SpeedFast_2026*";

    private static DatabaseConnection instancia;

    private DatabaseConnection() {
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instancia == null) {
            instancia = new DatabaseConnection();
        }
        return instancia;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}