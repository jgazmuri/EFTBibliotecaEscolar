package dao;

import modelo.Categoria;
import modelo.Estudiante;
import modelo.Libro;
import modelo.LibroPrestado;
import modelo.Prestamo;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    private static final Object CERROJO_STOCK = new Object();

    private static final String SELECT_PRESTAMOS =
            "SELECT p.id, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, " +
                    "e.id AS estudiante_id, e.nombre AS estudiante_nombre, e.rut AS estudiante_rut, " +
                    "e.curso AS estudiante_curso, e.correo AS estudiante_correo, " +
                    "l.id AS libro_id, l.titulo, l.autor, l.isbn, l.editorial, l.stock, " +
                    "c.id AS categoria_id, c.nombre AS categoria_nombre " +
                    "FROM prestamos p " +
                    "JOIN estudiantes e ON p.id_estudiante = e.id " +
                    "JOIN libros l ON p.id_libro = l.id " +
                    "LEFT JOIN categorias c ON l.id_categoria = c.id ";

    public List<Prestamo> listarTodos() {
        return consultar("ORDER BY p.fecha_prestamo DESC, p.id DESC");
    }

    public List<Prestamo> listarPorEstudiante(int idEstudiante) {
        return consultar("WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC, p.id DESC",
                idEstudiante);
    }

    public List<Prestamo> listarActivos() {
        return consultar("WHERE p.devuelto = FALSE ORDER BY p.fecha_devolucion");
    }

    public boolean registrarPrestamo(Prestamo prestamo) {
        String sqlStock = "UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0";
        String sqlPrestamo = "INSERT INTO prestamos " +
                "(id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) " +
                "VALUES (?, ?, ?, ?, FALSE)";

        synchronized (CERROJO_STOCK) {
            Connection con = null;
            try {
                con = DatabaseConnection.getInstance().getConnection();
                con.setAutoCommit(false);

                int filas;
                try (PreparedStatement ps = con.prepareStatement(sqlStock)) {
                    ps.setInt(1, prestamo.getLibro().getId());
                    filas = ps.executeUpdate();
                }

                if (filas == 0) {
                    revertir(con);
                    return false;
                }

                try (PreparedStatement ps = con.prepareStatement(sqlPrestamo)) {
                    ps.setInt(1, prestamo.getEstudiante().getId());
                    ps.setInt(2, prestamo.getLibro().getId());
                    ps.setDate(3, Date.valueOf(prestamo.getFechaPrestamo()));
                    ps.setDate(4, Date.valueOf(prestamo.getFechaDevolucion()));
                    ps.executeUpdate();
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                System.out.println("Error al registrar préstamo: " + e.getMessage());
                revertir(con);
                return false;
            } finally {
                cerrar(con);
            }
        }
    }

    public boolean registrarDevolucion(int idPrestamo) {
        String sqlBuscar = "SELECT id_libro, devuelto FROM prestamos WHERE id = ? FOR UPDATE";
        String sqlMarcar = "UPDATE prestamos SET devuelto = TRUE WHERE id = ?";
        String sqlStock = "UPDATE libros SET stock = stock + 1 WHERE id = ?";

        synchronized (CERROJO_STOCK) {
            Connection con = null;
            try {
                con = DatabaseConnection.getInstance().getConnection();
                con.setAutoCommit(false);

                int idLibro;
                try (PreparedStatement ps = con.prepareStatement(sqlBuscar)) {
                    ps.setInt(1, idPrestamo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getBoolean("devuelto")) {
                            revertir(con);
                            return false;
                        }
                        idLibro = rs.getInt("id_libro");
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sqlMarcar)) {
                    ps.setInt(1, idPrestamo);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(sqlStock)) {
                    ps.setInt(1, idLibro);
                    ps.executeUpdate();
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                System.out.println("Error al registrar devolución: " + e.getMessage());
                revertir(con);
                return false;
            } finally {
                cerrar(con);
            }
        }
    }

    public List<LibroPrestado> librosMasPrestados() {
        List<LibroPrestado> ranking = new ArrayList<>();
        String sql = "SELECT l.titulo, l.autor, COUNT(p.id) AS veces " +
                "FROM prestamos p JOIN libros l ON p.id_libro = l.id " +
                "GROUP BY l.id, l.titulo, l.autor " +
                "ORDER BY veces DESC, l.titulo " +
                "LIMIT 10";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ranking.add(new LibroPrestado(
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getInt("veces")));
            }
        } catch (SQLException e) {
            System.out.println("Error al generar ranking de libros: " + e.getMessage());
        }
        return ranking;
    }

    private List<Prestamo> consultar(String condicion, Object... parametros) {
        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_PRESTAMOS + condicion)) {

            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(crearPrestamo(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar préstamos: " + e.getMessage());
        }
        return prestamos;
    }

    private Prestamo crearPrestamo(ResultSet rs) throws SQLException {
        Estudiante estudiante = new Estudiante(
                rs.getInt("estudiante_id"),
                rs.getString("estudiante_nombre"),
                rs.getString("estudiante_rut"),
                rs.getString("estudiante_correo"),
                rs.getString("estudiante_curso")
        );

        Categoria categoria = null;
        int idCategoria = rs.getInt("categoria_id");
        if (!rs.wasNull()) {
            categoria = new Categoria(idCategoria, rs.getString("categoria_nombre"));
        }

        Libro libro = new Libro(
                rs.getInt("libro_id"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getString("isbn"),
                rs.getString("editorial"),
                rs.getInt("stock"),
                categoria
        );

        return new Prestamo(
                rs.getInt("id"),
                estudiante,
                libro,
                rs.getDate("fecha_prestamo").toLocalDate(),
                rs.getDate("fecha_devolucion").toLocalDate(),
                rs.getBoolean("devuelto")
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