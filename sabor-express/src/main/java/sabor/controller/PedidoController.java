package sabor.controller;

import java.sql.SQLException;
import java.util.List;
import sabor.dao.MenuDAO;
import sabor.dao.MesaDAO;
import sabor.dao.PedidoDAO;
import sabor.model.ItemPedido;
import sabor.model.Mesa;
import sabor.model.Pedido;
import sabor.model.Plato;
import sabor.model.Usuario;
import sabor.service.PedidoNotifier;

public class PedidoController {
    private final MesaDAO mesas = new MesaDAO();
    private final MenuDAO menu = new MenuDAO();
    private final PedidoDAO pedidos = new PedidoDAO();

    public List<Mesa> mesas() throws SQLException { return mesas.listar(); }
    public List<Plato> platos() throws SQLException { return menu.listar(); }
    public Pedido pedidoDeMesa(Mesa m) throws SQLException { return pedidos.activoDeMesa(m.id()); }

    public int enviarACocina(Mesa mesa, Usuario mozo, List<ItemPedido> items) throws SQLException {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Agrega al menos un ítem al pedido.");
        int id = pedidos.crear(mesa.id(), mozo.id(), items);
        PedidoNotifier.notificar();
        return id;
    }
}
