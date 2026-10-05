package controlador;

import dao.CategoriaDAO;
import modelo.Categoria;
import util.ValidacionException;

import java.util.List;

public class CategoriaController {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Categoria> listar() {
        return categoriaDAO.listarTodas();
    }

    public void guardar(String nombre) throws ValidacionException {
        validarNombre(nombre);
        if (!categoriaDAO.guardar(new Categoria(0, nombre.trim()))) {
            throw new ValidacionException("No se pudo guardar la categoría.");
        }
    }

    public void actualizar(int id, String nombre) throws ValidacionException {
        validarNombre(nombre);
        if (!categoriaDAO.actualizar(new Categoria(id, nombre.trim()))) {
            throw new ValidacionException("No se pudo actualizar la categoría.");
        }
    }

    public void eliminar(int id) throws ValidacionException {
        if (!categoriaDAO.eliminar(id)) {
            throw new ValidacionException(
                    "No se pudo eliminar la categoría. Verifique que no tenga libros asociados.");
        }
    }

    private void validarNombre(String nombre) throws ValidacionException {
        if (nombre == null || nombre.isBlank()) {
            throw new ValidacionException("El nombre de la categoría no puede estar vacío.");
        }
    }
}