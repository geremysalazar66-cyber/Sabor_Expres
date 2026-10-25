package sabor.view.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JComponent;

/** Gráfico de barras de ventas por hora (se pinta directamente, sin librerías). */
public class BarChart extends JComponent {
    private Map<Integer, Double> datos = new TreeMap<>();

    public BarChart() { setOpaque(false); }

    public void setDatos(Map<Integer, Double> d) { this.datos = d; repaint(); }

    @Override public Dimension getPreferredSize() { return new Dimension(420, 230); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        if (datos.isEmpty()) {
            g2.setFont(Theme.font(java.awt.Font.PLAIN, 13f));
            g2.setColor(Theme.MUTED);
            String t = "Sin ventas registradas hoy";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(t, (w - fm.stringWidth(t)) / 2, h / 2);
            g2.dispose();
            return;
        }
        int min = 11, max = 17;
        double top = 1;
        for (Map.Entry<Integer, Double> e : datos.entrySet()) {
            min = Math.min(min, e.getKey()); max = Math.max(max, e.getKey()); top = Math.max(top, e.getValue());
        }
        int n = max - min + 1, padB = 26, padT = 24, chartH = h - padB - padT, slot = w / n;
        int bw = Math.max(12, Math.min(56, slot - 18));
        for (int i = 0; i < n; i++) {
            int hora = min + i;
            double v = datos.getOrDefault(hora, 0.0);
            int bh = v <= 0 ? 0 : Math.max(6, (int) (chartH * v / top));
            int x = i * slot + (slot - bw) / 2, y = padT + chartH - bh;
            if (bh > 0) {
                g2.setPaint(new GradientPaint(0, y, Theme.BLUE2, 0, y + bh, Theme.BLUE));
                g2.fillRoundRect(x, y, bw, bh, 8, 8);
                g2.setFont(Theme.mono(java.awt.Font.BOLD, 11f));
                g2.setColor(Theme.BLUE);
                String s = String.valueOf(Math.round(v));
                g2.drawString(s, x + (bw - g2.getFontMetrics().stringWidth(s)) / 2, y - 6);
            }
            g2.setFont(Theme.font(java.awt.Font.PLAIN, 11f));
            g2.setColor(Theme.MUTED);
            String lab = String.format("%02d:00", hora);
            g2.drawString(lab, x + (bw - g2.getFontMetrics().stringWidth(lab)) / 2, h - 8);
        }
        g2.dispose();
    }
}
