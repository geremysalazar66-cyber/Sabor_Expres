package sabor.view.ui;

import java.awt.Color;
import java.awt.Font;

public final class Theme {
    private Theme() {}
    public static final Color BG = new Color(0xF1F5F9), WHITE = Color.WHITE, TEXT = new Color(0x1E293B),
            MUTED = new Color(0x64748B), LINE = new Color(0xE2E8F0), FIELD = new Color(0xF8FAFC),
            BLUE = new Color(0x1E7BF0), BLUE2 = new Color(0x3B9BFF), BLUE_SOFT = new Color(0xEFF6FF), BLUE_LINE = new Color(0x93C5FD),
            GREEN = new Color(0x047857), GREEN_BTN = new Color(0x16A34A), GREEN_BG = new Color(0xECFDF5), GREEN_LINE = new Color(0x6EE7B7),
            ORANGE = new Color(0xC2410C), ORANGE_BG = new Color(0xFFF7ED), ORANGE_LINE = new Color(0xFDBA74), ORANGE_DOT = new Color(0xF97316),
            RED = new Color(0xDC2626), RED_BG = new Color(0xFEF2F2), RED_LINE = new Color(0xFCA5A5), RED_DOT = new Color(0xEF4444),
            PURPLE = new Color(0x9333EA), PURPLE_BG = new Color(0xFAF5FF), PURPLE_LINE = new Color(0xD8B4FE);

    public static Font font(int style, float size) { return new Font("Segoe UI", style, 12).deriveFont(style, size); }
    public static Font mono(int style, float size) { return new Font(Font.MONOSPACED, style, 12).deriveFont(style, size); }
    public static Font emoji(float size) { return new Font("Segoe UI Emoji", Font.PLAIN, 12).deriveFont(Font.PLAIN, size); }
}
