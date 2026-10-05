package vista;

import controlador.EstudianteController;
import modelo.Estudiante;
import util.ValidacionException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaEstudiantes extends JFrame {

    private final EstudianteController controlador = new EstudianteController();

    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    private final JTextField campoNombre = new JTextField();
    private final JTextField campoRut = new JTextField();
    private final JTextField campoCurso = new JTextField();
    private final JTextField campoCorreo = new JTextField();
    private final JPasswordField campoContrasena = new JPasswordField();

    private List<Estudiante> estudiantes;

    public VentanaEstudiantes() {
        setTitle("Biblioteca Escolar - Estudiantes");
        setSize(900, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        String[] columnas = {"ID", "Nombre", "RUT", "Curso", "Correo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridLayout(3, 4, 10, 8));
        formulario.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);
        formulario.add(new JLabel("RUT (12345678-9):"));
        formulario.add(campoRut);
        formulario.add(new JLabel("Curso:"));
        formulario.add(campoCurso);
        formulario.add(new JLabel("Correo:"));
        formulario.add(campoCorreo);
        formulario.add(new JLabel("Contraseña (sólo al registrar):"));
        formulario.add(campoContrasena);

        JPanel botones = new JPanel();
        JButton botonRegistrar = new JButton("Registrar");
        JButton botonModificar = new JButton("Modificar");
        JButton botonEliminar = new JButton("Eliminar");
        JButton botonLimpiar = new JButton("Limpiar");
        botones.add(botonRegistrar);
        botones.add(botonModificar);
        botones.add(botonEliminar);
        botones.add(botonLimpiar);

        JPanel panelSur = new JPanel(new BorderLayout(5, 5));
        panelSur.add(formulario, BorderLayout.CENTER);
        panelSur.add(botones, BorderLayout.SOUTH);
        add(panelSur, BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tabla.getSelectedRow();
                if (fila != -1) {
                    cargarFormulario(estudiantes.get(fila));
                }
            }
        });

        botonRegistrar.addActionListener(e -> {
            if (tabla.getSelectedRow() != -1) {
                JOptionPane.showMessageDialog(this,
                        "Pulse Limpiar antes de registrar un estudiante nuevo.");
                return;
            }
            try {
                controlador.guardar(campoNombre.getText(), campoRut.getText(),
                        campoCurso.getText(), campoCorreo.getText(),
                        new String(campoContrasena.getPassword()));
                JOptionPane.showMessageDialog(this, "Estudiante registrado.");
                limpiarFormulario();
                cargarDatos();
            } catch (ValidacionException ex) {
                mostrarError(ex.getMessage());
            }
        });

        botonModificar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un estudiante de la tabla.");
                return;
            }
            Estudiante seleccionado = estudiantes.get(fila);
            try {
                controlador.actualizar(seleccionado.getId(), seleccionado.getRut(),
                        campoNombre.getText(), campoCurso.getText(), campoCorreo.getText());
                JOptionPane.showMessageDialog(this, "Estudiante actualizado.");
                limpiarFormulario();
                cargarDatos();
            } catch (ValidacionException ex) {
                mostrarError(ex.getMessage());
            }
        });

        botonEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un estudiante de la tabla.");
                return;
            }
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Seguro que desea eliminar a este estudiante?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                controlador.eliminar(estudiantes.get(fila));
                JOptionPane.showMessageDialog(this, "Estudiante eliminado.");
                limpiarFormulario();
                cargarDatos();
            } catch (ValidacionException ex) {
                mostrarError(ex.getMessage());
            }
        });

        botonLimpiar.addActionListener(e -> limpiarFormulario());

        cargarDatos();
        setVisible(true);
    }

    private void cargarDatos() {
        estudiantes = controlador.listar();
        modeloTabla.setRowCount(0);
        for (Estudiante est : estudiantes) {
            modeloTabla.addRow(new Object[]{est.getId(), est.getNombre(), est.getRut(),
                    est.getCurso(), est.getCorreo()});
        }
    }

    private void cargarFormulario(Estudiante est) {
        campoNombre.setText(est.getNombre());
        campoRut.setText(est.getRut());
        campoCurso.setText(est.getCurso());
        campoCorreo.setText(est.getCorreo());
        campoContrasena.setText("");
        campoRut.setEditable(false);
        campoContrasena.setEnabled(false);
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        campoNombre.setText("");
        campoRut.setText("");
        campoCurso.setText("");
        campoCorreo.setText("");
        campoContrasena.setText("");
        campoRut.setEditable(true);
        campoContrasena.setEnabled(true);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}