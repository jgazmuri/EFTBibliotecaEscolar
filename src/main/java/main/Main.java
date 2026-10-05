package main;

import util.DatabaseConnection;
import vista.VentanaLogin;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try (Connection con = DatabaseConnection.getInstance().getConnection()) {
                new VentanaLogin();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo conectar a la base de datos.\n" + e.getMessage()
                                + "\n\nVerifique que MySQL esté activo y revise el archivo config.properties.",
                        "Error de conexión", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}