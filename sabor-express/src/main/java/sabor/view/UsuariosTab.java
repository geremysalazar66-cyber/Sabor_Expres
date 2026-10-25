package sabor.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import sabor.controller.AdminController;
import sabor.model.Usuario;
import sabor.view.ui.RoundedButton;
import sabor.view.ui.RoundedPanel;
import sabor.view.ui.Theme;
import sabor.view.ui.Ui;

/** Gestión de usuarios y roles (CUS08). Las contraseñas se guardan con hash SHA-256. */
public class UsuariosTab extends JPanel implements Refrescable {
    private final AdminController ctrl;
    private final Usuario actual;
    private List<Usuario> usuarios = new ArrayList<>();
    private Map<String, Integer> roles = new LinkedHashMap<>();
    private boolean cargando;
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[] {"ID", "NOMBRE", "USUARIO", "ROL"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JTextField txtNombre = new JTextField(), txtUsuario = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();
    private final JComboBox<String> cmbRol = new JComboBox<>();

    public UsuariosTab(AdminController ctrl, Usuario actual) {
        this.ctrl = ctrl;
        this.actual = actual;
        setOpaque(false);
        setLayout(new GridBagLayout());

        RoundedPanel izq = new RoundedPanel(Theme.WHITE, Theme.LINE, 18);
        izq.setLayout(new BorderLayout(0, 8));
        izq.setBorder(new EmptyBorder(16, 18, 16, 18));
        izq.add(Ui.etiqueta("Usuarios registrados", Font.BOLD, 14f, Theme.TEXT), BorderLayout.NORTH);
        Ui.tabla(tabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && !cargando) cargarSeleccion(); });
        izq.add(Ui.scroll(tabla), BorderLayout.CENTER);

        RoundedPanel der = new RoundedPanel(Theme.WHITE, Theme.LINE, 18);
        der.setLayout(new BorderLayout());
        der.setBorder(new EmptyBorder(18, 18, 18, 18));
        der.setPreferredSize(new Dimension(330, 100));
        JPanel f = Ui.vertical();
        f.add(Ui.izq(Ui.etiqueta("Datos del usuario", Font.BOLD, 15f, Theme.TEXT)));
        f.add(Ui.hueco(14));
        f.add(Ui.izq(Ui.seccion("NOMBRE")));
        f.add(Ui.hueco(5));
        Ui.campo(txtNombre);
        f.add(Ui.ancho(txtNombre));
        f.add(Ui.hueco(12));
        f.add(Ui.izq(Ui.seccion("USUARIO")));
        f.add(Ui.hueco(5));
        Ui.campo(txtUsuario);
        f.add(Ui.ancho(txtUsuario));
        f.add(Ui.hueco(12));
        f.add(Ui.izq(Ui.seccion("CONTRASEÑA")));
        f.add(Ui.hueco(5));
        Ui.campo(txtClave);
        f.add(Ui.ancho(txtClave));
        f.add(Ui.hueco(12));
        f.add(Ui.izq(Ui.seccion("ROL")));
        f.add(Ui.hueco(5));
        cmbRol.setFont(Theme.font(Font.PLAIN, 14f));
        f.add(Ui.ancho(cmbRol));
        f.add(Ui.hueco(18));
        RoundedButton guardar = new RoundedButton("Guardar Usuario", RoundedButton.Estilo.PRIMARIO);
        guardar.setPreferredSize(new Dimension(100, 42));
        guardar.addActionListener(e -> guardar());
        f.add(Ui.ancho(guardar));
        f.add(Ui.hueco(8));
        RoundedButton rol = new RoundedButton("Cambiar rol del seleccionado", RoundedButton.Estilo.SUAVE);
        rol.setPreferredSize(new Dimension(100, 40));
        rol.addActionListener(e -> cambiarRol());
        f.add(Ui.ancho(rol));
        f.add(Ui.hueco(8));
        RoundedButton elim = new RoundedButton("Eliminar seleccionado", RoundedButton.Estilo.CONTORNO);
        elim.setPreferredSize(new Dimension(100, 40));
        elim.addActionListener(e -> eliminar());
        f.add(Ui.ancho(elim));
        f.add(Ui.hueco(8));
        RoundedButton limpiar = new RoundedButton("Limpiar formulario", RoundedButton.Estilo.CONTORNO);
        limpiar.setPreferredSize(new Dimension(100, 40));
        limpiar.addActionListener(e -> limpiar());
        f.add(Ui.ancho(limpiar));
        der.add(f, BorderLayout.NORTH);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH; c.weighty = 1;
        c.gridx = 0; c.weightx = 1; c.insets = new java.awt.Insets(0, 0, 0, 16);
        add(izq, c);
        c.gridx = 1; c.weightx = 0; c.insets = new java.awt.Insets(0, 0, 0, 0);
        add(der, c);
    }

    private void limpiar() {
        tabla.clearSelection();
        txtNombre.setText("");
        txtUsuario.setText("");
        txtClave.setText("");
        if (cmbRol.getItemCount() > 0) cmbRol.setSelectedIndex(0);
    }

    private Usuario seleccionado() {
        int r = tabla.getSelectedRow();
        if (r < 0 || r >= usuarios.size()) throw new IllegalArgumentException("Selecciona un usuario de la lista.");
        return usuarios.get(r);
    }

    private void cargarSeleccion() {
        int r = tabla.getSelectedRow();
        if (r < 0 || r >= usuarios.size()) return;
        Usuario u = usuarios.get(r);
        txtNombre.setText(u.nombre());
        txtUsuario.setText(u.usuario());
        txtClave.setText("");
        cmbRol.setSelectedItem(u.rol());
    }

    private void guardar() {
        Ui.seguro(this, () -> {
            String rol = (String) cmbRol.getSelectedItem();
            ctrl.crearUsuario(txtNombre.getText(), txtUsuario.getText(), new String(txtClave.getPassword()), roles.get(rol));
            limpiar();
            refrescar();
            JOptionPane.showMessageDialog(this, "Usuario registrado correctamente.", "Usuarios", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void cambiarRol() {
        Ui.seguro(this, () -> {
            Usuario u = seleccionado();
            ctrl.cambiarRol(u.id(), roles.get((String) cmbRol.getSelectedItem()));
            refrescar();
        });
    }

    private void eliminar() {
        Ui.seguro(this, () -> {
            Usuario u = seleccionado();
            int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar al usuario \"" + u.usuario() + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            ctrl.eliminarUsuario(u.id(), actual.id());
            limpiar();
            refrescar();
        });
    }

    @Override public void refrescar() {
        Ui.silencioso(() -> {
            roles = ctrl.roles();
            if (cmbRol.getItemCount() != roles.size()) {
                cmbRol.removeAllItems();
                for (String r : roles.keySet()) cmbRol.addItem(r);
            }
            usuarios = ctrl.usuarios();
            cargando = true;
            modelo.setRowCount(0);
            for (Usuario u : usuarios) modelo.addRow(new Object[] {u.id(), u.nombre(), u.usuario(), u.rol()});
            cargando = false;
        });
    }
}
