package modelo;

public enum Rol {
    BIBLIOTECARIO,
    ESTUDIANTE;

    public static Rol desdeBD(String valor) {
        return Rol.valueOf(valor.toUpperCase());
    }

    public String paraBD() {
        return name().toLowerCase();
    }
}