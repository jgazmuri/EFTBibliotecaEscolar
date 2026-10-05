package dao;

import modelo.Estudiante;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    public List<Estudiante> listarTodos() {
        List<Estudiante> estudiantes = new ArrayList<>();
        String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes ORDER BY nombre";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                estudiantes.add(crearEstudiante(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar estudiantes: " + e.getMessage());
        }
        return estudiantes;
    }

    public Estudiante buscarPorRut(String rut) {
        String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes WHERE rut = ?";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rut);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearEstudiante(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar estudiante: " + e.getMessage());
        }
        return null;
    }

    public boolean guardar(Estudiante estudiante, String contrasena) {
        String sqlEstudiante = "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";
        String sqlUsuario = "INSERT INTO usuarios (nombre, rut, correo, contraseña, rol) " +
                "VALUES (?, ?, ?, ?, 'estudiante')";
        Connection con = null;

        try {
            con = DatabaseConnection.getInstance().getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlEstudiante)) {
                ps.setString(1, estudiante.getNombre());
                ps.setString(2, estudiante.getRut());
                ps.setString(3, estudiante.getCurso());
                ps.setString(4, estudiante.getCorreo());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                ps.setString(1, estudiante.getNombre());
                ps.setString(2, estudiante.getRut());
                ps.setString(3, estudiante.getCorreo());
                ps.setString(4, contrasena);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("Error al guardar estudiante: " + e.getMessage());
            revertir(con);
            return false;
        } finally {
            cerrar(con);
        }
    }

    public boolean actualizar(Estudiante estudiante) {
        String sqlEstudiante = "UPDATE estudiantes SET nombre = ?, curso = ?, correo = ? WHERE id = ?";
        String sqlUsuario = "UPDATE usuarios SET nombre = ?, correo = ? WHERE rut = ?";
        Connection con = null;

        try {
            con = DatabaseConnection.getInstance().getConnection();
            con.setAutoCommit(false);

            int filas;
            try (PreparedStatement ps = con.prepareStatement(sqlEstudiante)) {
                ps.setString(1, estudiante.getNombre());
                ps.setString(2, estudiante.getCurso());
                ps.setString(3, estudiante.getCorreo());
                ps.setInt(4, estudiante.getId());
                filas = ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                ps.setString(1, estudiante.getNombre());
                ps.setString(2, estudiante.getCorreo());
                ps.setString(3, estudiante.getRut());
                ps.executeUpdate();
            }

            con.commit();
            return filas > 0;
        } catch (SQLException e) {
            System.out.println("Error al actualizar estudiante: " + e.getMessage());
            revertir(con);
            return false;
        } finally {
            cerrar(con);
        }
    }

    public boolean eliminar(Estudiante estudiante) {
        String sqlUsuario = "DELETE FROM usuarios WHERE rut = ?";
        String sqlEstudiante = "DELETE FROM estudiantes WHERE id = ?";
        Connection con = null;

        try {
            con = DatabaseConnection.getInstance().getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                ps.setString(1, estudiante.getRut());
                ps.executeUpdate();
            }

            int filas;
            try (PreparedStatement ps = con.prepareStatement(sqlEstudiante)) {
                ps.setInt(1, estudiante.getId());
                filas = ps.executeUpdate();
            }

            con.commit();
            return filas > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar estudiante: " + e.getMessage());
            revertir(con);
            return false;
        } finally {
            cerrar(con);
        }
    }

    private Estudiante crearEstudiante(ResultSet rs) throws SQLException {
        return new Estudiante(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("rut"),
                rs.getString("correo"),
                rs.getString("curso")
        );
    }

    private void revertir(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException e) {
                System.out.println("Error al revertir la transacción: " + e.getMessage());
            }
        }
    }

    private void cerrar(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}