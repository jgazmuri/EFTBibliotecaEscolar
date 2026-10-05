package controlador;

import dao.EstudianteDAO;
import modelo.Estudiante;
import util.ValidacionException;

import java.util.List;

public class EstudianteController {

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    public List<Estudiante> listar() {
        return estudianteDAO.listarTodos();
    }

    public Estudiante buscarPorRut(String rut) {
        return estudianteDAO.buscarPorRut(rut);
    }

    public void guardar(String nombre, String rut, String curso, String correo,
                        String contrasena) throws ValidacionException {
        validarDatos(nombre, rut, curso, correo);
        if (esVacio(contrasena)) {
            throw new ValidacionException("La contraseña es obligatoria.");
        }

        Estudiante estudiante = new Estudiante(0, nombre.trim(), rut.trim(),
                correo.trim(), curso.trim());
        if (!estudianteDAO.guardar(estudiante, contrasena)) {
            throw new ValidacionException(
                    "No se pudo registrar al estudiante. Verifique que el RUT no esté repetido.");
        }
    }

    public void actualizar(int id, String rut, String nombre, String curso, String correo)
            throws ValidacionException {
        validarDatos(nombre, rut, curso, correo);

        Estudiante estudiante = new Estudiante(id, nombre.trim(), rut.trim(),
                correo.trim(), curso.trim());
        if (!estudianteDAO.actualizar(estudiante)) {
            throw new ValidacionException("No se pudo actualizar al estudiante.");
        }
    }

    public void eliminar(Estudiante estudiante) throws ValidacionException {
        if (!estudianteDAO.eliminar(estudiante)) {
            throw new ValidacionException(
                    "No se pudo eliminar al estudiante. Verifique que no tenga préstamos registrados.");
        }
    }

    private void validarDatos(String nombre, String rut, String curso, String correo)
            throws ValidacionException {
        if (esVacio(nombre)) {
            throw new ValidacionException("El nombre es obligatorio.");
        }
        if (esVacio(rut) || !rut.trim().matches("\\d{7,8}-[\\dkK]")) {
            throw new ValidacionException("El RUT debe tener el formato 12345678-9.");
        }
        if (esVacio(curso)) {
            throw new ValidacionException("El curso es obligatorio.");
        }
        if (esVacio(correo) || !correo.contains("@")) {
            throw new ValidacionException("Ingrese un correo válido.");
        }
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}