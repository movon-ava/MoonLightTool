package toolbox.ui;

import toolbox.model.NodeType;
import toolbox.model.ToolboxNode;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public final class IconFactory {
    private IconFactory() {
    }

    public static BufferedImage createAppIcon(Color accent, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(0, 0, new Color(15, 23, 42), size, size, accent);
        g2.setPaint(gradient);
        g2.fill(new RoundRectangle2D.Double(0, 0, size, size, size * 0.32, size * 0.32));

        drawMoon(g2, size, new Color(253, 224, 71), new Color(255, 255, 255, 36));
        g2.dispose();
        return image;
    }

    public static BufferedImage createNodeIcon(ToolboxNode node, Color baseColor, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(0, 0, baseColor.brighter(), size, size, baseColor.darker());
        g2.setPaint(gradient);
        g2.fill(new RoundRectangle2D.Double(0, 0, size, size, size * 0.4, size * 0.4));

        g2.setColor(new Color(255, 255, 255, 70));
        g2.drawRoundRect(0, 0, size - 1, size - 1, size * 2 / 5, size * 2 / 5);

        drawNodeGlyph(g2, node, size);
        g2.dispose();
        return image;
    }

    private static void drawNodeGlyph(Graphics2D g2, ToolboxNode node, int size) {
        String iconKey = node.getIconKey() == null ? "" : node.getIconKey().trim().toLowerCase();
        if ("moon".equals(iconKey) || node.getType() == NodeType.ROOT) {
            drawMoon(g2, size, Color.WHITE, new Color(255, 255, 255, 30));
            return;
        }
        if ("globe".equals(iconKey) || node.getType() == NodeType.WEB) {
            drawGlobe(g2, size);
            return;
        }
        if ("archive".equals(iconKey)) {
            drawArchive(g2, size);
            return;
        }
        if ("book".equals(iconKey) || node.getType() == NodeType.DOCUMENT) {
            drawArchive(g2, size);
            return;
        }
        if ("terminal".equals(iconKey) || node.getType() == NodeType.TOOL) {
            drawTerminal(g2, size);
            return;
        }
        if ("star".equals(iconKey)) {
            drawStar(g2, size);
            return;
        }
        drawFolder(g2, size);
    }

    private static void drawMoon(Graphics2D g2, int size, Color moonColor, Color glowColor) {
        double outer = size * 0.58;
        double offset = size * 0.14;
        Ellipse2D outerMoon = new Ellipse2D.Double(size * 0.18, size * 0.18, outer, outer);
        Ellipse2D cutMoon = new Ellipse2D.Double(size * 0.18 + offset, size * 0.13, outer, outer);
        Area moon = new Area(outerMoon);
        moon.subtract(new Area(cutMoon));

        g2.setColor(glowColor);
        g2.fill(new Ellipse2D.Double(size * 0.26, size * 0.16, size * 0.14, size * 0.14));
        g2.fill(new Ellipse2D.Double(size * 0.56, size * 0.34, size * 0.08, size * 0.08));

        g2.setColor(moonColor);
        g2.fill(moon);
    }

    private static void drawTerminal(Graphics2D g2, int size) {
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(Math.max(1.8f, size * 0.08f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int left = (int) (size * 0.28);
        int center = (int) (size * 0.46);
        int top = (int) (size * 0.33);
        int middle = (int) (size * 0.5);
        int right = (int) (size * 0.72);
        g2.drawLine(left, top, center, middle);
        g2.drawLine(left, (int) (size * 0.68), center, middle);
        g2.drawLine((int) (size * 0.5), (int) (size * 0.68), right, (int) (size * 0.68));
    }

    private static void drawFolder(Graphics2D g2, int size) {
        g2.setColor(Color.WHITE);
        g2.fillRoundRect((int) (size * 0.2), (int) (size * 0.34), (int) (size * 0.6), (int) (size * 0.36),
                (int) (size * 0.12), (int) (size * 0.12));
        g2.fillRoundRect((int) (size * 0.24), (int) (size * 0.26), (int) (size * 0.22), (int) (size * 0.12),
                (int) (size * 0.06), (int) (size * 0.06));
    }

    private static void drawGlobe(Graphics2D g2, int size) {
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(Math.max(1.5f, size * 0.06f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int x = (int) (size * 0.24);
        int y = (int) (size * 0.24);
        int d = (int) (size * 0.52);
        g2.drawOval(x, y, d, d);
        g2.drawLine(x + d / 2, y, x + d / 2, y + d);
        g2.drawOval(x + d / 4, y, d / 2, d);
        g2.drawLine(x, y + d / 2, x + d, y + d / 2);
    }

    private static void drawArchive(Graphics2D g2, int size) {
        g2.setColor(Color.WHITE);
        g2.fillRoundRect((int) (size * 0.24), (int) (size * 0.26), (int) (size * 0.52), (int) (size * 0.14),
                (int) (size * 0.07), (int) (size * 0.07));
        g2.fillRoundRect((int) (size * 0.24), (int) (size * 0.4), (int) (size * 0.52), (int) (size * 0.32),
                (int) (size * 0.08), (int) (size * 0.08));
        g2.setColor(new Color(255, 255, 255, 170));
        g2.fillRoundRect((int) (size * 0.4), (int) (size * 0.49), (int) (size * 0.2), (int) (size * 0.08),
                (int) (size * 0.03), (int) (size * 0.03));
    }

    private static void drawStar(Graphics2D g2, int size) {
        g2.setColor(Color.WHITE);
        int cx = size / 2;
        int cy = size / 2;
        int r1 = (int) (size * 0.22);
        int r2 = (int) (size * 0.09);
        java.awt.Polygon polygon = new java.awt.Polygon();
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 + i * Math.PI / 5;
            int r = i % 2 == 0 ? r1 : r2;
            polygon.addPoint((int) (cx + Math.cos(angle) * r), (int) (cy - Math.sin(angle) * r));
        }
        g2.fillPolygon(polygon);
    }
}
