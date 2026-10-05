package main;

import controlador.CategoriaController;
import controlador.EstudianteController;
import controlador.LibroController;
import modelo.Categoria;
import modelo.Estudiante;
import modelo.Libro;
import util.ValidacionException;

public class Main {

    public static void main(String[] args) {
        LibroController libros = new LibroController();
        CategoriaController categorias = new CategoriaController();
        EstudianteController estudiantes = new EstudianteController();

        try {
            Categoria cat = categorias.listar().get(0);

            libros.guardar("Libro de Prueba", "Autor Prueba", "9999999999999", "Editorial X", "3", cat);
            Libro creado = libros.listar().stream()
                    .filter(l -> l.getIsbn().equals("9999999999999")).findFirst().orElseThrow();
            System.out.println("Libro creado, id " + creado.getId() + ", stock " + creado.getStock());

            libros.actualizar(creado.getId(), "Libro de Prueba", "Autor Prueba", "9999999999999",
                    "Editorial X", "10", cat);
            System.out.println("Libro actualizado: stock 10");

            libros.eliminar(creado.getId());
            System.out.println("Libro eliminado");

            estudiantes.guardar("Estudiante Prueba", "12121212-1", "1ro Medio Z", "prueba@correo.cl", "clave");
            Estudiante est = estudiantes.buscarPorRut("12121212-1");
            System.out.println("Estudiante creado: " + est.getDescripcion());
            estudiantes.eliminar(est);
            System.out.println("Estudiante eliminado");
        } catch (ValidacionException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            libros.guardar("Otro", "Autor", "123", "Ed", "-5", null);
        } catch (ValidacionException e) {
            System.out.println("Error esperado: " + e.getMessage());
        }

        try {
            estudiantes.guardar("Ana", "123", "1ro", "ana@correo.cl", "x");
        } catch (ValidacionException e) {
            System.out.println("Error esperado: " + e.getMessage());
        }
    }
}