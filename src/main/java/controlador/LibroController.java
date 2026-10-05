package controlador;

import dao.LibroDAO;
import modelo.Categoria;
import modelo.Libro;
import util.ValidacionException;

import java.util.List;

public class LibroController {

    private final LibroDAO libroDAO = new LibroDAO();

    public List<Libro> listar() {
        return libroDAO.listarTodos();
    }

    public void guardar(String titulo, String autor, String isbn, String editorial,
                        String stock, Categoria categoria) throws ValidacionException {
        Libro libro = construirLibro(0, titulo, autor, isbn, editorial, stock, categoria);
        if (!libroDAO.guardar(libro)) {
            throw new ValidacionException(
                    "No se pudo guardar el libro. Verifique que el ISBN no esté repetido.");
        }
    }

    public void actualizar(int id, String titulo, String autor, String isbn, String editorial,
                           String stock, Categoria categoria) throws ValidacionException {
        Libro libro = construirLibro(id, titulo, autor, isbn, editorial, stock, categoria);
        if (!libroDAO.actualizar(libro)) {
            throw new ValidacionException(
                    "No se pudo actualizar el libro. Verifique que el ISBN no esté repetido.");
        }
    }

    public void eliminar(int id) throws ValidacionException {
        if (!libroDAO.eliminar(id)) {
            throw new ValidacionException(
                    "No se pudo eliminar el libro. Verifique que no tenga préstamos registrados.");
        }
    }

    private Libro construirLibro(int id, String titulo, String autor, String isbn,
                                 String editorial, String stock, Categoria categoria)
            throws ValidacionException {

        if (esVacio(titulo)) {
            throw new ValidacionException("El título es obligatorio.");
        }
        if (esVacio(autor)) {
            throw new ValidacionException("El autor es obligatorio.");
        }
        if (esVacio(isbn)) {
            throw new ValidacionException("El ISBN es obligatorio.");
        }
        if (esVacio(editorial)) {
            throw new ValidacionException("La editorial es obligatoria.");
        }
        if (esVacio(stock)) {
            throw new ValidacionException("El stock es obligatorio.");
        }

        int stockNumero;
        try {
            stockNumero = Integer.parseInt(stock.trim());
        } catch (NumberFormatException e) {
            throw new ValidacionException("El stock debe ser un número entero.");
        }
        if (stockNumero < 0) {
            throw new ValidacionException("El stock no puede ser negativo.");
        }
        if (categoria == null) {
            throw new ValidacionException("Debe seleccionar una categoría.");
        }

        return new Libro(id, titulo.trim(), autor.trim(), isbn.trim(),
                editorial.trim(), stockNumero, categoria);
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}