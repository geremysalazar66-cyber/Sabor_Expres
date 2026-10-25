package sabor.model;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import sabor.util.Fmt;

public class Pedido {
    public final int id, idMesa, numeroMesa;
    public final String estado;
    public final LocalDateTime fecha;
    public final double total;
    public final List<ItemPedido> items = new ArrayList<>();

    public Pedido(int id, int idMesa, int numeroMesa, String estado, LocalDateTime fecha, double total) {
        this.id = id; this.idMesa = idMesa; this.numeroMesa = numeroMesa;
        this.estado = estado; this.fecha = fecha; this.total = total;
    }
    public String codigo() { return Fmt.codigo(id); }
    public int cantidadArticulos() { return items.stream().mapToInt(i -> i.cantidad).sum(); }
}
