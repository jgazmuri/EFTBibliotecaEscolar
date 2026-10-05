package main;

import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        DatabaseConnection a = DatabaseConnection.getInstance();
        DatabaseConnection b = DatabaseConnection.getInstance();
        System.out.println("¿Es la misma instancia (Singleton)? " + (a == b));

        try (Connection con = a.getConnection()) {
            System.out.println("Conexión exitosa a la base de datos: " + con.getCatalog());
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }
}