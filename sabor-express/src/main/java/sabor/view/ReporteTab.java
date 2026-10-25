package sabor.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import sabor.controller.CajaController;
import sabor.model.Estados;
import sabor.model.PagoReg;
import sabor.model.ResumenDia;
import sabor.model.Usuario;
import sabor.util.Fmt;
import sabor.view.ui.RoundedButton;
import sabor.view.ui.RoundedPanel;
import sabor.view.ui.Theme;
import sabor.view.ui.Ui;

/** Reporte de Cierre de Caja: resumen del día, comandas pagadas y exportación a PDF. */
public class ReporteTab extends JPanel implements Refrescable {
    private final CajaController ctrl;
    private final Usuario usuario;
    private final JSpinner selFecha = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
    private final JLabel lblFecha = Ui.etiqueta(" ", Font.PLAIN, 13f, Theme.MUTED);
    private final JLabel vVentas = valor(), vCerradas = valor(), vProm = valor(), vMayor = valor(),
            vEfectivo = valor(), vTarjeta = valor(), vYape = valor();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[] {"IDENTIFICACIÓN", "MESA", "HORA", "MÉTODO", "TOTAL"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public ReporteTab(CajaController ctrl, Usuario usuario) {
        this.ctrl = ctrl;
        this.usuario = usuario;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel tit = Ui.vertical();
        tit.add(Ui.izq(Ui.etiqueta("Reporte de Cierre de Caja", Font.BOLD, 22f, Theme.TEXT)));
        tit.add(Ui.izq(lblFecha));
        top.add(tit, BorderLayout.WEST);
        JPanel acc = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        acc.setOpaque(false);
        selFecha.setEditor(new JSpinner.DateEditor(selFecha, "dd/MM/yyyy"));
        selFecha.setPreferredSize(new Dimension(120, 36));
        selFecha.addChangeListener(e -> refrescar());
        acc.add(Ui.etiqueta("Fecha:", Font.PLAIN, 13f, Theme.MUTED));
        acc.add(selFecha);
        RoundedButton pdf = new RoundedButton("📄", "Generar Informe PDF", RoundedButton.Estilo.PRIMARIO);
        pdf.addActionListener(e -> generarPdf());
        acc.add(pdf);
        top.add(acc, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // resumen
        RoundedPanel izq = new RoundedPanel(Theme.WHITE, Theme.LINE, 18);
        izq.setLayout(new BorderLayout());
        izq.setBorder(new EmptyBorder(16, 18, 16, 18));
        izq.add(Ui.etiqueta("Resumen de Ventas", Font.BOLD, 14f, Theme.TEXT), BorderLayout.NORTH);
        JPanel filas = Ui.vertical();
        filas.setBorder(new EmptyBorder(10, 0, 0, 0));
        filas.add(fila("Ventas brutas del día", vVentas, true));
        filas.add(fila("N° de comandas cerradas", vCerradas, false));
        filas.add(fila("Boleta promedio", vProm, false));
        filas.add(fila("Mayor venta individual", vMayor, false));
        filas.add(fila("Pagos en efectivo", vEfectivo, false));
        filas.add(fila("Pagos con tarjeta", vTarjeta, false));
        filas.add(fila("Pagos con Yape/Plin", vYape, false));
        JPanel env = new JPanel(new BorderLayout());
        env.setOpaque(false);
        env.add(filas, BorderLayout.NORTH);
        izq.add(env, BorderLayout.CENTER);

        // tabla
        RoundedPanel der = new RoundedPanel(Theme.WHITE, Theme.LINE, 18);
        der.setLayout(new BorderLayout(0, 8));
        der.setBorder(new EmptyBorder(16, 18, 16, 18));
        der.add(Ui.etiqueta("Registro de Comandas Pagadas", Font.BOLD, 14f, Theme.TEXT), BorderLayout.NORTH);
        JTable tabla = new JTable(modelo);
        Ui.tabla(tabla);
        der.add(Ui.scroll(tabla), BorderLayout.CENTER);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH; c.weighty = 1;
        c.gridx = 0; c.weightx = 0.38; c.insets = new java.awt.Insets(0, 0, 0, 16);
        centro.add(izq, c);
        c.gridx = 1; c.weightx = 0.62; c.insets = new java.awt.Insets(0, 0, 0, 0);
        centro.add(der, c);
        add(centro, BorderLayout.CENTER);
    }

    private static JLabel valor() {
        JLabel l = new JLabel("—");
        l.setFont(Theme.font(Font.BOLD, 13f));
        l.setForeground(Theme.TEXT);
        return l;
    }

    private JPanel fila(String texto, JLabel v, boolean destacada) {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE), new EmptyBorder(11, 2, 11, 2)));
        JLabel l = Ui.etiqueta(texto, destacada ? Font.BOLD : Font.PLAIN, 13f, destacada ? Theme.BLUE : Theme.TEXT);
        if (destacada) v.setForeground(Theme.BLUE);
        f.add(l, BorderLayout.WEST);
        f.add(v, BorderLayout.EAST);
        return Ui.ancho(f);
    }

    private LocalDate fecha() {
        return ((Date) selFecha.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override public void refrescar() {
        Ui.silencioso(() -> {
            LocalDate d = fecha();
            lblFecha.setText(Fmt.fechaLarga(d));
            ResumenDia r = ctrl.resumen(d);
            vVentas.setText(Fmt.soles(r.ventas()));
            vCerradas.setText(String.valueOf(r.pedidos()));
            vProm.setText(Fmt.soles(r.ticketPromedio()));
            vMayor.setText(Fmt.soles(r.mayor()));
            vEfectivo.setText(Fmt.soles(r.efectivo()));
            vTarjeta.setText(Fmt.soles(r.tarjeta()));
            vYape.setText(Fmt.soles(r.yape()));
            modelo.setRowCount(0);
            List<PagoReg> lista = ctrl.pagosDelDia(d);
            for (PagoReg g : lista) {
                modelo.addRow(new Object[] {g.codigo(), "Mesa " + g.numeroMesa(), Fmt.hora(g.hora()),
                        Estados.metodoTexto(g.metodo()), Fmt.soles(g.monto())});
            }
        });
    }

    private void generarPdf() {
        Ui.seguro(this, () -> {
            LocalDate d = fecha();
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Guardar informe de cierre");
            fc.setFileFilter(new FileNameExtensionFilter("Documento PDF (*.pdf)", "pdf"));
            fc.setSelectedFile(new File("cierre_caja_" + d + ".pdf"));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".pdf")) f = new File(f.getParentFile(), f.getName() + ".pdf");
            ctrl.generarPdf(d, usuario, f);
            JOptionPane.showMessageDialog(this, "Informe generado:\n" + f.getAbsolutePath(), "Informe PDF", JOptionPane.INFORMATION_MESSAGE);
            try { if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(f); } catch (Exception ignored) { }
        });
    }
}
