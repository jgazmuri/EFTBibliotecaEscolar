package controlador;

import dao.UsuarioDAO;
import modelo.Usuario;
import util.ValidacionException;

public class LoginController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario iniciarSesion(String identificador, String contrasena)
            throws ValidacionException {

        if (identificador == null || identificador.isBlank()
                || contrasena == null || contrasena.isBlank()) {
            throw new ValidacionException("Ingrese su correo o RUT y su contraseña.");
        }

        Usuario usuario = usuarioDAO.autenticar(identificador.trim(), contrasena);
        if (usuario == null) {
            throw new ValidacionException("Credenciales incorrectas.");
        }
        return usuario;
    }
}