package sabor.model;
public class ItemPedido {
    public final int idMenu;
    public final String nombre;
    public final double precio;
    public int cantidad;

    public ItemPedido(int idMenu, String nombre, double precio, int cantidad) {
        this.idMenu = idMenu; this.nombre = nombre; this.precio = precio; this.cantidad = cantidad;
    }
    public double subtotal() { return Math.round(precio * cantidad * 100.0) / 100.0; }
}
