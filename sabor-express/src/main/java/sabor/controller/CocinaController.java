package sabor.controller;

import java.sql.SQLException;
import java.util.List;
import sabor.dao.PedidoDAO;
import sabor.model.Estados;
import sabor.model.Pedido;
import sabor.service.PedidoNotifier;

public class CocinaController {
    private final PedidoDAO dao = new PedidoDAO();

    public List<Pedido> activos() throws SQLException {
        return dao.listarPorEstados(Estados.PENDIENTE, Estados.EN_PREPARACION, Estados.LISTO);
    }

    public void iniciarPreparacion(Pedido p) throws SQLException { mover(p, Estados.PENDIENTE, Estados.EN_PREPARACION); }
    public void marcarListo(Pedido p) throws SQLException { mover(p, Estados.EN_PREPARACION, Estados.LISTO); }

    private void mover(Pedido p, String desde, String hacia) throws SQLException {
        boolean ok = dao.cambiarEstado(p.id, desde, hacia);
        PedidoNotifier.notificar();
        if (!ok) throw new IllegalStateException("La comanda " + p.codigo() + " ya cambió de estado (puede haber sido actualizada en otra terminal).");
    }
}
