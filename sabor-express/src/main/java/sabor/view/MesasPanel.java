package sabor.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import sabor.controller.PedidoController;
import sabor.model.Estados;
import sabor.model.Mesa;
import sabor.view.ui.Pill;
import sabor.view.ui.RoundedPanel;
import sabor.view.ui.Theme;
import sabor.view.ui.Ui;

/** Mapa de mesas (Libre / Ocupada / Por Pagar). */
public class MesasPanel extends JPanel {
    private final PedidoController ctrl;
    private final Consumer<Mesa> alElegir;
    private final JPanel grilla = new JPanel(new GridLayout(0, 4, 14, 14));
    private final JLabel nLibre = Ui.etiqueta("0", Font.BOLD, 28f, Theme.GREEN);
    private final JLabel nOcupada = Ui.etiqueta("0", Font.BOLD, 28f, Theme.ORANGE);
    private final JLabel nPorPagar = Ui.etiqueta("0", Font.BOLD, 28f, Theme.RED);
    private List<Mesa> ultimas = new ArrayList<>();

    public MesasPanel(PedidoController ctrl, Consumer<Mesa> alElegir) {
        this.ctrl = ctrl;
        this.alElegir = alElegir;
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.setOpaque(false);
        contenido.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel tit = Ui.vertical();
        tit.add(Ui.izq(Ui.etiqueta("Mapa de Mesas", Font.BOLD, 24f, Theme.TEXT)));
        tit.add(Ui.izq(Ui.etiqueta("Selecciona una mesa para tomar un pedido.", Font.PLAIN, 13f, Theme.MUTED)));
        top.add(tit, BorderLayout.WEST);
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        leyenda.setOpaque(false);
        leyenda.add(punto("Libre", Theme.GREEN_LINE.darker()));
        leyenda.add(punto("Ocupada", Theme.ORANGE_DOT));
        leyenda.add(punto("Por Pagar", Theme.RED_DOT));
        top.add(leyenda, BorderLayout.EAST);
        contenido.add(top, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(Theme.WHITE, Theme.LINE, 20);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        grilla.setOpaque(false);
        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(grilla, BorderLayout.NORTH);
        card.add(envoltura, BorderLayout.CENTER);
        contenido.add(Ui.scroll(card), BorderLayout.CENTER);

        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));
        stats.setOpaque(false);
        stats.add(stat(nLibre, "Libre", Theme.GREEN_BG, Theme.GREEN_LINE, Theme.GREEN));
        stats.add(stat(nOcupada, "Ocupada", Theme.ORANGE_BG, Theme.ORANGE_LINE, Theme.ORANGE));
        stats.add(stat(nPorPagar, "Por Pagar", Theme.RED_BG, Theme.RED_LINE, Theme.RED));
        contenido.add(stats, BorderLayout.SOUTH);
        add(contenido, BorderLayout.CENTER);
    }

    private JPanel punto(String texto, Color c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillOval(0, 0, 10, 10);
                g2.dispose();
            }
        };
        d.setOpaque(false);
        d.setPreferredSize(new Dimension(10, 10));
        p.add(d);
        p.add(Ui.etiqueta(texto, Font.PLAIN, 13f, Theme.MUTED));
        return p;
    }

    private RoundedPanel stat(JLabel numero, String texto, Color fondo, Color borde, Color fg) {
        RoundedPanel p = new RoundedPanel(fondo, borde, 16);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(14, 18, 14, 18));
        numero.setAlignmentX(LEFT_ALIGNMENT);
        p.add(numero);
        p.add(Ui.izq(Ui.etiqueta(texto, Font.PLAIN, 13f, fg)));
        return p;
    }

    public void refrescar() {
        Ui.silencioso(() -> {
            List<Mesa> ms = ctrl.mesas();
            if (ms.equals(ultimas)) return;
            ultimas = ms;
            grilla.removeAll();
            int l = 0, o = 0, pp = 0;
            for (Mesa m : ms) {
                grilla.add(new TarjetaMesa(m));
                switch (m.estado()) {
                    case Estados.LIBRE -> l++;
                    case Estados.OCUPADA -> o++;
                    default -> pp++;
                }
            }
            nLibre.setText(String.valueOf(l));
            nOcupada.setText(String.valueOf(o));
            nPorPagar.setText(String.valueOf(pp));
            grilla.revalidate();
            grilla.repaint();
        });
    }

    private class TarjetaMesa extends RoundedPanel {
        private final Color punto;

        TarjetaMesa(Mesa m) {
            super(Theme.GREEN_BG, Theme.GREEN_LINE, 16);
            Color fg = Theme.GREEN;
            String txt = "Libre";
            Color pillBg = Theme.GREEN_BG, pillLine = Theme.GREEN_LINE;
            Color dot = new Color(0x10B981);
            if (Estados.OCUPADA.equals(m.estado())) {
                colores(Theme.ORANGE_BG, Theme.ORANGE_LINE); fg = Theme.ORANGE; txt = "Ocupada";
                pillBg = Theme.ORANGE_BG; pillLine = Theme.ORANGE_LINE; dot = Theme.ORANGE_DOT;
            } else if (Estados.POR_PAGAR.equals(m.estado())) {
                colores(Theme.RED_BG, Theme.RED_LINE); fg = Theme.RED; txt = "Por Pagar";
                pillBg = Theme.RED_BG; pillLine = Theme.RED_LINE; dot = Theme.RED_DOT;
            }
            punto = dot;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(16, 8, 14, 8));
            setPreferredSize(new Dimension(150, 134));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel e = Ui.emoji("🪑", 26f);
            e.setAlignmentX(CENTER_ALIGNMENT);
            JLabel n = Ui.etiqueta("Mesa " + m.numero(), Font.BOLD, 16f, fg);
            n.setAlignmentX(CENTER_ALIGNMENT);
            JLabel a = Ui.etiqueta(m.asientos() + " asientos", Font.PLAIN, 12f, fg);
            a.setAlignmentX(CENTER_ALIGNMENT);
            Pill p = new Pill(txt, fg, pillBg, pillLine);
            p.setAlignmentX(CENTER_ALIGNMENT);
            add(e); add(Ui.hueco(4)); add(n); add(a); add(Ui.hueco(8)); add(p);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent ev) { alElegir.accept(m); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(punto);
            g2.fillOval(getWidth() - 18, 10, 8, 8);
            g2.dispose();
        }
    }
}
