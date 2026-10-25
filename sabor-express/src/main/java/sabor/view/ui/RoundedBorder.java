package sabor.view.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.border.AbstractBorder;

public class RoundedBorder extends AbstractBorder {
    private final Color color;
    private final int arc;

    public RoundedBorder(Color color, int arc) { this.color = color; this.arc = arc; }

    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
        g2.dispose();
    }
    @Override public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
}
