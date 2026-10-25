package sabor.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import sabor.controller.PedidoController;
import sabor.model.ItemPedido;
import sabor.model.Mesa;
import sabor.model.Plato;
import sabor.model.Usuario;
import sabor.util.Fmt;
import sabor.view.ui.Emojis;
import sabor.view.ui.Pill;
import sabor.view.ui.RoundedButton;
import sabor.view.ui.RoundedPanel;
import sabor.view.ui.Theme;
import sabor.view.ui.Ui;

/** Toma de pedidos: categorías, platos y resumen del pedido de la mesa. */
public class PedidoPanel extends JPanel {
    private static final String[] CATEGORIAS = {"Entradas", "Principales", "Bebidas", "Postres"};

    private final PedidoController ctrl;
    private final Usuario usuario;
    private final Runnable volver;
    private Mesa mesa;
    private List<Plato> platos = new ArrayList<>();
    private String categoria = "Principales";
    private final Map<Integer, ItemPedido> carrito = new LinkedHashMap<>();

    private final JLabel lblTitulo = Ui.etiqueta(" ", Font.BOLD, 15f, Theme.TEXT);
    private final JLabel lblResumenMesa = Ui.etiqueta(" ", Font.PLAIN, 12f, Theme.MUTED);
    private final JLabel lblTotal = Ui.etiqueta("S/ 0.00", Font.BOLD, 20f, Theme.TEXT);
    private final JPanel filaTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JPanel grilla = new JPanel(new GridLayout(0, 2, 12, 12));
    private final JPanel listaItems = Ui.vertical();

    public PedidoPanel(PedidoController ctrl, Usuario usuario, Runnable volver) {
        this.ctrl = ctrl;
        this.usuario = usuario;
        this.volver = volver;
        setLayout(new BorderLayout());
        setOpaque(false);

        // barra "← Mesas | Mesa 4 — 4 personas"
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        barra.setBackground(Theme.WHITE);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE));
        JButton atras = new JButton("←  Mesas");
        atras.setFont(Theme.font(Font.PLAIN, 13f));
        atras.setForeground(Theme.MUTED);
        atras.setContentAreaFilled(false);
        atras.setBorderPainted(false);
        atras.setFocusPainted(false);
        atras.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        atras.addActionListener(e -> volver.run());
        barra.add(atras);
        barra.add(Ui.etiqueta("|", Font.PLAIN, 14f, Theme.LINE));
        barra.add(lblTitulo);

        // izquierda: categorías + platos
        JPanel izq = new JPanel(new BorderLayout(0, 14));
        izq.setOpaque(false);
        izq.setBorder(new EmptyBorder(18, 22, 18, 12));
        filaTabs.setOpaque(false);
        izq.add(filaTabs, BorderLayout.NORTH);
        grilla.setOpaque(false);
        JPanel env = new JPanel(new BorderLayout());
        env.setOpaque(false);
        env.add(grilla, BorderLayout.NORTH);
        izq.add(Ui.scroll(env), BorderLayout.CENTER);

        // derecha: resumen
        JPanel der = new JPanel(new BorderLayout());
        der.setBackground(Theme.WHITE);
        der.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.LINE));
        der.setPreferredSize(new Dimension(350, 100));
        JPanel cab = Ui.vertical();
        cab.setBorder(new EmptyBorder(16, 18, 10, 18));
        cab.add(Ui.izq(Ui.etiqueta("Resumen del Pedido", Font.BOLD, 15f, Theme.TEXT)));
        cab.add(Ui.izq(lblResumenMesa));
        der.add(cab, BorderLayout.NORTH);
        JPanel itemsEnv = new JPanel(new BorderLayout());
        itemsEnv.setOpaque(false);
        itemsEnv.setBorder(new EmptyBorder(0, 14, 0, 14));
        itemsEnv.add(listaItems, BorderLayout.NORTH);
        der.add(Ui.scroll(itemsEnv), BorderLayout.CENTER);
        JPanel pie = Ui.vertical();
        pie.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE), new EmptyBorder(14, 18, 16, 18)));
        JPanel filaTotal = new JPanel(new BorderLayout());
        filaTotal.setOpaque(false);
        filaTotal.add(Ui.etiqueta("Total", Font.BOLD, 15f, Theme.TEXT), BorderLayout.WEST);
        filaTotal.add(lblTotal, BorderLayout.EAST);
        pie.add(Ui.ancho(filaTotal));
        pie.add(Ui.hueco(12));
        RoundedButton enviar = new RoundedButton("🔥", "Enviar a Cocina", RoundedButton.Estilo.PRIMARIO);
        enviar.setPreferredSize(new Dimension(100, 46));
        enviar.addActionListener(e -> enviar());
        pie.add(Ui.ancho(enviar));
        der.add(pie, BorderLayout.SOUTH);

        add(barra, BorderLayout.NORTH);
        add(izq, BorderLayout.CENTER);
        add(der, BorderLayout.EAST);
    }

    public void abrir(Mesa m) {
        this.mesa = m;
        carrito.clear();
        Ui.seguro(this, () -> platos = ctrl.platos());
        categoria = "Principales";
        lblTitulo.setText("Mesa " + m.numero() + " — " + m.asientos() + " personas");
        lblResumenMesa.setText("Mesa " + m.numero());
        pintarTabs();
        pintarPlatos();
        pintarResumen();
    }

    private void pintarTabs() {
        filaTabs.removeAll();
        for (String c : CATEGORIAS) {
            RoundedButton b = new RoundedButton(Emojis.paraCategoria(c), c,
                    c.equals(categoria) ? RoundedButton.Estilo.PRIMARIO : RoundedButton.Estilo.CONTORNO);
            b.arco(12);
            b.setPreferredSize(new Dimension(b.getPreferredSize().width, 36));
            b.addActionListener(e -> { categoria = c; pintarTabs(); pintarPlatos(); });
            filaTabs.add(b);
        }
        filaTabs.revalidate();
        filaTabs.repaint();
    }

    private void pintarPlatos() {
        grilla.removeAll();
        for (Plato p : platos) {
            if (!p.categoria().equalsIgnoreCase(categoria)) continue;
            grilla.add(new TarjetaPlato(p));
        }
        // relleno para que con un solo plato no ocupe todo el ancho
        if (grilla.getComponentCount() % 2 == 1) { JPanel v = new JPanel(); v.setOpaque(false); grilla.add(v); }
        grilla.revalidate();
        grilla.repaint();
    }

    private void agregar(Plato p) {
        if (!p.disponible()) return;
        carrito.computeIfAbsent(p.id(), k -> new ItemPedido(p.id(), p.nombre(), p.precio(), 0)).cantidad++;
        pintarPlatos();
        pintarResumen();
    }

    private void cambiarCantidad(ItemPedido it, int delta) {
        it.cantidad += delta;
        if (it.cantidad <= 0) carrito.remove(it.idMenu);
        pintarPlatos();
        pintarResumen();
    }

    private void pintarResumen() {
        listaItems.removeAll();
        double total = 0;
        for (ItemPedido it : carrito.values()) {
            total += it.subtotal();
            listaItems.add(filaItem(it));
            listaItems.add(Ui.hueco(8));
        }
        if (carrito.isEmpty()) {
            JLabel v = Ui.etiqueta("Toca un plato para agregarlo.", Font.PLAIN, 13f, Theme.MUTED);
            v.setBorder(new EmptyBorder(14, 6, 0, 0));
            listaItems.add(Ui.izq(v));
        }
        lblTotal.setText(Fmt.soles(total));
        listaItems.revalidate();
        listaItems.repaint();
    }

    private JPanel filaItem(ItemPedido it) {
        RoundedPanel f = new RoundedPanel(Theme.FIELD, Theme.LINE, 14).flexible();
        f.setLayout(new BorderLayout(8, 0));
        f.setBorder(new EmptyBorder(8, 10, 8, 10));
        f.add(Ui.emoji(Emojis.paraPlato(it.nombre, ""), 18f), BorderLayout.WEST);
        JPanel c = Ui.vertical();
        c.add(Ui.izq(Ui.etiqueta(it.nombre, Font.BOLD, 13f, Theme.TEXT)));
        c.add(Ui.izq(Ui.etiqueta(Fmt.soles(it.precio) + " × " + it.cantidad, Font.PLAIN, 11f, Theme.MUTED)));
        f.add(c, BorderLayout.CENTER);
        JPanel d = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        d.setOpaque(false);
        d.add(botonCantidad("−", () -> cambiarCantidad(it, -1)));
        d.add(Ui.etiqueta(String.valueOf(it.cantidad), Font.BOLD, 13f, Theme.TEXT));
        d.add(botonCantidad("+", () -> cambiarCantidad(it, +1)));
        d.add(Ui.etiqueta(Fmt.soles(it.subtotal()), Font.BOLD, 13f, Theme.TEXT));
        f.add(d, BorderLayout.EAST);
        return f;
    }

    private RoundedButton botonCantidad(String t, Runnable r) {
        RoundedButton b = new RoundedButton(t, RoundedButton.Estilo.CONTORNO).arco(20);
        b.setPreferredSize(new Dimension(24, 24));
        b.addActionListener(e -> r.run());
        return b;
    }

    private void enviar() {
        Ui.seguro(this, () -> {
            int id = ctrl.enviarACocina(mesa, usuario, new ArrayList<>(carrito.values()));
            JOptionPane.showMessageDialog(this, "Pedido " + Fmt.codigo(id) + " enviado a cocina (Mesa " + mesa.numero() + ").",
                    "Pedido enviado", JOptionPane.INFORMATION_MESSAGE);
            carrito.clear();
            volver.run();
        });
    }

    private class TarjetaPlato extends RoundedPanel {
        TarjetaPlato(Plato p) {
            super(Theme.WHITE, Theme.LINE, 16);
            ItemPedido en = carrito.get(p.id());
            if (en != null) colores(Theme.BLUE_SOFT, Theme.BLUE_LINE);
            if (!p.disponible()) colores(Theme.FIELD, Theme.LINE);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(12, 14, 12, 14));
            setPreferredSize(new Dimension(220, 96));
            setCursor(Cursor.getPredefinedCursor(p.disponible() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            add(Ui.izq(Ui.emoji(Emojis.paraPlato(p.nombre(), p.categoria()), 22f)));
            add(Ui.hueco(4));
            add(Ui.izq(Ui.etiqueta(p.nombre(), Font.BOLD, 13f, p.disponible() ? Theme.TEXT : Theme.MUTED)));
            add(Ui.hueco(2));
            JPanel pie = new JPanel(new BorderLayout());
            pie.setOpaque(false);
            pie.setAlignmentX(LEFT_ALIGNMENT);
            pie.add(Ui.etiqueta(Fmt.soles(p.precio()), Font.BOLD, 13f, p.disponible() ? Theme.BLUE : Theme.MUTED), BorderLayout.WEST);
            if (!p.disponible()) pie.add(new Pill("Agotado", Theme.RED, Theme.RED_BG, Theme.RED_LINE), BorderLayout.EAST);
            else if (en != null) pie.add(new Pill("×" + en.cantidad, Color_WHITE, Theme.BLUE, null), BorderLayout.EAST);
            add(Ui.ancho(pie));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { agregar(p); }
            });
        }
    }

    private static final java.awt.Color Color_WHITE = java.awt.Color.WHITE;
}
