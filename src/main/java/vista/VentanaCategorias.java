package vista;

import controlador.CategoriaController;
import modelo.Categoria;
import util.ValidacionException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaCategorias extends JFrame {

    private final CategoriaController controlador = new CategoriaController();
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;
    private final JTextField campoNombre = new JTextField(20);
    private List<Categoria> categorias;

    public VentanaCategorias() {
        setTitle("Biblioteca Escolar - Categorías");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel(new FlowLayout());
        panelFormulario.add(new JLabel("Nombre:"));
        panelFormulario.add(campoNombre);
        add(panelFormulario, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();
        JButton botonAgregar = new JButton("Agregar");
        JButton botonModificar = new JButton("Modificar");
        JButton botonEliminar = new JButton("Eliminar");
        panelBotones.add(botonAgregar);
        panelBotones.add(botonModificar);
        panelBotones.add(botonEliminar);
        add(panelBotones, BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tabla.getSelectedRow();
                if (fila != -1) {
                    campoNombre.setText(categorias.get(fila).getNombre());
                }
            }
        });

        botonAgregar.addActionListener(e -> {
            try {
                controlador.guardar(campoNombre.getText());
                JOptionPane.showMessageDialog(this, "Categoría guardada.");
                campoNombre.setText("");
                cargarDatos();
            } catch (ValidacionException ex) {
                mostrarError(ex.getMessage());
            }
        });

        botonModificar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione una categoría de la tabla.");
                return;
            }
            try {
                controlador.actualizar(categorias.get(fila).getId(), campoNombre.getText());
                JOptionPane.showMessageDialog(this, "Categoría actualizada.");
                campoNombre.setText("");
                cargarDatos();
            } catch (ValidacionException ex) {
                mostrarError(ex.getMessage());
            }
        });

        botonEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione una categoría de la tabla.");
                return;
            }
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Seguro que desea eliminar esta categoría?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                controlador.eliminar(categorias.get(fila).getId());
                JOptionPane.showMessageDialog(this, "Categoría eliminada.");
                campoNombre.setText("");
                cargarDatos();
            } catch (ValidacionException ex) {
                mostrarError(ex.getMessage());
            }
        });

        cargarDatos();
        setVisible(true);
    }

    private void cargarDatos() {
        categorias = controlador.listar();
        modeloTabla.setRowCount(0);
        for (Categoria c : categorias) {
            modeloTabla.addRow(new Object[]{c.getId(), c.getNombre()});
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}