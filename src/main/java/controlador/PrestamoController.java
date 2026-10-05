package controlador;

import dao.LibroDAO;
import dao.PrestamoDAO;
import modelo.Estudiante;
import modelo.Libro;
import modelo.LibroPrestado;
import modelo.Prestamo;
import util.ValidacionException;

import java.util.List;

public class PrestamoController {

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final LibroDAO libroDAO = new LibroDAO();

    public void registrarPrestamo(Estudiante estudiante, Libro libro,
                                  PrestamoListener listener) throws ValidacionException {
        if (estudiante == null) {
            throw new ValidacionException("Debe seleccionar un estudiante.");
        }
        if (libro == null) {
            throw new ValidacionException("Debe seleccionar un libro.");
        }

        Prestamo prestamo = new Prestamo(estudiante, libro);
        new HiloPrestamo(prestamo, prestamoDAO, libroDAO, listener).start();
    }

    public void registrarDevolucion(int idPrestamo) throws ValidacionException {
        if (!prestamoDAO.registrarDevolucion(idPrestamo)) {
            throw new ValidacionException(
                    "No se pudo registrar la devolución. Es posible que el libro ya esté devuelto.");
        }
    }

    public List<Prestamo> listarTodos() {
        return prestamoDAO.listarTodos();
    }

    public List<Prestamo> listarActivos() {
        return prestamoDAO.listarActivos();
    }

    public List<Prestamo> listarAtrasados() {
        return prestamoDAO.listarActivos().stream()
                .filter(Prestamo::estaAtrasado)
                .toList();
    }

    public List<Prestamo> historialDeEstudiante(int idEstudiante) {
        return prestamoDAO.listarPorEstudiante(idEstudiante);
    }

    public List<LibroPrestado> librosMasPrestados() {
        return prestamoDAO.librosMasPrestados();
    }
}
