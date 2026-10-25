package sabor.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import sabor.controller.PedidoController;
import sabor.model.Estados;
import sabor.model.ItemPedido;
import sabor.model.Mesa;
import sabor.model.Pedido;
import sabor.model.Usuario;
import sabor.service.PedidoNotifier;
import sabor.view.ui.Theme;
import sabor.view.ui.Ui;

/** Rol Mozo: mapa de mesas y toma de pedidos. */
public class MozoView extends JPanel implements Detenible {
    private final PedidoController ctrl = new PedidoController();
    private final HeaderBar header;
    private final CardLayout cl = new CardLayout();
    private final JPanel cuerpo = new JPanel(cl);
    private final MesasPanel mesas;
    private final PedidoPanel pedido;
    private final Timer sondeo;
    private final Runnable observador;
    private boolean enMesas = true;

    public MozoView(Usuario u, Runnable cerrarSesion) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        header = new HeaderBar("Toma de Pedidos", u, cerrarSesion);
        mesas = new MesasPanel(ctrl, this::seleccionar);
        pedido = new PedidoPanel(ctrl, u, this::volverAMesas);
        cuerpo.setOpaque(false);
        cuerpo.add(mesas, "mesas");
        cuerpo.add(pedido, "pedido");
        add(header, BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);

        observador = () -> { if (enMesas) mesas.refrescar(); };
        PedidoNotifier.suscribir(observador);
        sondeo = new Timer(3000, e -> observador.run());
        sondeo.start();
        mesas.refrescar();
    }

    private void seleccionar(Mesa m) {
        if (Estados.LIBRE.equals(m.estado())) {
            pedido.abrir(m);
            enMesas = false;
            cl.show(cuerpo, "pedido");
            return;
        }
        Ui.seguro(this, () -> {
            Pedido p = ctrl.pedidoDeMesa(m);
            StringBuilder sb = new StringBuilder("Mesa " + m.numero() + " — " + (Estados.OCUPADA.equals(m.estado()) ? "Ocupada" : "Por pagar") + "\n\n");
            if (p == null) sb.append("No hay un pedido activo asociado.");
            else {
                sb.append(p.codigo()).append("  ·  ").append(textoEstado(p.estado)).append("\n");
                for (ItemPedido i : p.items) sb.append("  • ").append(i.nombre).append(" × ").append(i.cantidad).append('\n');
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Estado de la mesa", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private static String textoEstado(String e) {
        return switch (e) {
            case Estados.PENDIENTE -> "Pendiente";
            case Estados.EN_PREPARACION -> "En preparación";
            case Estados.LISTO -> "Listo para servir";
            default -> e;
        };
    }

    private void volverAMesas() {
        enMesas = true;
        cl.show(cuerpo, "mesas");
        mesas.refrescar();
    }

    @Override public void detener() {
        header.detener();
        sondeo.stop();
        PedidoNotifier.cancelar(observador);
    }
}
