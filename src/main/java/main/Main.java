package main;

import dao.LibroDAO;
import modelo.Libro;

public class Main {

    public static void main(String[] args) {
        LibroDAO libroDAO = new LibroDAO();
        for (Libro l : libroDAO.listarTodos()) {
            System.out.println(l.getId() + " - " + l.getTitulo()
                    + " | " + l.getCategoria() + " | stock: " + l.getStock());
        }
    }
}