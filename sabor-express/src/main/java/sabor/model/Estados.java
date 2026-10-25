package sabor.model;

/** Valores de texto que se guardan en las columnas "estado" y "metodo_pago". */
public final class Estados {
    private Estados() {}
    // mesas
    public static final String LIBRE = "LIBRE", OCUPADA = "OCUPADA", POR_PAGAR = "POR_PAGAR";
    // pedidos
    public static final String PENDIENTE = "PENDIENTE", EN_PREPARACION = "EN_PREPARACION",
            LISTO = "LISTO", COBRADO = "COBRADO";
    // menu
    public static final String DISPONIBLE = "DISPONIBLE", AGOTADO = "AGOTADO";
    // pagos
    public static final String EFECTIVO = "EFECTIVO", TARJETA = "TARJETA", YAPE_PLIN = "YAPE_PLIN";

    public static String metodoTexto(String m) {
        return switch (m) {
            case EFECTIVO -> "Efectivo";
            case TARJETA -> "Tarjeta";
            case YAPE_PLIN -> "Yape/Plin";
            default -> m;
        };
    }
}
