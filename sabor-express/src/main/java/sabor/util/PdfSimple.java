package sabor.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Generador mínimo de PDF (solo texto, Helvetica) para no depender de librerías externas.
 * Las líneas que empiezan con "# " se escriben en negrita.
 */
public final class PdfSimple {
    private PdfSimple() {}
    private static final int POR_PAGINA = 46;

    public static void escribir(File f, List<String> lineas) throws IOException {
        int nPag = Math.max(1, (lineas.size() + POR_PAGINA - 1) / POR_PAGINA);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offs = new ArrayList<>();
        w(out, "%PDF-1.4\n");
        obj(out, offs, 1, "<< /Type /Catalog /Pages 2 0 R >>");
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < nPag; i++) kids.append(5 + 2 * i).append(" 0 R ");
        obj(out, offs, 2, "<< /Type /Pages /Kids [" + kids + "] /Count " + nPag + " >>");
        obj(out, offs, 3, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        obj(out, offs, 4, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");
        for (int p = 0; p < nPag; p++) {
            StringBuilder c = new StringBuilder("BT\n14 TL\n50 790 Td\n/F1 11 Tf\n");
            for (int i = p * POR_PAGINA; i < Math.min(lineas.size(), (p + 1) * POR_PAGINA); i++) {
                String l = lineas.get(i);
                boolean h = l.startsWith("# ");
                if (h) { l = l.substring(2); c.append("/F2 13 Tf\n"); }
                c.append('(').append(esc(l)).append(") Tj T*\n");
                if (h) c.append("/F1 11 Tf\n");
            }
            c.append("ET");
            obj(out, offs, 5 + 2 * p, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + (6 + 2 * p) + " 0 R >>");
            obj(out, offs, 6 + 2 * p, "<< /Length " + c.length() + " >>\nstream\n" + c + "\nendstream");
        }
        int xref = out.size();
        w(out, "xref\n0 " + (offs.size() + 1) + "\n0000000000 65535 f \n");
        for (int o : offs) w(out, String.format("%010d 00000 n \n", o));
        w(out, "trailer\n<< /Size " + (offs.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
        Files.write(f.toPath(), out.toByteArray());
    }

    private static void obj(ByteArrayOutputStream o, List<Integer> offs, int n, String body) {
        offs.add(o.size());
        w(o, n + " 0 obj\n" + body + "\nendobj\n");
    }
    private static void w(ByteArrayOutputStream o, String s) { o.writeBytes(s.getBytes(StandardCharsets.ISO_8859_1)); }

    private static String esc(String s) {
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (ch == '(' || ch == ')' || ch == '\\') sb.append('\\').append(ch);
            else if (ch > 255) sb.append('?');
            else sb.append(ch);
        }
        return sb.toString();
    }
}
