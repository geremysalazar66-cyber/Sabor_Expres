package sabor.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import sabor.controller.AdminController;
import sabor.controller.CajaController;
import sabor.model.Usuario;
import sabor.service.PedidoNotifier;
import sabor.view.ui.Theme;

/** Rol Administrador: Panel, Caja / Pagos, Reporte del Día, Menú y Usuarios. */
public class AdminView extends JPanel implements Detenible {
    private final HeaderBar header;
    private final CardLayout cl = new CardLayout();
    private final JPanel cuerpo = new JPanel(cl);
    private final Refrescable[] tabs;
    private final PestanaBtn[] botones;
    private final Timer sondeo;
    private final Runnable observador;
    private int actual = 0;

    public AdminView(Usuario u, Runnable cerrarSesion) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        header = new HeaderBar("Caja y Reportes", u, cerrarSesion);
        add(header, BorderLayout.NORTH);

        AdminController admin = new AdminController();
        CajaController caja = new CajaController();
        PanelTab panel = new PanelTab(admin);
        CajaTab cajaTab = new CajaTab(caja);
        ReporteTab reporte = new ReporteTab(caja, u);
        MenuTab menu = new MenuTab(admin);
        UsuariosTab usuarios = new UsuariosTab(admin, u);
        tabs = new Refrescable[] {panel, cajaTab, reporte, menu, usuarios};
        String[] nombres = {"Panel", "Caja / Pagos", "Reporte del Día", "Menú", "Usuarios"};
        JPanel[] vistas = {panel, cajaTab, reporte, menu, usuarios};

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barra.setBackground(Theme.WHITE);
        barra.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE));
        botones = new PestanaBtn[nombres.length];
        for (int i = 0; i < nombres.length; i++) {
            final int k = i;
            botones[i] = new PestanaBtn(nombres[i]);
            botones[i].addActionListener(e -> seleccionar(k));
            barra.add(botones[i]);
            vistas[i].setBorder(new EmptyBorder(20, 28, 20, 28));
            cuerpo.add(vistas[i], "t" + i);
        }
        cuerpo.setOpaque(false);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(barra, BorderLayout.NORTH);
        centro.add(cuerpo, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        observador = this::refrescoAutomatico;
        PedidoNotifier.suscribir(observador);
        sondeo = new Timer(4000, e -> refrescoAutomatico());
        sondeo.start();
        seleccionar(0);
    }

    private void seleccionar(int i) {
        actual = i;
        for (int k = 0; k < botones.length; k++) botones[k].setSel(k == i);
        cl.show(cuerpo, "t" + i);
        tabs[i].refrescar();
    }

    /** Solo Panel y Caja se actualizan solos; las demás tienen formularios que no deben pisarse. */
    private void refrescoAutomatico() { if (actual <= 1) tabs[actual].refrescar(); }

    @Override public void detener() {
        header.detener();
        sondeo.stop();
        PedidoNotifier.cancelar(observador);
    }

    private static class PestanaBtn extends JButton {
        private boolean sel;

        PestanaBtn(String t) {
            super(t);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Theme.font(Font.BOLD, 14f));
            setPreferredSize(new Dimension(getFontMetrics(getFont()).stringWidth(t) + 40, 46));
        }
        void setSel(boolean s) { sel = s; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            g2.setColor(sel ? Theme.BLUE : Theme.MUTED);
            int x = (getWidth() - g2.getFontMetrics().stringWidth(getText())) / 2;
            g2.drawString(getText(), x, (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent());
            if (sel) { g2.setColor(Theme.BLUE); g2.fillRect(8, getHeight() - 3, getWidth() - 16, 3); }
            g2.dispose();
        }
    }
}
