package com.dapun.dlc.render;

import net.minecraft.client.gui.DrawContext;

public class RenderUtils {

    public static int colorWithAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    public static void drawRect(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + h, color);
    }

    public static void drawRoundedRect(DrawContext context, int x, int y, int w, int h,
                                        int radius, int color) {
        // Центральная часть
        context.fill(x + radius, y, x + w - radius, y + h, color);
        // Левая и правая полосы
        context.fill(x, y + radius, x + radius, y + h - radius, color);
        context.fill(x + w - radius, y + radius, x + w, y + h - radius, color);
        // Углы (аппроксимация кругами через заливки)
        drawCircleQuadrant(context, x + radius, y + radius, radius, color, 0);
        drawCircleQuadrant(context, x + w - radius, y + radius, radius, color, 1);
        drawCircleQuadrant(context, x + radius, y + h - radius, radius, color, 2);
        drawCircleQuadrant(context, x + w - radius, y + h - radius, radius, color, 3);
    }

    public static void drawRoundedRectOutline(DrawContext context, int x, int y, int w, int h,
                                               int radius, int thickness, int color) {
        // Верх/низ
        context.fill(x + radius, y, x + w - radius, y + thickness, color);
        context.fill(x + radius, y + h - thickness, x + w - radius, y + h, color);
        // Лево/право
        context.fill(x, y + radius, x + thickness, y + h - radius, color);
        context.fill(x + w - thickness, y + radius, x + w, y + h - radius, color);
    }

    private static void drawCircleQuadrant(DrawContext context, int cx, int cy,
                                            int radius, int color, int quadrant) {
        for (int dy = 0; dy < radius; dy++) {
            int dx = (int) Math.sqrt((double) radius * radius - (double) dy * dy);
            int x1, y1, x2, y2;
            switch (quadrant) {
                case 0 -> { x1 = cx - dx; y1 = cy - radius + dy; x2 = cx; y2 = cy - radius + dy + 1; }
                case 1 -> { x1 = cx; y1 = cy - radius + dy; x2 = cx + dx; y2 = cy - radius + dy + 1; }
                case 2 -> { x1 = cx - dx; y1 = cy + dy; x2 = cx; y2 = cy + dy + 1; }
                default -> { x1 = cx; y1 = cy + dy; x2 = cx + dx; y2 = cy + dy + 1; }
            }
            context.fill(x1, y1, x2, y2, color);
        }
    }
}
