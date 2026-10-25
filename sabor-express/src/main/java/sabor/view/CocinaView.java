package sabor.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import sabor.controller.CocinaController;
import sabor.model.Estados;
import sabor.model.ItemPedido;
import sabor.model.Pedido;
import sabor.model.Usuario;
import sabor.service.PedidoNotifier;
import sabor.util.Fmt;
import sabor.view.ui.Emojis;
import sabor.view.ui.Pill;
import sabor.view.ui.RoundedButton;
import sabor.view.ui.RoundedPanel;
import sabor.view.ui.Theme;
import sabor.view.ui.Ui;

/** Rol Cocinero: Monitor de Cocina (KDS) con tres columnas de estado. */
public class CocinaView extends JPanel implements Detenible {
    private final CocinaController ctrl = new CocinaController();
    private final HeaderBar header;
    private final JPanel[] columnas = new JPanel[3];
    private final Pill[] contadores = new Pill[3];
    private final JLabel lblActivas = Ui.etiqueta("0 órdenes activas", Font.PLAIN, 13f, Theme.MUTED);
    private final JLabel lblEstado = Ui.etiqueta("● Sistema en línea", Font.PLAIN, 13f, Theme.GREEN_BTN);
    private final Timer sondeo;
    private final Runnable observador = this::refrescar;
    private String firma = "";

    public CocinaView(Usuario u, Runnable cerrarSesion) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        header = new HeaderBar("Monitor de Cocina (KDS)", u, cerrarSesion);
        add(header, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(new EmptyBorder(22, 28, 22, 28));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel tit = Ui.vertical();
        tit.add(Ui.izq(Ui.etiqueta("Monitor de Cocina", Font.BOLD, 24f, Theme.TEXT)));
        tit.add(Ui.izq(lblActivas));
        top.add(tit, BorderLayout.WEST);
        JPanel est = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        est.setOpaque(false);
        est.add(lblEstado);
        top.add(est, BorderLayout.EAST);
        cuerpo.add(top, BorderLayout.NORTH);

        JPanel cols = new JPanel(new GridLayout(1, 3, 16, 0));
        cols.setOpaque(false);
        String[] tit3 = {"Pendiente", "En Preparación", "Listo para Servir"};
        String[] ico = {"⏳", "🔥", "✅"};
        Color[] fg = {Theme.ORANGE, Theme.BLUE, Theme.GREEN};
        Color[] bg = {new Color(0xFFFBF5), new Color(0xF7FAFF), new Color(0xF5FDF9)};
        Color[] ln = {Theme.ORANGE_LINE, Theme.BLUE_LINE, Theme.GREEN_LINE};
        Color[] pbg = {Theme.ORANGE_BG, Theme.BLUE_SOFT, Theme.GREEN_BG};
        for (int i = 0; i < 3; i++) {
            RoundedPanel col = new RoundedPanel(bg[i], ln[i], 18);
            col.setLayout(new BorderLayout(0, 10));
            col.setBorder(new EmptyBorder(14, 14, 14, 14));
            JPanel cab = new JPanel(new BorderLayout());
            cab.setOpaque(false);
            JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            izq.setOpaque(false);
            izq.add(Ui.emoji(ico[i], 16f));
            izq.add(Ui.etiqueta(tit3[i], Font.BOLD, 14f, fg[i]));
            cab.add(izq, BorderLayout.WEST);
            contadores[i] = new Pill("0", fg[i], pbg[i], ln[i]);
            cab.add(contadores[i], BorderLayout.EAST);
            col.add(cab, BorderLayout.NORTH);
            columnas[i] = Ui.vertical();
            JPanel env = new JPanel(new BorderLayout());
            env.setOpaque(false);
            env.add(columnas[i], BorderLayout.NORTH);
            col.add(Ui.scroll(env), BorderLayout.CENTER);
            cols.add(col);
        }
        cuerpo.add(cols, BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);

        PedidoNotifier.suscribir(observador);
        sondeo = new Timer(3000, e -> refrescar());
        sondeo.start();
        refrescar();
    }

    private void refrescar() {
        boolean ok = Ui.silencioso(() -> {
            List<Pedido> ps = ctrl.activos();
            StringBuilder sb = new StringBuilder();
            for (Pedido p : ps) sb.append(p.id).append(p.estado).append(Fmt.hace(p.fecha)).append(';');
            if (sb.toString().equals(firma)) return;
            firma = sb.toString();

            List<List<Pedido>> grupos = new ArrayList<>();
            for (int i = 0; i < 3; i++) grupos.add(new ArrayList<>());
            for (Pedido p : ps) {
                switch (p.estado) {
                    case Estados.PENDIENTE -> grupos.get(0).add(p);
                    case Estados.EN_PREPARACION -> grupos.get(1).add(p);
                    default -> grupos.get(2).add(p);
                }
            }
            for (int i = 0; i < 3; i++) {
                columnas[i].removeAll();
                for (Pedido p : grupos.get(i)) {
                    columnas[i].add(new TarjetaOrden(p, i));
                    columnas[i].add(Ui.hueco(12));
                }
                contadores[i].setTexto(String.valueOf(grupos.get(i).size()));
                columnas[i].revalidate();
                columnas[i].repaint();
            }
            lblActivas.setText(ps.size() + (ps.size() == 1 ? " orden activa" : " órdenes activas"));
        });
        lblEstado.setText(ok ? "● Sistema en línea" : "● Sin conexión");
        lblEstado.setForeground(ok ? Theme.GREEN_BTN : Theme.RED);
    }

    private class TarjetaOrden extends RoundedPanel {
        TarjetaOrden(Pedido p, int col) {
            super(Theme.WHITE, col == 0 ? Theme.ORANGE_LINE : col == 1 ? Theme.BLUE_LINE : Theme.GREEN_LINE, 14);
            flexible();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(12, 14, 14, 14));

            JPanel cab = new JPanel(new BorderLayout());
            cab.setOpaque(false);
            cab.add(Ui.etiqueta(p.codigo(), Font.BOLD, 11f, Theme.MUTED), BorderLayout.WEST);
            cab.add(new Pill(Fmt.hace(p.fecha), Theme.MUTED, Theme.BG, null), BorderLayout.EAST);
            add(Ui.ancho(cab));
            add(Ui.hueco(2));
            add(Ui.izq(Ui.etiqueta("Mesa " + p.numeroMesa, Font.BOLD, 17f, Theme.TEXT)));
            add(Ui.hueco(8));
            for (ItemPedido i : p.items) {
                JPanel f = new JPanel(new BorderLayout(8, 0));
                f.setOpaque(false);
                JPanel nombre = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                nombre.setOpaque(false);
                nombre.add(Ui.emoji(Emojis.paraPlato(i.nombre, ""), 14f));
                nombre.add(Ui.etiqueta(i.nombre, Font.PLAIN, 13f, Theme.TEXT));
                f.add(nombre, BorderLayout.WEST);
                f.add(Ui.etiqueta("× " + i.cantidad, Font.BOLD, 12f, Theme.MUTED), BorderLayout.EAST);
                add(Ui.ancho(f));
                add(Ui.hueco(2));
            }
            add(Ui.hueco(10));
            RoundedButton b;
            if (col == 0) {
                b = new RoundedButton("🔥", "Iniciar Preparación", RoundedButton.Estilo.PRIMARIO);
                b.addActionListener(e -> Ui.seguro(CocinaView.this, () -> { ctrl.iniciarPreparacion(p); firma = ""; refrescar(); }));
            } else if (col == 1) {
                b = new RoundedButton("✅", "Marcar como Listo", RoundedButton.Estilo.EXITO);
                b.addActionListener(e -> Ui.seguro(CocinaView.this, () -> { ctrl.marcarListo(p); firma = ""; refrescar(); }));
            } else {
                b = new RoundedButton("🛎", "Listo para servir", RoundedButton.Estilo.SUAVE);
                b.setEnabled(false);
            }
            add(Ui.ancho(b));
        }
    }

    @Override public void detener() {
        header.detener();
        sondeo.stop();
        PedidoNotifier.cancelar(observador);
    }
}
