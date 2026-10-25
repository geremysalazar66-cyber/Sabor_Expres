package sabor.model;
public record Plato(int id, String nombre, String categoria, double precio, String estado) {
    public boolean disponible() { return Estados.DISPONIBLE.equalsIgnoreCase(estado); }
}
