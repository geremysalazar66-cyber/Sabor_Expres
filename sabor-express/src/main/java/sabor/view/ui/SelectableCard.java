package sabor.view.ui;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Tarjeta que se puede seleccionar con un clic (roles del login, método de pago). */
public class SelectableCard extends RoundedPanel {
    public SelectableCard(Runnable alClic) {
        super(Theme.WHITE, Theme.LINE, 16);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { alClic.run(); }
        });
    }
    public void setSeleccionada(boolean s) {
        if (s) colores(Theme.BLUE_SOFT, Theme.BLUE); else colores(Theme.WHITE, Theme.LINE);
    }
}
