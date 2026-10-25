package sabor.view.ui;

public final class Emojis {
    private Emojis() {}

    public static String paraRol(String rol) {
        String r = rol == null ? "" : rol.toLowerCase();
        if (r.startsWith("moz")) return "🍽";
        if (r.startsWith("coc")) return "🍳";
        return "💼";
    }

    public static String paraCategoria(String c) {
        return switch (c) {
            case "Entradas" -> "🥗";
            case "Principales" -> "🍽";
            case "Bebidas" -> "🥤";
            case "Postres" -> "🍰";
            default -> "🍴";
        };
    }

    public static String paraPlato(String nombre, String categoria) {
        String n = nombre.toLowerCase();
        if (n.contains("lomo")) return "🥩";
        if (n.contains("ají") || n.contains("aji") || n.contains("pollo")) return "🍗";
        if (n.contains("seco")) return "🍲";
        if (n.contains("ceviche")) return "🦐";
        if (n.contains("causa") || n.contains("papa")) return "🥔";
        if (n.contains("anticucho")) return "🍢";
        if (n.contains("arroz")) return "🍚";
        if (n.contains("suspiro") || n.contains("mazamorra")) return "🍮";
        if (n.contains("inca") || n.contains("chicha")) return "🥤";
        if (n.contains("agua")) return "💧";
        if (n.contains("cerveza")) return "🍺";
        return categoria == null || categoria.isEmpty() ? "🍴" : paraCategoria(categoria);
    }
}
