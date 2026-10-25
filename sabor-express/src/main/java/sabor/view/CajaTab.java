package sabor.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import sabor.controller.CajaController;
import sabor.model.Estados;
import sabor.model.ItemPedido;
import sabor.model.PagoReg;
import sabor.model.Pedido;
import sabor.service.CajaFacade;
import sabor.util.Fmt;
import sabor.view.ui.Pill;
import sabor.view.ui.RoundedButton;
import sabor.view.ui.RoundedPanel;
import sabor.view.ui.SelectableCard;
import sabor.view.ui.Theme;
import sabor.view.ui.Ui;

/** Caja / Pagos: órdenes listas para cobro, método de pago, vuelto y boleta. */
public class CajaTab extends JPanel implements Refrescable {
    private static final String[] METODOS = {Estados.EFECTIVO, Estados.TARJETA, Estados.YAPE_PLIN};
    private static final String[] ICONOS = {"💵", "💳", "📱"};
    private static final String[] NOMBRES = {"Efectivo", "Tarjeta", "Yape/Plin"};

    private final CajaController ctrl;
    private Pedido sel;
    private String metodo = Estados.EFECTIVO;
    private String firma = "", firmaRec = "";

    private final JPanel listaOrdenes = Ui.verticalFlexible();
    private final JPanel listaRecientes = Ui.verticalFlexible();
    private final CardLayout cartas = new CardLayout();
    private final JPanel detalle = new JPanel(cartas);
    private final JLabel lblSub = Ui.etiqueta(" ", Font.PLAIN, 12f, Theme.MUTED);
    private final JPanel itemsBox = Ui.verticalFlexible();
    private final JLabel lblTotal = Ui.etiqueta("S/ 0.00", Font.BOLD, 22f, Theme.BLUE);
    private final SelectableCard[] tarjetasMetodo = new SelectableCard[3];
    private final JTextField txtRecibido = new JTextField();
    private final JLabel lblVuelto = Ui.etiqueta(" ", Font.PLAIN, 12f, Theme.MUTED);
    private final RoundedButton btnPagar = new RoundedButton("Registrar Pago", RoundedButton.Estilo.PRIMARIO);

    public CajaTab(CajaController ctrl) {
        this.ctrl = ctrl;
        setOpaque(false);
        setLayout(new GridBagLayout());

        // ----- izquierda -----
        JPanel izq = Ui.vertical();
        izq.add(Ui.izq(Ui.seccion("ÓRDENES LISTAS PARA COBRO")));
        izq.add(Ui.hueco(10));
        izq.add(listaOrdenes);
        izq.add(Ui.hueco(18));
        izq.add(Ui.izq(Ui.seccion("PAGOS RECIENTES")));
        izq.add(Ui.hueco(10));
        izq.add(listaRecientes);
        JPanel izqEnv = new JPanel(new BorderLayout());
        izqEnv.setOpaque(false);
        izqEnv.add(izq, BorderLayout.NORTH);
        JScrollPane sp = Ui.scroll(izqEnv);

        // ----- derecha -----
        RoundedPanel card = new RoundedPanel(Theme.WHITE, Theme.LINE, 20).barraSuperior(Theme.BLUE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        detalle.setOpaque(false);
        JLabel vacio = Ui.etiqueta("Selecciona una orden para cobrar", Font.PLAIN, 14f, Theme.MUTED);
        vacio.setHorizontalAlignment(JLabel.CENTER);
        detalle.add(vacio, "vacio");
        detalle.add(construirDetalle(), "orden");
        card.add(detalle, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridx = 0; c.weightx = 1; c.insets = new java.awt.Insets(0, 0, 0, 20);
        add(sp, c);
        c.gridx = 1; c.weightx = 0; c.insets = new java.awt.Insets(0, 0, 0, 0);
        card.setPreferredSize(new Dimension(400, 100));
        add(card, c);
        cartas.show(detalle, "vacio");
    }

    private JPanel construirDetalle() {
        JPanel p = Ui.vertical();
        p.add(Ui.izq(Ui.etiqueta("Orden de Cobrar", Font.BOLD, 16f, Theme.TEXT)));
        p.add(Ui.izq(lblSub));
        p.add(Ui.hueco(12));
        p.add(itemsBox);
        p.add(Ui.hueco(8));
        JPanel tot = new JPanel(new BorderLayout());
        tot.setOpaque(false);
        tot.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE), new EmptyBorder(10, 0, 0, 0)));
        tot.add(Ui.etiqueta("Total a Cobrar", Font.BOLD, 14f, Theme.TEXT), BorderLayout.WEST);
        tot.add(lblTotal, BorderLayout.EAST);
        p.add(Ui.ancho(tot));
        p.add(Ui.hueco(16));
        p.add(Ui.izq(Ui.seccion("MÉTODO DE PAGO")));
        p.add(Ui.hueco(8));
        JPanel fila = new JPanel(new GridLayout(1, 3, 8, 0));
        fila.setOpaque(false);
        for (int i = 0; i < 3; i++) {
            final int k = i;
            SelectableCard m = new SelectableCard(() -> elegirMetodo(k));
            m.setLayout(new BoxLayout(m, BoxLayout.Y_AXIS));
            m.setBorder(new EmptyBorder(10, 4, 8, 4));
            JLabel e = Ui.emoji(ICONOS[i], 18f);
            e.setAlignmentX(CENTER_ALIGNMENT);
            JLabel n = Ui.etiqueta(NOMBRES[i], Font.BOLD, 12f, Theme.TEXT);
            n.setAlignmentX(CENTER_ALIGNMENT);
            m.add(e); m.add(n);
            tarjetasMetodo[i] = m;
            fila.add(m);
        }
        fila.setPreferredSize(new Dimension(100, 62));
        p.add(Ui.ancho(fila));
        p.add(Ui.hueco(14));
        p.add(Ui.izq(Ui.seccion("EFECTIVO RECIBIDO")));
        p.add(Ui.hueco(6));
        Ui.campo(txtRecibido);
        txtRecibido.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { actualizarBoton(); }
            public void removeUpdate(DocumentEvent e) { actualizarBoton(); }
            public void changedUpdate(DocumentEvent e) { actualizarBoton(); }
        });
        p.add(Ui.ancho(txtRecibido));
        p.add(Ui.hueco(4));
        p.add(Ui.izq(lblVuelto));
        p.add(Ui.hueco(14));
        btnPagar.setPreferredSize(new Dimension(100, 46));
        btnPagar.addActionListener(e -> registrar());
        p.add(Ui.ancho(btnPagar));
        elegirMetodo(0);
        return p;
    }

    private void elegirMetodo(int i) {
        metodo = METODOS[i];
        for (int k = 0; k < 3; k++) tarjetasMetodo[k].setSeleccionada(k == i);
        txtRecibido.setEnabled(Estados.EFECTIVO.equals(metodo));
        if (!txtRecibido.isEnabled()) txtRecibido.setText("");
        actualizarBoton();
    }

    private double recibido() {
        try { return Double.parseDouble(txtRecibido.getText().trim().replace(',', '.')); }
        catch (NumberFormatException e) { return 0; }
    }

    private void actualizarBoton() {
        if (sel == null) return;
        btnPagar.setText("Registrar Pago · " + Fmt.soles(sel.total));
        boolean ok = true;
        lblVuelto.setText(" ");
        lblVuelto.setForeground(Theme.MUTED);
        if (Estados.EFECTIVO.equals(metodo)) {
            double r = recibido();
            ok = r + 0.001 >= sel.total;
            if (r > 0 && ok) lblVuelto.setText("Vuelto: " + Fmt.soles(r - sel.total));
            else if (r > 0) { lblVuelto.setText("Dinero insuficiente"); lblVuelto.setForeground(Theme.RED); }
        }
        btnPagar.setEnabled(ok);
        btnPagar.repaint();
    }

    private void pintarDetalle() {
        if (sel == null) { cartas.show(detalle, "vacio"); return; }
        lblSub.setText(sel.codigo() + " · Mesa " + sel.numeroMesa);
        itemsBox.removeAll();
        for (ItemPedido i : sel.items) {
            JPanel f = new JPanel(new BorderLayout());
            f.setOpaque(false);
            f.setBorder(new EmptyBorder(3, 0, 3, 0));
            f.add(Ui.etiqueta(i.nombre + " × " + i.cantidad, Font.PLAIN, 13f, Theme.TEXT), BorderLayout.WEST);
            f.add(Ui.etiqueta(Fmt.soles(i.subtotal()), Font.PLAIN, 13f, Theme.TEXT), BorderLayout.EAST);
            itemsBox.add(Ui.ancho(f));
        }
        lblTotal.setText(Fmt.soles(sel.total));
        itemsBox.revalidate();
        cartas.show(detalle, "orden");
        elegirMetodo(0);
        txtRecibido.setText("");
        actualizarBoton();
    }

    private RoundedPanel tarjetaOrden(Pedido p) {
        boolean s = sel != null && sel.id == p.id;
        RoundedPanel c = new RoundedPanel(s ? Theme.BLUE_SOFT : Theme.WHITE, s ? Theme.BLUE : Theme.LINE, 16).flexible();
        c.setLayout(new BorderLayout());
        c.setBorder(new EmptyBorder(12, 16, 12, 16));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel izq = Ui.vertical();
        izq.add(Ui.izq(Ui.etiqueta(p.codigo(), Font.BOLD, 11f, Theme.MUTED)));
        izq.add(Ui.izq(Ui.etiqueta("Mesa " + p.numeroMesa, Font.BOLD, 16f, Theme.TEXT)));
        int n = p.cantidadArticulos();
        izq.add(Ui.izq(Ui.etiqueta(n + (n == 1 ? " artículo" : " artículos") + " · " + Fmt.hace(p.fecha).toLowerCase(), Font.PLAIN, 12f, Theme.MUTED)));
        c.add(izq, BorderLayout.WEST);
        c.add(Ui.etiqueta(Fmt.soles(p.total), Font.BOLD, 17f, Theme.TEXT), BorderLayout.EAST);
        c.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { sel = p; firma = ""; pintarListas(ordenesActuales); pintarDetalle(); }
        });
        return c;
    }

    private List<Pedido> ordenesActuales = new ArrayList<>();

    private void pintarListas(List<Pedido> ordenes) {
        listaOrdenes.removeAll();
        if (ordenes.isEmpty()) {
            listaOrdenes.add(Ui.izq(Ui.etiqueta("No hay órdenes listas para cobro.", Font.PLAIN, 13f, Theme.MUTED)));
        }
        for (Pedido p : ordenes) {
            listaOrdenes.add(tarjetaOrden(p));
            listaOrdenes.add(Ui.hueco(10));
        }
        listaOrdenes.revalidate();
        listaOrdenes.repaint();
    }

    private void pintarRecientes(List<PagoReg> pagos) {
        listaRecientes.removeAll();
        if (pagos.isEmpty()) listaRecientes.add(Ui.izq(Ui.etiqueta("Sin pagos registrados.", Font.PLAIN, 13f, Theme.MUTED)));
        for (PagoReg g : pagos) {
            RoundedPanel f = new RoundedPanel(Theme.WHITE, Theme.LINE, 12).flexible();
            f.setLayout(new BorderLayout(10, 0));
            f.setBorder(new EmptyBorder(8, 14, 8, 14));
            f.add(Ui.etiqueta(g.codigo() + " · Mesa " + g.numeroMesa() + " · " + Fmt.hora(g.hora()), Font.PLAIN, 12f, Theme.MUTED), BorderLayout.WEST);
            JPanel der = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
            der.setOpaque(false);
            der.add(new Pill(Estados.metodoTexto(g.metodo()), Theme.BLUE, Theme.BLUE_SOFT, Theme.BLUE_LINE));
            der.add(Ui.etiqueta(Fmt.soles(g.monto()), Font.BOLD, 13f, Theme.TEXT));
            f.add(der, BorderLayout.EAST);
            listaRecientes.add(f);
            listaRecientes.add(Ui.hueco(6));
        }
        listaRecientes.revalidate();
        listaRecientes.repaint();
    }

    @Override public void refrescar() {
        Ui.silencioso(() -> {
            List<Pedido> ordenes = ctrl.listosParaCobro();
            ordenesActuales = ordenes;
            if (sel != null) {
                Pedido nuevo = ordenes.stream().filter(o -> o.id == sel.id).findFirst().orElse(null);
                if (nuevo == null) { sel = null; pintarDetalle(); firma = ""; }
            }
            StringBuilder sb = new StringBuilder();
            for (Pedido p : ordenes) sb.append(p.id).append(Fmt.hace(p.fecha)).append(';');
            sb.append(sel == null ? -1 : sel.id);
            if (!sb.toString().equals(firma)) { firma = sb.toString(); pintarListas(ordenes); }

            List<PagoReg> rec = ctrl.pagosRecientes();
            StringBuilder s2 = new StringBuilder();
            for (PagoReg g : rec) s2.append(g.idPedido()).append(';');
            if (!s2.toString().equals(firmaRec)) { firmaRec = s2.toString(); pintarRecientes(rec); }
        });
    }

    private void registrar() {
        Ui.seguro(this, () -> {
            if (sel == null) return;
            CajaFacade.Boleta b = ctrl.cobrar(sel, metodo, recibido());
            JTextArea area = new JTextArea(b.texto());
            area.setFont(Theme.mono(Font.PLAIN, 12f));
            area.setEditable(false);
            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(360, 380));
            JOptionPane.showMessageDialog(this, sp, "Boleta de venta", JOptionPane.INFORMATION_MESSAGE);
            sel = null;
            firma = "";
            pintarDetalle();
            refrescar();
        });
    }
}
