package sabor.view.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.sql.SQLException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;

public final class Ui {
    private Ui() {}

    public interface Tarea { void ejecutar() throws Exception; }

    /** Ejecuta la tarea y muestra un aviso si algo falla (validaciones o error de base de datos). */
    public static void seguro(Component padre, Tarea t) {
        try { t.ejecutar(); }
        catch (IllegalArgumentException | IllegalStateException e) { aviso(padre, e.getMessage()); }
        catch (SQLException e) { JOptionPane.showMessageDialog(padre, "Error de base de datos:\n" + e.getMessage(), "Sabor Express", JOptionPane.ERROR_MESSAGE); }
        catch (Exception e) { JOptionPane.showMessageDialog(padre, e.toString(), "Sabor Express", JOptionPane.ERROR_MESSAGE); }
    }

    /** Para refrescos automáticos: no molesta al usuario con ventanas si falla. */
    public static boolean silencioso(Tarea t) {
        try { t.ejecutar(); return true; } catch (Exception e) { return false; }
    }

    public static void aviso(Component padre, String msg) {
        JOptionPane.showMessageDialog(padre, msg, "Sabor Express", JOptionPane.WARNING_MESSAGE);
    }

    public static JLabel etiqueta(String t, int estilo, float tam, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(Theme.font(estilo, tam));
        l.setForeground(c);
        return l;
    }

    public static JLabel emoji(String e, float tam) {
        JLabel l = new JLabel(e);
        l.setFont(Theme.emoji(tam));
        return l;
    }

    /** Título de sección en mayúsculas (ROL DE ACCESO, USUARIO...). */
    public static JLabel seccion(String t) { return etiqueta(t, Font.BOLD, 11f, Theme.MUTED); }

    public static JPanel vertical() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    /** Columna cuyo contenido cambia: ocupa todo el ancho y crece en alto según lo que tenga. */
    public static JPanel verticalFlexible() {
        JPanel p = new JPanel() {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    public static JPanel fila(int hgap) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, hgap, 0));
        p.setOpaque(false);
        return p;
    }

    public static <T extends JComponent> T izq(T c) { c.setAlignmentX(Component.LEFT_ALIGNMENT); return c; }

    /** Hace que el componente ocupe todo el ancho en un BoxLayout vertical. */
    public static <T extends JComponent> T ancho(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    public static Component hueco(int h) { return Box.createVerticalStrut(h); }

    public static void campo(JTextComponent f) {
        f.setFont(Theme.font(Font.PLAIN, 14f));
        f.setForeground(Theme.TEXT);
        f.setBackground(Theme.FIELD);
        f.setCaretColor(Theme.TEXT);
        f.setBorder(new CompoundBorder(new RoundedBorder(Theme.LINE, 12), new EmptyBorder(9, 12, 9, 12)));
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        return sp;
    }

    public static void tabla(JTable t) {
        t.setRowHeight(34);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFont(Theme.font(Font.PLAIN, 13f));
        t.setForeground(Theme.TEXT);
        t.setSelectionBackground(Theme.BLUE_SOFT);
        t.setSelectionForeground(Theme.TEXT);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer head = new DefaultTableCellRenderer();
        head.setBackground(Theme.FIELD);
        head.setForeground(Theme.MUTED);
        head.setFont(Theme.font(Font.BOLD, 11f));
        head.setBorder(new EmptyBorder(8, 10, 8, 10));
        t.getTableHeader().setDefaultRenderer(head);
        DefaultTableCellRenderer cell = new DefaultTableCellRenderer();
        cell.setBorder(new EmptyBorder(0, 10, 0, 10));
        t.setDefaultRenderer(Object.class, cell);
    }
}
