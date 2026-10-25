package sabor.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Fmt {
    private Fmt() {}
    private static final Locale ES = Locale.forLanguageTag("es-PE");

    public static String soles(double v) { return String.format(Locale.US, "S/ %.2f", v); }
    public static String codigo(int id) { return String.format("ORD-%03d", id); }
    public static String hora(LocalDateTime t) { return t.format(DateTimeFormatter.ofPattern("HH:mm")); }
    public static String fechaLarga(LocalDate d) {
        return d.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", ES));
    }

    public static String hace(LocalDateTime t) {
        long m = Duration.between(t, LocalDateTime.now()).toMinutes();
        if (m < 1) return "justo ahora";
        if (m == 1) return "Hace 1 minuto";
        if (m < 60) return "Hace " + m + " minutos";
        long h = m / 60;
        return "Hace " + h + (h == 1 ? " hora" : " horas");
    }

    public static String sha256(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
