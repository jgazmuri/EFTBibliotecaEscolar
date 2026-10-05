package vista;

import modelo.Usuario;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal(Usuario usuario) {
        setTitle("Biblioteca Escolar - Menú principal");
        setSize(420, usuario.esBibliotecario() ? 500 : 340);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel bienvenida = new JLabel(
                "Bienvenido(a), " + usuario.getNombre() + " (" + usuario.getRol() + ")",
                SwingConstants.CENTER);
        bienvenida.setFont(bienvenida.getFont().deriveFont(Font.BOLD, 15f));
        bienvenida.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(bienvenida, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new GridLayout(0, 1, 10, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        JButton botonLibros = new JButton(
                usuario.esBibliotecario() ? "Gestión de libros" : "Consultar libros");
        JButton botonPrestamos = new JButton("Préstamos y devoluciones");
        panelBotones.add(botonLibros);
        panelBotones.add(botonPrestamos);

        botonLibros.addActionListener(e -> new VentanaLibros(usuario));
        botonPrestamos.addActionListener(e -> new VentanaPrestamos(usuario));

        if (usuario.esBibliotecario()) {
            JButton botonCategorias = new JButton("Categorías");
            JButton botonEstudiantes = new JButton("Estudiantes");
            JButton botonReportes = new JButton("Reportes");
            panelBotones.add(botonCategorias);
            panelBotones.add(botonEstudiantes);
            panelBotones.add(botonReportes);

            botonCategorias.addActionListener(e -> new VentanaCategorias());
            botonEstudiantes.addActionListener(e -> new VentanaEstudiantes());
            botonReportes.addActionListener(e -> new VentanaReportes());
        }

        JButton botonCerrarSesion = new JButton("Cerrar sesión");
        JButton botonSalir = new JButton("Salir");
        panelBotones.add(botonCerrarSesion);
        panelBotones.add(botonSalir);

        botonCerrarSesion.addActionListener(e -> {
            for (Window ventana : Window.getWindows()) {
                ventana.dispose();
            }
            new VentanaLogin();
        });
        botonSalir.addActionListener(e -> System.exit(0));

        add(panelBotones, BorderLayout.CENTER);
        setVisible(true);
    }
}