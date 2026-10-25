package sabor.view.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;

/** Botón redondeado pintado a mano (no depende del Look & Feel). Admite un emoji al inicio. */
public class RoundedButton extends JButton {
    public enum Estilo { PRIMARIO, EXITO, CONTORNO, SUAVE }

    private final Estilo estilo;
    private final String emoji;
    private int arc = 14;
    private boolean hover;

    public RoundedButton(String texto, Estilo estilo) { this("", texto, estilo); }

    public RoundedButton(String emoji, String texto, Estilo estilo) {
        super(texto);
        this.emoji = emoji;
        this.estilo = estilo;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setFont(Theme.font(java.awt.Font.BOLD, 13f));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }

    public RoundedButton arco(int a) { this.arc = a; return this; }

    @Override public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) return super.getPreferredSize();
        FontMetrics fm = getFontMetrics(getFont());
        int w = fm.stringWidth(getText()) + 36;
        if (!emoji.isEmpty()) w += getFontMetrics(Theme.emoji(14f)).stringWidth(emoji) + 8;
        return new Dimension(w, 38);
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        RoundRectangle2D.Float rr = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, arc, arc);
        Color fg;
        Color borde = null;
        if (!isEnabled()) {
            g2.setColor(Theme.LINE); g2.fill(rr); fg = new Color(0x94A3B8);
        } else {
            switch (estilo) {
                case PRIMARIO -> {
                    Color a = hover ? Theme.BLUE.darker() : Theme.BLUE, b = hover ? Theme.BLUE2.darker() : Theme.BLUE2;
                    g2.setPaint(new GradientPaint(0, 0, a, w, 0, b)); g2.fill(rr); fg = Color.WHITE;
                }
                case EXITO -> {
                    g2.setColor(hover ? Theme.GREEN_BTN.darker() : Theme.GREEN_BTN); g2.fill(rr); fg = Color.WHITE;
                }
                case SUAVE -> {
                    g2.setColor(hover ? new Color(0xDBEAFE) : Theme.BLUE_SOFT); g2.fill(rr);
                    borde = Theme.BLUE_LINE; fg = Theme.BLUE;
                }
                default -> {
                    g2.setColor(hover ? Theme.FIELD : Color.WHITE); g2.fill(rr);
                    borde = Theme.LINE; fg = Theme.TEXT;
                }
            }
        }
        if (borde != null) { g2.setColor(borde); g2.draw(rr); }

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(getText());
        java.awt.Font ef = Theme.emoji(14f);
        int ew = emoji.isEmpty() ? 0 : g2.getFontMetrics(ef).stringWidth(emoji) + 8;
        int x = (w - tw - ew) / 2;
        g2.setColor(fg);
        if (!emoji.isEmpty()) {
            g2.setFont(ef);
            g2.drawString(emoji, x, (h - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent());
            g2.setFont(getFont());
            x += ew;
        }
        g2.drawString(getText(), x, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
