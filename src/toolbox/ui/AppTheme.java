package toolbox.ui;

import java.awt.Color;
import java.awt.Font;
import toolbox.model.ToolboxNode;

public final class AppTheme {
    public static final Color BACKGROUND = new Color(238, 242, 247);
    public static final Color BACKDROP = new Color(225, 232, 241);
    public static final Color PANEL = new Color(250, 252, 255);
    public static final Color PANEL_SOFT = new Color(231, 238, 247);
    public static final Color PANEL_DARK = new Color(18, 26, 38);
    public static final Color PANEL_DARK_SOFT = new Color(31, 40, 56);
    public static final Color TEXT = new Color(22, 31, 45);
    public static final Color SUBTEXT = new Color(97, 108, 124);
    public static final Color SUCCESS = new Color(32, 138, 82);
    public static final Color DANGER = new Color(191, 62, 62);

    public static final Font TITLE_FONT = new Font("Microsoft YaHei UI", Font.BOLD, 30);
    public static final Font HEADING_FONT = new Font("Microsoft YaHei UI", Font.BOLD, 18);
    public static final Font BODY_FONT = new Font("Microsoft YaHei UI", Font.PLAIN, 15);
    public static final Font SMALL_FONT = new Font("Microsoft YaHei UI", Font.PLAIN, 13);

    private AppTheme() {
    }

    public static Color parseAccent(String hex) {
        try {
            return Color.decode(hex);
        } catch (Exception ignored) {
            return new Color(31, 111, 235);
        }
    }

    public static Color resolveNodeColor(ToolboxNode node, Color fallback) {
        if (node == null) {
            return fallback;
        }
        String colorHex = node.getColorHex();
        if (colorHex != null && !colorHex.trim().isEmpty()) {
            try {
                return Color.decode(colorHex.trim());
            } catch (Exception ignored) {
            }
        }
        return fallback;
    }

    public static String toHex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }
}
