package sabor.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import sabor.controller.AdminController;
import sabor.model.ResumenDia;
import sabor.util.Fmt;
import sabor.view.ui.BarChart;
import sabor.view.ui.RoundedPanel;
import sabor.view.ui.Theme;
import sabor.view.ui.TopList;
import sabor.view.ui.Ui;

/** Panel: indicadores del día, ventas por hora y platos más pedidos. */
public class PanelTab extends JPanel implements Refrescable {
    private final AdminController ctrl;
    private final JLabel vVentas = new JLabel(), vPedidos = new JLabel(), vTicket = new JLabel(), vMesas = new JLabel();
    private final BarChart grafico = new BarChart();
    private final TopList top = new TopList();

    public PanelTab(AdminController ctrl) {
        this.ctrl = ctrl;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        JPanel kpis = new JPanel(new GridLayout(1, 4, 16, 0));
        kpis.setOpaque(false);
        kpis.add(kpi("💰", vVentas, "Ventas del Día", Theme.BLUE_SOFT, Theme.BLUE_LINE, Theme.BLUE));
        kpis.add(kpi("📋", vPedidos, "Pedidos Atendidos", Theme.GREEN_BG, Theme.GREEN_LINE, Theme.GREEN));
        kpis.add(kpi("🎫", vTicket, "Ticket Promedio", Theme.PURPLE_BG, Theme.PURPLE_LINE, Theme.PURPLE));
        kpis.add(kpi("🪑", vMesas, "Mesas Activas", Theme.ORANGE_BG, Theme.ORANGE_LINE, Theme.ORANGE));
        add(kpis, BorderLayout.NORTH);

        JPanel abajo = new JPanel(new GridBagLayout());
        abajo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridy = 0;
        c.gridx = 0; c.weightx = 0.64; c.insets = new java.awt.Insets(0, 0, 0, 16);
        abajo.add(tarjeta("Ventas por Hora (S/)", grafico), c);
        c.gridx = 1; c.weightx = 0.36; c.insets = new java.awt.Insets(0, 0, 0, 0);
        abajo.add(tarjeta("Más Pedidos", top), c);
        add(abajo, BorderLayout.CENTER);
    }

    private RoundedPanel kpi(String emoji, JLabel valor, String texto, Color fondo, Color borde, Color fg) {
        RoundedPanel p = new RoundedPanel(fondo, borde, 18);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(14, 18, 16, 18));
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(Ui.emoji(emoji, 22f), BorderLayout.WEST);
        fila.add(Ui.etiqueta("HOY", Font.BOLD, 11f, Theme.MUTED), BorderLayout.EAST);
        p.add(Ui.ancho(fila));
        p.add(Ui.hueco(8));
        valor.setFont(Theme.font(Font.BOLD, 28f));
        valor.setForeground(fg);
        valor.setText("—");
        p.add(Ui.izq(valor));
        p.add(Ui.izq(Ui.etiqueta(texto, Font.PLAIN, 13f, Theme.MUTED)));
        return p;
    }

    private RoundedPanel tarjeta(String titulo, javax.swing.JComponent contenido) {
        RoundedPanel p = new RoundedPanel(Theme.WHITE, Theme.LINE, 18);
        p.setLayout(new BorderLayout(0, 10));
        p.setBorder(new EmptyBorder(16, 18, 16, 18));
        p.add(Ui.etiqueta(titulo, Font.BOLD, 14f, Theme.TEXT), BorderLayout.NORTH);
        p.add(contenido, BorderLayout.CENTER);
        return p;
    }

    @Override public void refrescar() {
        Ui.silencioso(() -> {
            LocalDate hoy = LocalDate.now();
            ResumenDia r = ctrl.resumen(hoy);
            vVentas.setText(Fmt.soles(r.ventas()));
            vPedidos.setText(String.valueOf(r.pedidos()));
            vTicket.setText(Fmt.soles(r.ticketPromedio()));
            vMesas.setText(String.valueOf(ctrl.mesasActivas()));
            grafico.setDatos(ctrl.ventasPorHora(hoy));
            top.setDatos(ctrl.masPedidos(hoy));
        });
    }
}
