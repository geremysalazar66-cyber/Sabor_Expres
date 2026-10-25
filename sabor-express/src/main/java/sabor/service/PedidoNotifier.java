package sabor.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Patrón Observer: las pantallas (mesas, monitor de cocina, caja) se suscriben y se refrescan
 * apenas cambia un pedido en esta terminal. Para otras terminales las pantallas además consultan
 * la base de datos cada pocos segundos.
 */
public final class PedidoNotifier {
    private PedidoNotifier() {}
    private static final List<Runnable> OBSERVADORES = new CopyOnWriteArrayList<>();

    public static void suscribir(Runnable r) { OBSERVADORES.add(r); }
    public static void cancelar(Runnable r) { OBSERVADORES.remove(r); }
    public static void notificar() { for (Runnable r : OBSERVADORES) r.run(); }
}
