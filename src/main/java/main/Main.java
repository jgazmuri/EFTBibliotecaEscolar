package main;

import vista.VentanaEstudiantes;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VentanaEstudiantes::new);
    }
}