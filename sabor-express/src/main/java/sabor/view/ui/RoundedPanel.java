package sabor.view.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;

public class RoundedPanel extends JPanel {
    private Color fill, line, topBar;
    private int arc;
    private boolean flex;

    public RoundedPanel(Color fill, Color line, int arc) {
        this.fill = fill; this.line = line; this.arc = arc;
        setOpaque(false);
        setAlignmentX(LEFT_ALIGNMENT);
    }
    public RoundedPanel() { this(Theme.WHITE, Theme.LINE, 20); }

    public RoundedPanel colores(Color fill, Color line) { this.fill = fill; this.line = line; repaint(); return this; }
    public RoundedPanel barraSuperior(Color c) { this.topBar = c; return this; }
    /** Dentro de un BoxLayout vertical, ocupa todo el ancho disponible. */
    public RoundedPanel flexible() { this.flex = true; return this; }

    @Override public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        return flex ? new Dimension(Integer.MAX_VALUE, getPreferredSize().height) : d;
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RoundRectangle2D.Float rr = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, arc, arc);
        if (fill != null) { g2.setColor(fill); g2.fill(rr); }
        if (topBar != null) {
            g2.setClip(rr);
            g2.setColor(topBar);
            g2.fillRect(0, 0, getWidth(), 4);
            g2.setClip(null);
        }
        if (line != null) { g2.setColor(line); g2.draw(rr); }
        g2.dispose();
        super.paintComponent(g);
    }
}
