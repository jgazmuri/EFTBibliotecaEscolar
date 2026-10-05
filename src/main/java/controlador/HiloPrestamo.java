package controlador;

import dao.LibroDAO;
import dao.PrestamoDAO;
import modelo.Libro;
import modelo.Prestamo;

import javax.swing.SwingUtilities;

public class HiloPrestamo extends Thread {

    private static final int PAUSA_MS = 1500;

    private final Prestamo prestamo;
    private final PrestamoDAO prestamoDAO;
    private final LibroDAO libroDAO;
    private final PrestamoListener listener;

    public HiloPrestamo(Prestamo prestamo, PrestamoDAO prestamoDAO,
                        LibroDAO libroDAO, PrestamoListener listener) {
        this.prestamo = prestamo;
        this.prestamoDAO = prestamoDAO;
        this.libroDAO = libroDAO;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(PAUSA_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean exito = prestamoDAO.registrarPrestamo(prestamo);
        String mensaje = armarMensaje(exito);

        SwingUtilities.invokeLater(() -> listener.alTerminar(exito, mensaje));
    }

    private String armarMensaje(boolean exito) {
        if (exito) {
            return "Préstamo registrado. Fecha de vencimiento: " + prestamo.getFechaDevolucion();
        }

        Libro actual = libroDAO.buscarPorId(prestamo.getLibro().getId());
        if (actual != null && !actual.estaDisponible()) {
            return "El libro \"" + actual.getTitulo() + "\" no tiene stock disponible.";
        }
        return "No se pudo registrar el préstamo.";
    }
}