package sabor.view.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/** Etiqueta con forma de píldora (estados, contadores, rol del usuario). */
public class Pill extends JComponent {
    private String texto, emoji;
    private Color fg, bg, borde;
    private final float size;

    public Pill(String emoji, String texto, Color fg, Color bg, Color borde, float size) {
        this.emoji = emoji; this.texto = texto; this.fg = fg; this.bg = bg; this.borde = borde; this.size = size;
        setOpaque(false);
    }
    public Pill(String texto, Color fg, Color bg, Color borde) { this("", texto, fg, bg, borde, 11f); }

    public void setTexto(String t) { texto = t; revalidate(); repaint(); }

    @Override public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(Theme.font(java.awt.Font.BOLD, size));
        int w = fm.stringWidth(texto) + 20;
        if (!emoji.isEmpty()) w += getFontMetrics(Theme.emoji(size + 1)).stringWidth(emoji) + 6;
        return new Dimension(w, fm.getHeight() + 8);
    }
    @Override public Dimension getMaximumSize() { return getPreferredSize(); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        if (bg != null) { g2.setColor(bg); g2.fillRoundRect(0, 0, w - 1, h - 1, h, h); }
        if (borde != null) { g2.setColor(borde); g2.drawRoundRect(0, 0, w - 1, h - 1, h, h); }
        g2.setColor(fg);
        java.awt.Font f = Theme.font(java.awt.Font.BOLD, size), ef = Theme.emoji(size + 1);
        FontMetrics fm = g2.getFontMetrics(f);
        int ew = emoji.isEmpty() ? 0 : g2.getFontMetrics(ef).stringWidth(emoji) + 6;
        int x = (w - fm.stringWidth(texto) - ew) / 2;
        if (!emoji.isEmpty()) {
            g2.setFont(ef);
            g2.drawString(emoji, x, (h - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent());
            x += ew;
        }
        g2.setFont(f);
        g2.drawString(texto, x, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
