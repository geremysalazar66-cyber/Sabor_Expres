package sabor.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import sabor.model.Usuario;
import sabor.view.ui.Emojis;
import sabor.view.ui.Pill;
import sabor.view.ui.RoundedPanel;
import sabor.view.ui.Theme;
import sabor.view.ui.Ui;

/** Barra superior común: logo, módulo actual, reloj, rol del usuario y cerrar sesión. */
public class HeaderBar extends JPanel {
    private final Timer reloj;

    public HeaderBar(String subtitulo, Usuario u, Runnable cerrarSesion) {
        setLayout(new BorderLayout());
        setBackground(Theme.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izq.setOpaque(false);
        RoundedPanel logo = new RoundedPanel(Theme.BLUE, null, 10);
        logo.setLayout(new java.awt.GridBagLayout());
        logo.setPreferredSize(new java.awt.Dimension(32, 32));
        JLabel ic = Ui.emoji("🍽", 15f);
        ic.setForeground(java.awt.Color.WHITE);
        logo.add(ic);
        izq.add(logo);
        izq.add(Ui.etiqueta("Sabor Express", Font.BOLD, 15f, Theme.TEXT));
        izq.add(Ui.etiqueta("·", Font.PLAIN, 15f, Theme.MUTED));
        izq.add(Ui.etiqueta(subtitulo, Font.PLAIN, 14f, Theme.MUTED));
        add(izq, BorderLayout.WEST);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        der.setOpaque(false);
        JLabel hora = new JLabel();
        hora.setFont(Theme.mono(Font.PLAIN, 12f));
        hora.setForeground(Theme.MUTED);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
        hora.setText(LocalTime.now().format(f));
        reloj = new Timer(1000, e -> hora.setText(LocalTime.now().format(f)));
        reloj.start();
        der.add(hora);
        der.add(new Pill(Emojis.paraRol(u.rol()), u.rol(), Theme.BLUE, Theme.BLUE_SOFT, Theme.BLUE_LINE, 12f));
        JButton salir = new JButton("Cerrar Sesión");
        salir.setFont(Theme.font(Font.PLAIN, 13f));
        salir.setForeground(Theme.MUTED);
        salir.setContentAreaFilled(false);
        salir.setBorderPainted(false);
        salir.setFocusPainted(false);
        salir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        salir.addActionListener(e -> cerrarSesion.run());
        der.add(salir);
        add(der, BorderLayout.EAST);
    }

    public void detener() { reloj.stop(); }
}
