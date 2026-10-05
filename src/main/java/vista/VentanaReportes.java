package vista;

import controlador.EstudianteController;
import controlador.PrestamoController;
import modelo.Estudiante;
import modelo.LibroPrestado;
import modelo.Prestamo;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaReportes extends JFrame {

    private final PrestamoController prestamoControlador = new PrestamoController();
    private final EstudianteController estudianteControlador = new EstudianteController();

    private final DefaultTableModel modeloRanking =
            crearModelo("Posición", "Título", "Autor", "Veces prestado");
    private final DefaultTableModel modeloHistorial =
            crearModelo("ID", "Libro", "Fecha préstamo", "Vence", "Estado");
    private final DefaultTableModel modeloActivos =
            crearModelo("ID", "Libro", "Estudiante", "Fecha préstamo", "Vence", "Estado");

    private final JComboBox<Estudiante> comboEstudiante = new JComboBox<>();
    private final JLabel etiquetaActivos = new JLabel(" ");

    public VentanaReportes() {
        setTitle("Biblioteca Escolar - Reportes");
        setSize(900, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Libros más prestados", crearPestanaRanking());
        pestanas.addTab("Historial por estudiante", crearPestanaHistorial());
        pestanas.addTab("Libros en préstamo", crearPestanaActivos());
        add(pestanas);

        pestanas.addChangeListener(e -> cargarPestana(pestanas.getSelectedIndex()));

        cargarRanking();
        setVisible(true);
    }

    private JPanel crearPestanaRanking() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(crearTabla(modeloRanking, -1)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPestanaHistorial() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        superior.add(new JLabel("Estudiante:"));
        for (Estudiante e : estudianteControlador.listar()) {
            comboEstudiante.addItem(e);
        }
        superior.add(comboEstudiante);
        JButton botonVer = new JButton("Ver historial");
        superior.add(botonVer);
        botonVer.addActionListener(e -> cargarHistorial());

        panel.add(superior, BorderLayout.NORTH);
        panel.add(new JScrollPane(crearTabla(modeloHistorial, 4)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPestanaActivos() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        superior.add(etiquetaActivos);

        panel.add(superior, BorderLayout.NORTH);
        panel.add(new JScrollPane(crearTabla(modeloActivos, 5)), BorderLayout.CENTER);
        return panel;
    }

    private void cargarPestana(int indice) {
        switch (indice) {
            case 0 -> cargarRanking();
            case 1 -> cargarHistorial();
            case 2 -> cargarActivos();
            default -> { }
        }
    }

    private void cargarRanking() {
        modeloRanking.setRowCount(0);
        int posicion = 1;
        for (LibroPrestado lp : prestamoControlador.librosMasPrestados()) {
            modeloRanking.addRow(new Object[]{posicion++, lp.titulo(), lp.autor(), lp.veces()});
        }
    }

    private void cargarHistorial() {
        modeloHistorial.setRowCount(0);
        Estudiante estudiante = (Estudiante) comboEstudiante.getSelectedItem();
        if (estudiante == null) {
            return;
        }
        for (Prestamo p : prestamoControlador.historialDeEstudiante(estudiante.getId())) {
            modeloHistorial.addRow(new Object[]{p.getId(), p.getLibro().getTitulo(),
                    p.getFechaPrestamo(), p.getFechaDevolucion(), p.getEstadoTexto()});
        }
    }

    private void cargarActivos() {
        modeloActivos.setRowCount(0);
        List<Prestamo> activos = prestamoControlador.listarActivos();
        long atrasados = 0;
        for (Prestamo p : activos) {
            if (p.estaAtrasado()) {
                atrasados++;
            }
            modeloActivos.addRow(new Object[]{p.getId(), p.getLibro().getTitulo(),
                    p.getEstudiante().getNombre(), p.getFechaPrestamo(),
                    p.getFechaDevolucion(), p.getEstadoTexto()});
        }
        etiquetaActivos.setText("Libros actualmente en préstamo: " + activos.size()
                + "  |  Atrasados: " + atrasados);
    }

    private static DefaultTableModel crearModelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private static JTable crearTabla(DefaultTableModel modelo, int columnaEstado) {
        JTable tabla = new JTable(modelo);
        if (columnaEstado >= 0) {
            tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object valor,
                                                               boolean seleccionada, boolean foco, int fila, int columna) {
                    Component c = super.getTableCellRendererComponent(
                            t, valor, seleccionada, foco, fila, columna);
                    String estado = String.valueOf(t.getValueAt(fila, columnaEstado));
                    if (!seleccionada) {
                        c.setForeground(estado.startsWith("ATRASADO") ? Color.RED : Color.BLACK);
                    }
                    return c;
                }
            });
        }
        return tabla;
    }
}