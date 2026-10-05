package vista;

import controlador.EstudianteController;
import controlador.LibroController;
import controlador.PrestamoController;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;
import modelo.Usuario;
import util.ValidacionException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VentanaPrestamos extends JFrame {

    private static final int COLUMNA_ESTADO = 5;

    private final PrestamoController prestamoControlador = new PrestamoController();
    private final LibroController libroControlador = new LibroController();
    private final EstudianteController estudianteControlador = new EstudianteController();
    private final Usuario usuario;
    private Estudiante estudianteActual;

    private final JComboBox<Estudiante> comboEstudiante = new JComboBox<>();
    private final JComboBox<String> comboLibro = new JComboBox<>();
    private final JLabel etiquetaProceso = new JLabel("Préstamos en proceso: 0");
    private final JCheckBox checkPendientes = new JCheckBox("Mostrar sólo pendientes");

    private DefaultTableModel modeloTabla;
    private JTable tabla;

    private List<Libro> libros = new ArrayList<>();
    private List<Prestamo> mostrados = new ArrayList<>();
    private int enProceso = 0;

    public VentanaPrestamos(Usuario usuario) {
        this.usuario = usuario;

        if (!usuario.esBibliotecario()) {
            estudianteActual = estudianteControlador.buscarPorRut(usuario.getRut());
            if (estudianteActual == null) {
                JOptionPane.showMessageDialog(null,
                        "Su usuario no tiene un estudiante asociado.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
        }

        setTitle("Biblioteca Escolar - Préstamos y devoluciones");
        setSize(1000, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);
        add(crearPanelInferior(), BorderLayout.SOUTH);

        cargarLibros();
        cargarPrestamos();
        setVisible(true);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));

        panel.add(new JLabel("Estudiante:"));
        if (usuario.esBibliotecario()) {
            for (Estudiante e : estudianteControlador.listar()) {
                comboEstudiante.addItem(e);
            }
        } else {
            comboEstudiante.addItem(estudianteActual);
            comboEstudiante.setEnabled(false);
        }
        panel.add(comboEstudiante);

        panel.add(new JLabel("Libro:"));
        comboLibro.setPreferredSize(new Dimension(300, 26));
        panel.add(comboLibro);

        JButton botonPrestar = new JButton("Registrar préstamo");
        panel.add(botonPrestar);
        panel.add(etiquetaProceso);

        botonPrestar.addActionListener(e -> {
            Estudiante estudiante = (Estudiante) comboEstudiante.getSelectedItem();
            int indice = comboLibro.getSelectedIndex();
            Libro libro = indice >= 0 ? libros.get(indice) : null;
            try {
                prestamoControlador.registrarPrestamo(estudiante, libro, this::alTerminarPrestamo);
                enProceso++;
                actualizarEtiquetaProceso();
            } catch (ValidacionException ex) {
                mostrarError(ex.getMessage());
            }
        });

        return panel;
    }

    private JScrollPane crearTabla() {
        String[] columnas = {"ID", "Estudiante", "Libro", "Fecha préstamo", "Vence", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                                                           boolean seleccionada, boolean foco, int fila, int columna) {
                Component c = super.getTableCellRendererComponent(
                        t, valor, seleccionada, foco, fila, columna);
                String estado = String.valueOf(t.getValueAt(fila, COLUMNA_ESTADO));
                if (!seleccionada) {
                    c.setForeground(estado.startsWith("ATRASADO") ? Color.RED : Color.BLACK);
                }
                return c;
            }
        });

        return new JScrollPane(tabla);
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));

        JButton botonDevolver = new JButton("Registrar devolución");
        JButton botonActualizar = new JButton("Actualizar");
        panel.add(checkPendientes);
        panel.add(botonDevolver);
        panel.add(botonActualizar);

        checkPendientes.addActionListener(e -> cargarPrestamos());
        botonActualizar.addActionListener(e -> {
            cargarLibros();
            cargarPrestamos();
        });

        botonDevolver.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un préstamo de la tabla.");
                return;
            }
            Prestamo prestamo = mostrados.get(fila);
            if (prestamo.isDevuelto()) {
                JOptionPane.showMessageDialog(this, "Este préstamo ya fue devuelto.");
                return;
            }
            try {
                prestamoControlador.registrarDevolucion(prestamo.getId());
                String aviso = "Devolución registrada.";
                if (prestamo.estaAtrasado()) {
                    aviso += " El libro se devolvió con " + prestamo.diasAtraso() + " días de atraso.";
                }
                JOptionPane.showMessageDialog(this, aviso);
                cargarLibros();
                cargarPrestamos();
            } catch (ValidacionException ex) {
                mostrarError(ex.getMessage());
            }
        });

        return panel;
    }

    private void alTerminarPrestamo(boolean exito, String mensaje) {
        enProceso--;
        actualizarEtiquetaProceso();
        if (exito) {
            JOptionPane.showMessageDialog(this, mensaje, "Préstamo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            mostrarError(mensaje);
        }
        cargarLibros();
        cargarPrestamos();
    }

    private void actualizarEtiquetaProceso() {
        etiquetaProceso.setText("Préstamos en proceso: " + enProceso);
    }

    private void cargarLibros() {
        int seleccionado = comboLibro.getSelectedIndex();
        libros = libroControlador.listar();
        comboLibro.removeAllItems();
        for (Libro l : libros) {
            comboLibro.addItem(l.getTitulo() + " (stock: " + l.getStock() + ")");
        }
        if (seleccionado >= 0 && seleccionado < libros.size()) {
            comboLibro.setSelectedIndex(seleccionado);
        }
    }

    private void cargarPrestamos() {
        List<Prestamo> todos = usuario.esBibliotecario()
                ? prestamoControlador.listarTodos()
                : prestamoControlador.historialDeEstudiante(estudianteActual.getId());

        mostrados = new ArrayList<>();
        for (Prestamo p : todos) {
            if (!checkPendientes.isSelected() || !p.isDevuelto()) {
                mostrados.add(p);
            }
        }

        modeloTabla.setRowCount(0);
        for (Prestamo p : mostrados) {
            modeloTabla.addRow(new Object[]{p.getId(), p.getEstudiante().getNombre(),
                    p.getLibro().getTitulo(), p.getFechaPrestamo(), p.getFechaDevolucion(),
                    textoEstado(p)});
        }
    }

    private String textoEstado(Prestamo p) {
        if (p.isDevuelto()) {
            return "Devuelto";
        }
        if (p.estaAtrasado()) {
            return "ATRASADO (" + p.diasAtraso() + " días)";
        }
        return "Pendiente";
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}