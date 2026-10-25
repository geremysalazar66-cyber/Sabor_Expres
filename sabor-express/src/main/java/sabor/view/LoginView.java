package sabor.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import sabor.controller.AuthController;
import sabor.model.Usuario;
import sabor.view.ui.RoundedButton;
import sabor.view.ui.RoundedPanel;
import sabor.view.ui.SelectableCard;
import sabor.view.ui.Theme;
import sabor.view.ui.Ui;

public class LoginView extends JPanel {
    private static final String[] ROLES = {"Mozo", "Cocinero", "Administrador"};
    private static final String[] ICONOS = {"🍽", "🍳", "💼"};
    private static final String[] SUBS = {"Toma de pedidos", "Monitor de cocina", "Caja y reportes"};

    private final AuthController auth = new AuthController();
    private final Consumer<Usuario> alIngresar;
    private final SelectableCard[] tarjetas = new SelectableCard[3];
    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();
    private final JLabel lblError = Ui.etiqueta(" ", Font.PLAIN, 12f, Theme.RED);
    private String rol = ROLES[0];

    public LoginView(Consumer<Usuario> alIngresar) {
        this.alIngresar = alIngresar;
        setBackground(Theme.BG);
        setLayout(new GridBagLayout());

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        // logo + título
        RoundedPanel logo = new RoundedPanel(Theme.BLUE, null, 22);
        logo.setLayout(new GridBagLayout());
        logo.setPreferredSize(new Dimension(60, 60));
        logo.setMaximumSize(new Dimension(60, 60));
        JLabel ic = Ui.emoji("🍽", 26f);
        ic.setForeground(java.awt.Color.WHITE);
        logo.add(ic);
        logo.setAlignmentX(CENTER_ALIGNMENT);
        col.add(logo);
        col.add(Ui.hueco(10));
        JLabel t = Ui.etiqueta("Sabor Express", Font.BOLD, 30f, Theme.TEXT);
        t.setAlignmentX(CENTER_ALIGNMENT);
        col.add(t);
        JLabel st = Ui.etiqueta("Sistema de Gestión de Restaurante", Font.PLAIN, 13f, Theme.MUTED);
        st.setAlignmentX(CENTER_ALIGNMENT);
        col.add(st);
        col.add(Ui.hueco(22));

        // tarjeta
        RoundedPanel card = new RoundedPanel(Theme.WHITE, Theme.LINE, 24).barraSuperior(Theme.BLUE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 28, 22, 28));
        card.setPreferredSize(new Dimension(440, 480));
        card.setMaximumSize(new Dimension(440, 480));
        card.setAlignmentX(CENTER_ALIGNMENT);
        JPanel in = Ui.vertical();
        card.add(in, BorderLayout.CENTER);

        in.add(Ui.izq(Ui.etiqueta("Iniciar Sesión", Font.BOLD, 18f, Theme.TEXT)));
        in.add(Ui.hueco(18));
        in.add(Ui.izq(Ui.seccion("ROL DE ACCESO")));
        in.add(Ui.hueco(8));
        JPanel filaRoles = new JPanel(new GridLayout(1, 3, 10, 0));
        filaRoles.setOpaque(false);
        for (int i = 0; i < 3; i++) {
            final int k = i;
            SelectableCard c = new SelectableCard(() -> elegirRol(k));
            c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
            c.setBorder(new EmptyBorder(12, 6, 10, 6));
            JLabel e = Ui.emoji(ICONOS[i], 22f);
            e.setAlignmentX(CENTER_ALIGNMENT);
            JLabel n = Ui.etiqueta(ROLES[i], Font.BOLD, 13f, Theme.TEXT);
            n.setAlignmentX(CENTER_ALIGNMENT);
            JLabel s = Ui.etiqueta(SUBS[i], Font.PLAIN, 10f, Theme.MUTED);
            s.setAlignmentX(CENTER_ALIGNMENT);
            c.add(e); c.add(Ui.hueco(4)); c.add(n); c.add(s);
            tarjetas[i] = c;
            filaRoles.add(c);
        }
        filaRoles.setPreferredSize(new Dimension(100, 82));
        in.add(Ui.ancho(filaRoles));
        in.add(Ui.hueco(16));

        in.add(Ui.izq(Ui.seccion("USUARIO")));
        in.add(Ui.hueco(6));
        Ui.campo(txtUsuario);
        in.add(Ui.ancho(txtUsuario));
        in.add(Ui.hueco(14));
        in.add(Ui.izq(Ui.seccion("CONTRASEÑA")));
        in.add(Ui.hueco(6));
        Ui.campo(txtClave);
        in.add(Ui.ancho(txtClave));
        in.add(Ui.hueco(8));
        in.add(Ui.izq(lblError));
        in.add(Ui.hueco(8));

        RoundedButton btn = new RoundedButton("Ingresar al Sistema", RoundedButton.Estilo.PRIMARIO);
        btn.setPreferredSize(new Dimension(100, 44));
        in.add(Ui.ancho(btn));
        in.add(Ui.hueco(14));
        JLabel demo = Ui.etiqueta("Demo: mozo/mozo123 · cocinero/cocina123 · admin/admin123", Font.PLAIN, 11f, Theme.MUTED);
        demo.setHorizontalAlignment(SwingConstants.CENTER);
        demo.setAlignmentX(LEFT_ALIGNMENT);
        in.add(demo);

        col.add(card);
        col.add(Ui.hueco(14));
        JLabel ver = Ui.etiqueta("v1.0 · Sabor Express © 2026", Font.PLAIN, 11f, Theme.MUTED);
        ver.setAlignmentX(CENTER_ALIGNMENT);
        col.add(ver);
        add(col);

        btn.addActionListener(e -> ingresar());
        txtClave.addActionListener(e -> ingresar());
        txtUsuario.addActionListener(e -> txtClave.requestFocusInWindow());
        elegirRol(0);
    }

    private void elegirRol(int i) {
        rol = ROLES[i];
        for (int k = 0; k < 3; k++) tarjetas[k].setSeleccionada(k == i);
    }

    private void ingresar() {
        try {
            Usuario u = auth.login(txtUsuario.getText(), new String(txtClave.getPassword()), rol);
            lblError.setText(" ");
            alIngresar.accept(u);
        } catch (IllegalArgumentException e) {
            lblError.setText(e.getMessage());
        } catch (SQLException e) {
            lblError.setText("No se pudo consultar PostgreSQL. Revisa db.properties.");
        }
    }

    public void reiniciar() {
        txtUsuario.setText("");
        txtClave.setText("");
        lblError.setText(" ");
        elegirRol(0);
        txtUsuario.requestFocusInWindow();
    }
}
