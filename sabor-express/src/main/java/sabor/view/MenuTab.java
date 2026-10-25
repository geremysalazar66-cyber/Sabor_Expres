package sabor.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import sabor.controller.AdminController;
import sabor.model.Estados;
import sabor.model.Plato;
import sabor.util.Fmt;
import sabor.view.ui.RoundedButton;
import sabor.view.ui.RoundedPanel;
import sabor.view.ui.Theme;
import sabor.view.ui.Ui;

/** Gestión del menú (CUS02): registrar, modificar y marcar platos como agotados. */
public class MenuTab extends JPanel implements Refrescable {
    private final AdminController ctrl;
    private List<Plato> platos = new ArrayList<>();
    private boolean cargando;
    private Integer idSel;
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[] {"ID", "NOMBRE", "CATEGORÍA", "PRECIO", "ESTADO"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblForm = Ui.etiqueta("Nuevo plato", Font.BOLD, 15f, Theme.TEXT);
    private final JTextField txtNombre = new JTextField(), txtPrecio = new JTextField();
    private final JComboBox<String> cmbCategoria = new JComboBox<>(new String[] {"Entradas", "Principales", "Bebidas", "Postres"});
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[] {Estados.DISPONIBLE, Estados.AGOTADO});

    public MenuTab(AdminController ctrl) {
        this.ctrl = ctrl;
        setOpaque(false);
        setLayout(new GridBagLayout());

        RoundedPanel izq = new RoundedPanel(Theme.WHITE, Theme.LINE, 18);
        izq.setLayout(new BorderLayout(0, 8));
        izq.setBorder(new EmptyBorder(16, 18, 16, 18));
        izq.add(Ui.etiqueta("Carta del restaurante", Font.BOLD, 14f, Theme.TEXT), BorderLayout.NORTH);
        Ui.tabla(tabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && !cargando) cargarSeleccion(); });
        izq.add(Ui.scroll(tabla), BorderLayout.CENTER);

        RoundedPanel der = new RoundedPanel(Theme.WHITE, Theme.LINE, 18);
        der.setLayout(new BorderLayout());
        der.setBorder(new EmptyBorder(18, 18, 18, 18));
        der.setPreferredSize(new Dimension(330, 100));
        JPanel f = Ui.vertical();
        f.add(Ui.izq(lblForm));
        f.add(Ui.hueco(14));
        f.add(Ui.izq(Ui.seccion("NOMBRE")));
        f.add(Ui.hueco(5));
        Ui.campo(txtNombre);
        f.add(Ui.ancho(txtNombre));
        f.add(Ui.hueco(12));
        f.add(Ui.izq(Ui.seccion("CATEGORÍA")));
        f.add(Ui.hueco(5));
        cmbCategoria.setFont(Theme.font(Font.PLAIN, 14f));
        f.add(Ui.ancho(cmbCategoria));
        f.add(Ui.hueco(12));
        f.add(Ui.izq(Ui.seccion("PRECIO (S/)")));
        f.add(Ui.hueco(5));
        Ui.campo(txtPrecio);
        f.add(Ui.ancho(txtPrecio));
        f.add(Ui.hueco(12));
        f.add(Ui.izq(Ui.seccion("ESTADO")));
        f.add(Ui.hueco(5));
        cmbEstado.setFont(Theme.font(Font.PLAIN, 14f));
        f.add(Ui.ancho(cmbEstado));
        f.add(Ui.hueco(18));
        RoundedButton guardar = new RoundedButton("Guardar", RoundedButton.Estilo.PRIMARIO);
        guardar.setPreferredSize(new Dimension(100, 42));
        guardar.addActionListener(e -> guardar());
        f.add(Ui.ancho(guardar));
        f.add(Ui.hueco(8));
        RoundedButton nuevo = new RoundedButton("Nuevo plato", RoundedButton.Estilo.CONTORNO);
        nuevo.setPreferredSize(new Dimension(100, 42));
        nuevo.addActionListener(e -> nuevo());
        f.add(Ui.ancho(nuevo));
        der.add(f, BorderLayout.NORTH);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH; c.weighty = 1;
        c.gridx = 0; c.weightx = 1; c.insets = new java.awt.Insets(0, 0, 0, 16);
        add(izq, c);
        c.gridx = 1; c.weightx = 0; c.insets = new java.awt.Insets(0, 0, 0, 0);
        add(der, c);
    }

    private void nuevo() {
        idSel = null;
        tabla.clearSelection();
        lblForm.setText("Nuevo plato");
        txtNombre.setText("");
        txtPrecio.setText("");
        cmbCategoria.setSelectedIndex(0);
        cmbEstado.setSelectedItem(Estados.DISPONIBLE);
    }

    private void cargarSeleccion() {
        int r = tabla.getSelectedRow();
        if (r < 0 || r >= platos.size()) return;
        Plato p = platos.get(r);
        idSel = p.id();
        lblForm.setText("Editar plato");
        txtNombre.setText(p.nombre());
        txtPrecio.setText(String.format(java.util.Locale.US, "%.2f", p.precio()));
        cmbCategoria.setSelectedItem(p.categoria());
        cmbEstado.setSelectedItem(p.disponible() ? Estados.DISPONIBLE : Estados.AGOTADO);
    }

    private void guardar() {
        Ui.seguro(this, () -> {
            ctrl.guardarPlato(idSel, txtNombre.getText(), (String) cmbCategoria.getSelectedItem(),
                    txtPrecio.getText(), (String) cmbEstado.getSelectedItem());
            nuevo();
            refrescar();
        });
    }

    @Override public void refrescar() {
        Ui.silencioso(() -> {
            platos = ctrl.platos();
            cargando = true;
            modelo.setRowCount(0);
            for (Plato p : platos) {
                modelo.addRow(new Object[] {p.id(), p.nombre(), p.categoria(), Fmt.soles(p.precio()),
                        p.disponible() ? "Disponible" : "Agotado"});
            }
            cargando = false;
        });
    }
}
