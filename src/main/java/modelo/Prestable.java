package modelo;

public interface Prestable {

    boolean estaDisponible();

    void descontarStock();

    void reponerStock();
}