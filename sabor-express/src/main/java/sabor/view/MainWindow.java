package sabor.view;

import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import sabor.db.DatabaseInit;
import sabor.model.Usuario;
import sabor.view.ui.Theme;

public class MainWindow extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel raiz = new JPanel(cards);
    private final LoginView login;
    private JPanel actual;

    public static void arrancar() {
        try {
            DatabaseInit.iniciar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar con PostgreSQL:\n" + e.getMessage()
                    + "\n\nRevisa db.properties (url, usuario y contraseña) y que la base bd_saborexpress exista.",
                    "Sabor Express", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        new MainWindow().setVisible(true);
    }

    private MainWindow() {
        super("Sabor Express · Sistema de Gestión de Restaurante");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setSize(1200, 800);
        setLocationRelativeTo(null);
        raiz.setBackground(Theme.BG);
        login = new LoginView(this::abrirPara);
        raiz.add(login, "login");
        setContentPane(raiz);
        cards.show(raiz, "login");
    }

    private void abrirPara(Usuario u) {
        JPanel vista;
        String rol = u.rol().toLowerCase();
        if (rol.startsWith("moz")) vista = new MozoView(u, this::cerrarSesion);
        else if (rol.startsWith("coc")) vista = new CocinaView(u, this::cerrarSesion);
        else vista = new AdminView(u, this::cerrarSesion);
        actual = vista;
        raiz.add(vista, "app");
        cards.show(raiz, "app");
    }

    private void cerrarSesion() {
        if (actual instanceof Detenible d) d.detener();
        if (actual != null) raiz.remove(actual);
        actual = null;
        login.reiniciar();
        cards.show(raiz, "login");
        raiz.revalidate();
        raiz.repaint();
    }
}
