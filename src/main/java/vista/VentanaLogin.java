package vista;

import controlador.LoginController;
import modelo.Usuario;
import util.ValidacionException;

import javax.swing.*;
import java.awt.*;

public class VentanaLogin extends JFrame {

    private final LoginController controlador = new LoginController();
    private final JTextField campoIdentificador = new JTextField(20);
    private final JPasswordField campoContrasena = new JPasswordField(20);

    public VentanaLogin() {
        setTitle("Biblioteca Escolar - Inicio de sesión");
        setSize(440, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("Sistema de Gestión de Biblioteca Escolar",
                SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 15f));
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(titulo, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridLayout(2, 2, 10, 10));
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 30, 5, 30));
        formulario.add(new JLabel("Correo o RUT:"));
        formulario.add(campoIdentificador);
        formulario.add(new JLabel("Contraseña:"));
        formulario.add(campoContrasena);
        add(formulario, BorderLayout.CENTER);

        JButton botonIngresar = new JButton("Ingresar");
        JPanel panelBoton = new JPanel();
        panelBoton.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panelBoton.add(botonIngresar);
        add(panelBoton, BorderLayout.SOUTH);

        botonIngresar.addActionListener(e -> iniciarSesion());
        getRootPane().setDefaultButton(botonIngresar);

        setVisible(true);
    }

    private void iniciarSesion() {
        try {
            Usuario usuario = controlador.iniciarSesion(
                    campoIdentificador.getText(),
                    new String(campoContrasena.getPassword()));
            dispose();
            new VentanaPrincipal(usuario);
        } catch (ValidacionException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error de acceso", JOptionPane.ERROR_MESSAGE);
            campoContrasena.setText("");
        }
    }
}