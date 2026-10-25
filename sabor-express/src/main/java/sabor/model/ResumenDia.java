package sabor.model;
public record ResumenDia(double ventas, int pedidos, double mayor, double efectivo, double tarjeta, double yape) {
    public double ticketPromedio() { return pedidos == 0 ? 0 : ventas / pedidos; }
}
