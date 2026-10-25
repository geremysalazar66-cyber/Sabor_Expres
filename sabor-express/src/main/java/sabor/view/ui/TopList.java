package sabor.view.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import sabor.model.TopPlato;

/** Lista "Más Pedidos" con barra de progreso por plato. */
public class TopList extends JComponent {
    private List<TopPlato> datos = new ArrayList<>();

    public TopList() { setOpaque(false); }
    public void setDatos(List<TopPlato> d) { datos = d; repaint(); }

    @Override public Dimension getPreferredSize() { return new Dimension(260, 230); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        if (datos.isEmpty()) {
            g2.setFont(Theme.font(java.awt.Font.PLAIN, 13f));
            g2.setColor(Theme.MUTED);
            g2.drawString("Aún no hay pedidos hoy", 4, 30);
            g2.dispose();
            return;
        }
        int max = datos.stream().mapToInt(TopPlato::cantidad).max().orElse(1);
        int rowH = 44;
        for (int i = 0; i < datos.size(); i++) {
            TopPlato t = datos.get(i);
            int y = i * rowH;
            g2.setFont(Theme.font(java.awt.Font.PLAIN, 12f));
            g2.setColor(Theme.MUTED);
            g2.drawString(String.valueOf(i + 1), 2, y + 16);
            g2.setFont(Theme.emoji(16f));
            g2.setColor(Theme.TEXT);
            g2.drawString(Emojis.paraPlato(t.nombre(), ""), 22, y + 18);
            g2.setFont(Theme.font(java.awt.Font.PLAIN, 13f));
            g2.drawString(t.nombre(), 50, y + 16);
            g2.setFont(Theme.font(java.awt.Font.BOLD, 13f));
            String c = String.valueOf(t.cantidad());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(c, w - fm.stringWidth(c) - 2, y + 16);
            int bx = 50, bw = w - 50 - 4;
            g2.setColor(Theme.LINE);
            g2.fillRoundRect(bx, y + 24, bw, 5, 5, 5);
            g2.setColor(Theme.BLUE);
            g2.fillRoundRect(bx, y + 24, Math.max(6, bw * t.cantidad() / max), 5, 5, 5);
        }
        g2.dispose();
    }
}
