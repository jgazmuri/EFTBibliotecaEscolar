package vista;

import controlador.CategoriaController;
import controlador.LibroController;
import modelo.Categoria;
import modelo.Libro;
import modelo.Usuario;
import util.ValidacionException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VentanaLibros extends JFrame {

    private final LibroController libroControlador = new LibroController();
    private final CategoriaController categoriaControlador = new CategoriaController();
    private final Usuario usuario;

    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    private final JTextField campoBuscar = new JTextField(20);
    private final JTextField campoTitulo = new JTextField();
    private final JTextField campoAutor = new JTextField();
    private final JTextField campoIsbn = new JTextField();
    private final JTextField campoEditorial = new JTextField();
    private final JTextField campoStock = new JTextField();
    private final JComboBox<Categoria> comboCategoria = new JComboBox<>();

    private List<Libro> mostrados = new ArrayList<>();

    public VentanaLibros(Usuario usuario) {
        this.usuario = usuario;

        setTitle("Biblioteca Escolar - Libros");
        setSize(950, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.add(new JLabel("Buscar (título o autor):"));
        panelBusqueda.add(campoBuscar);
        JButton botonBuscar = new JButton("Buscar");
        JButton botonVerTodos = new JButton("Ver todos");
        panelBusqueda.add(botonBuscar);
        panelBusqueda.add(botonVerTodos);
        add(panelBusqueda, BorderLayout.NORTH);

        String[] columnas = {"ID", "Título", "Autor", "ISBN", "Editorial", "Stock", "Categoría"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        botonBuscar.addActionListener(e -> cargarDatos(campoBuscar.getText()));
        botonVerTodos.addActionListener(e -> {
            campoBuscar.setText("");
            cargarDatos("");
        });

        if (usuario.esBibliotecario()) {
            add(crearPanelEdicion(), BorderLayout.SOUTH);
        }

        cargarDatos("");
        setVisible(true);
    }

    private JPanel crearPanelEdicion() {
        JPanel formulario = new JPanel(new GridLayout(3, 4, 10, 8));
        formulario.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        formulario.add(new JLabel("Título:"));
        formulario.add(campoTitulo);
        formulario.add(new JLabel("Autor:"));
        formulario.add(campoAutor);
        formulario.add(new JLabel("ISBN:"));
        formulario.add(campoIsbn);
        formulario.add(new JLabel("Editorial:"));
        formulario.add(campoEditorial);
        formulario.add(new JLabel("Stock:"));
        formulario.add(campoStock);
        formulario.add(new JLabel("Categoría:"));
        formulario.add(comboCategoria);

        JPanel botones = new JPanel();
        JButton botonAgregar = new JButton("Agregar");
        JButton botonModificar = new JButton("Modificar");
        JButton botonEliminar = new JButton("Eliminar");
        JButton botonLimpiar = new JButton("Limpiar");
        botones.add(botonAgregar);
        botones.add(botonModificar);
        botones.add(botonEliminar);
        botones.add(botonLimpiar);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(formulario, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);

        cargarCategorias();

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tabla.getSelectedRow();
                if (fila != -1) {
                    cargarFormulario(mostrados.get(fila));
                }
            }
        });

        botonAgregar.addActionListener(e -> {
            try {
                libroControlador.guardar(campoTitulo.getText(), campoAutor.getText(),
                        campoIsbn.getText(), campoEditorial.getText(), campoStock.getText(),
                        (Categoria) comboCategoria.getSelectedItem());
                JOptionPane.showMessageDialog(this, "Libro guardado.");
                limpiarFormulario();
                cargarDatos(campoBuscar.getText());
            } catch (ValidacionException ex) {
                mostrarError(ex.getMessage());
            }
        });

        botonModificar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un libro de la tabla.");
                return;
            }
            try {
                libroControlador.actualizar(mostrados.get(fila).getId(),
                        campoTitulo.getText(), campoAutor.getText(), campoIsbn.getText(),
                        campoEditorial.getText(), campoStock.getText(),
                        (Categoria) comboCategoria.getSelectedItem());
                JOptionPane.showMessageDialog(this, "Libro actualizado.");
                limpiarFormulario();
                cargarDatos(campoBuscar.getText());
            } catch (ValidacionException ex) {
                mostrarError(ex.getMessage());
            }
        });

        botonEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un libro de la tabla.");
                return;
            }
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Seguro que desea eliminar este libro?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                libroControlador.eliminar(mostrados.get(fila).getId());
                JOptionPane.showMessageDialog(this, "Libro eliminado.");
                limpiarFormulario();
                cargarDatos(campoBuscar.getText());
            } catch (ValidacionException ex) {
                mostrarError(ex.getMessage());
            }
        });

        botonLimpiar.addActionListener(e -> {
            tabla.clearSelection();
            limpiarFormulario();
        });

        return panel;
    }

    private void cargarCategorias() {
        comboCategoria.removeAllItems();
        for (Categoria c : categoriaControlador.listar()) {
            comboCategoria.addItem(c);
        }
    }

    private void cargarDatos(String filtro) {
        String texto = filtro == null ? "" : filtro.trim().toLowerCase();

        mostrados = new ArrayList<>();
        for (Libro l : libroControlador.listar()) {
            if (texto.isEmpty()
                    || l.getTitulo().toLowerCase().contains(texto)
                    || l.getAutor().toLowerCase().contains(texto)) {
                mostrados.add(l);
            }
        }

        modeloTabla.setRowCount(0);
        for (Libro l : mostrados) {
            String categoria = l.getCategoria() != null ? l.getCategoria().getNombre() : "";
            modeloTabla.addRow(new Object[]{l.getId(), l.getTitulo(), l.getAutor(),
                    l.getIsbn(), l.getEditorial(), l.getStock(), categoria});
        }
    }

    private void cargarFormulario(Libro libro) {
        campoTitulo.setText(libro.getTitulo());
        campoAutor.setText(libro.getAutor());
        campoIsbn.setText(libro.getIsbn());
        campoEditorial.setText(libro.getEditorial());
        campoStock.setText(String.valueOf(libro.getStock()));
        seleccionarCategoria(libro.getCategoria());
    }

    private void seleccionarCategoria(Categoria categoria) {
        if (categoria == null) {
            return;
        }
        for (int i = 0; i < comboCategoria.getItemCount(); i++) {
            if (comboCategoria.getItemAt(i).getId() == categoria.getId()) {
                comboCategoria.setSelectedIndex(i);
                return;
            }
        }
    }

    private void limpiarFormulario() {
        campoTitulo.setText("");
        campoAutor.setText("");
        campoIsbn.setText("");
        campoEditorial.setText("");
        campoStock.setText("");
        if (comboCategoria.getItemCount() > 0) {
            comboCategoria.setSelectedIndex(0);
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}