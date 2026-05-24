package com.dapun.dlc.gui;

import com.dapun.dlc.DapunDLC;
import com.dapun.dlc.config.DapunConfig;
import com.dapun.dlc.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClickGui extends Screen {

    private static final int PANEL_W = 280;
    private static final int PANEL_H = 360;
    private static final int HEADER_H = 24;
    private static final int ITEM_H = 22;
    private static final int SLIDER_W = 120;

    private int panelX, panelY;

    // Drag state
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    // Slider drag
    private int activeSlider = -1;

    public ClickGui() {
        super(Text.literal("DapunDLC Settings"));
    }

    @Override
    protected void init() {
        panelX = (this.width - PANEL_W) / 2;
        panelY = (this.height - PANEL_H) / 2;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Затемнение фона
        context.fill(0, 0, this.width, this.height, 0x88000000);

        DapunConfig cfg = DapunDLC.config;

        // Панель
        RenderUtils.drawRoundedRect(context, panelX, panelY, PANEL_W, PANEL_H, 8, 0xE5101010);
        RenderUtils.drawRoundedRectOutline(context, panelX, panelY, PANEL_W, PANEL_H, 8, 1, 0xFF4A90D9);

        // Заголовок
        context.fill(panelX, panelY, panelX + PANEL_W, panelY + HEADER_H, 0xFF1A2A3A);
        context.drawText(this.textRenderer, "§bDapun§fDLC §7— Settings",
                panelX + 8, panelY + 7, 0xFFFFFF, true);
        context.drawText(this.textRenderer, "§7[RShift to close]",
                panelX + PANEL_W - this.textRenderer.getWidth("§7[RShift to close]") - 6,
                panelY + 7, 0x888888, false);

        int row = panelY + HEADER_H + 6;

        // === TARGET HUD ===
        row = renderSection(context, "§6Target HUD", row, mouseX, mouseY);
        row = renderToggle(context, "Enabled", cfg.targetHudEnabled, row, 0, mouseX, mouseY);
        row = renderSlider(context, "Scale", cfg.targetHudScale, 0.5f, 2.0f, row, 0, mouseX, mouseY);
        row = renderSlider(context, "Pos X", cfg.targetHudX, 0f, 400f, row, 1, mouseX, mouseY);
        row = renderSlider(context, "Pos Y", cfg.targetHudY, 0f, 300f, row, 2, mouseX, mouseY);

        // === HIT PARTICLES ===
        row = renderSection(context, "§cHit Particles", row, mouseX, mouseY);
        row = renderToggle(context, "Enabled", cfg.hitParticlesEnabled, row, 1, mouseX, mouseY);
        row = renderSlider(context, "Size", cfg.hitParticleSize, 0.5f, 3.0f, row, 3, mouseX, mouseY);
        row = renderSlider(context, "Lifetime", cfg.hitParticleLifetime, 5f, 60f, row, 4, mouseX, mouseY);

        // === DAMAGE NUMBERS ===
        row = renderSection(context, "§eNumbers", row, mouseX, mouseY);
        row = renderToggle(context, "Enabled", cfg.damageNumbersEnabled, row, 2, mouseX, mouseY);
        row = renderToggle(context, "Show Heals", cfg.showHealNumbers, row, 3, mouseX, mouseY);
        row = renderSlider(context, "Scale", cfg.damageNumbersScale, 0.5f, 2.0f, row, 5, mouseX, mouseY);

        // === SMOOTH ANIMATIONS ===
        row = renderSection(context, "§aAnimations", row, mouseX, mouseY);
        row = renderToggle(context, "Enabled", cfg.smoothAnimationsEnabled, row, 4, mouseX, mouseY);
        row = renderSlider(context, "Swing Speed", cfg.itemSwingSpeed, 0.5f, 3.0f, row, 6, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    private int renderSection(DrawContext context, String title, int y, int mx, int my) {
        context.fill(panelX + 4, y + 2, panelX + PANEL_W - 4, y + ITEM_H - 2, 0x33FFFFFF);
        context.drawText(this.textRenderer, title, panelX + 8, y + 6, 0xFFFFFF, true);
        return y + ITEM_H;
    }

    // toggleId: уникальный индекс для определения клика
    private int renderToggle(DrawContext context, String label, boolean value,
                              int y, int toggleId, int mx, int my) {
        boolean hovered = mx >= panelX + 4 && mx <= panelX + PANEL_W - 4
                && my >= y && my <= y + ITEM_H;

        int bgColor = hovered ? 0x33FFFFFF : 0x22FFFFFF;
        context.fill(panelX + 4, y + 1, panelX + PANEL_W - 4, y + ITEM_H - 1, bgColor);
        context.drawText(this.textRenderer, label, panelX + 10, y + 6, 0xCCCCCC, false);

        int boxX = panelX + PANEL_W - 34;
        int boxY = y + 5;
        int boxW = 28;
        int boxH = 12;

        int trackColor = value ? 0xFF4A90D9 : 0xFF555555;
        RenderUtils.drawRoundedRect(context, boxX, boxY, boxW, boxH, 6, trackColor);

        int knobX = value ? boxX + boxW - 12 : boxX + 2;
        RenderUtils.drawRoundedRect(context, knobX, boxY + 1, 10, 10, 5, 0xFFFFFFFF);

        return y + ITEM_H;
    }

    // sliderId: уникальный индекс слайдера
    private int renderSlider(DrawContext context, String label, float value,
                              float min, float max, int y, int sliderId, int mx, int my) {
        context.drawText(this.textRenderer, label, panelX + 10, y + 6, 0xAAAAAA, false);

        int sx = panelX + PANEL_W - SLIDER_W - 8;
        int sy = y + 7;
        int sw = SLIDER_W;
        int sh = 8;

        RenderUtils.drawRect(context, sx, sy, sw, sh, 0xFF333333);

        float ratio = (value - min) / (max - min);
        RenderUtils.drawRect(context, sx, sy, (int) (sw * ratio), sh, 0xFF4A90D9);

        int knobX = sx + (int) (sw * ratio) - 4;
        RenderUtils.drawRoundedRect(context, knobX, sy - 2, 8, 12, 4, 0xFFFFFFFF);

        String valStr = (max <= 60 && min >= 0 && (value == (int) value))
                ? String.valueOf((int) value)
                : String.format("%.2f", value);
        context.drawText(this.textRenderer, "§7" + valStr,
                sx - this.textRenderer.getWidth("§7" + valStr) - 4,
                y + 6, 0xFFFFFF, false);

        // Drag update
        if (activeSlider == sliderId) {
            float newRatio = Math.max(0f, Math.min(1f, (float) (mx - sx) / sw));
            float newVal = min + newRatio * (max - min);
            applySlider(sliderId, newVal);
        }

        return y + ITEM_H;
    }

    private void applySlider(int id, float val) {
        DapunConfig cfg = DapunDLC.config;
        switch (id) {
            case 0 -> cfg.targetHudScale = val;
            case 1 -> cfg.targetHudX = val;
            case 2 -> cfg.targetHudY = val;
            case 3 -> cfg.hitParticleSize = val;
            case 4 -> cfg.hitParticleLifetime = (int) val;
            case 5 -> cfg.damageNumbersScale = val;
            case 6 -> cfg.itemSwingSpeed = val;
        }
    }

    private void applyToggle(int id) {
        DapunConfig cfg = DapunDLC.config;
        switch (id) {
            case 0 -> cfg.targetHudEnabled = !cfg.targetHudEnabled;
            case 1 -> cfg.hitParticlesEnabled = !cfg.hitParticlesEnabled;
            case 2 -> cfg.damageNumbersEnabled = !cfg.damageNumbersEnabled;
            case 3 -> cfg.showHealNumbers = !cfg.showHealNumbers;
            case 4 -> cfg.smoothAnimationsEnabled = !cfg.smoothAnimationsEnabled;
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            // Drag заголовок
            if (mx >= panelX && mx <= panelX + PANEL_W
                    && my >= panelY && my <= panelY + HEADER_H) {
                dragging = true;
                dragOffsetX = (int) (mx - panelX);
                dragOffsetY = (int) (my - panelY);
                return true;
            }

            // Тоглы
            int row = panelY + HEADER_H + 6;
            // Обходим секции вручную (та же последовательность что в render)
            row += ITEM_H; // Target HUD header
            row = checkToggleClick((int) mx, (int) my, row, 0) ? row + ITEM_H : row + ITEM_H;
            row = skipSlider(row); row = skipSlider(row); row = skipSlider(row);
            row += ITEM_H; // Hit Particles header
            row = checkToggleClick((int) mx, (int) my, row, 1) ? row + ITEM_H : row + ITEM_H;
            row = skipSlider(row); row = skipSlider(row);
            row += ITEM_H; // Numbers header
            row = checkToggleClick((int) mx, (int) my, row, 2) ? row + ITEM_H : row + ITEM_H;
            row = checkToggleClick((int) mx, (int) my, row, 3) ? row + ITEM_H : row + ITEM_H;
            row = skipSlider(row);
            row += ITEM_H; // Animations header
            checkToggleClick((int) mx, (int) my, row, 4);

            // Слайдеры — начинаем drag
            activeSlider = getSliderAtMouse((int) mx, (int) my);
        }
        return super.mouseClicked(mx, my, button);
    }

    private boolean checkToggleClick(int mx, int my, int row, int toggleId) {
        if (mx >= panelX + 4 && mx <= panelX + PANEL_W - 4
                && my >= row && my <= row + ITEM_H) {
            applyToggle(toggleId);
            DapunDLC.config.save();
            return true;
        }
        return false;
    }

    private int skipSlider(int row) { return row + ITEM_H; }

    private int getSliderAtMouse(int mx, int my) {
        int sx = panelX + PANEL_W - SLIDER_W - 8;
        int sw = SLIDER_W;
        if (mx < sx || mx > sx + sw) return -1;

        int row = panelY + HEADER_H + 6;
        row += ITEM_H; // section
        row += ITEM_H; // toggle
        int[] sliderRows = new int[7];
        sliderRows[0] = row; row += ITEM_H;
        sliderRows[1] = row; row += ITEM_H;
        sliderRows[2] = row; row += ITEM_H;
        row += ITEM_H; // section
        row += ITEM_H; // toggle
        sliderRows[3] = row; row += ITEM_H;
        sliderRows[4] = row; row += ITEM_H;
        row += ITEM_H; // section
        row += ITEM_H; // toggle
        row += ITEM_H; // toggle
        sliderRows[5] = row; row += ITEM_H;
        row += ITEM_H; // section
        row += ITEM_H; // toggle
        sliderRows[6] = row;

        for (int i = 0; i < sliderRows.length; i++) {
            if (my >= sliderRows[i] && my <= sliderRows[i] + ITEM_H) return i;
        }
        return -1;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) {
            dragging = false;
            if (activeSlider != -1) {
                DapunDLC.config.save();
                activeSlider = -1;
            }
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging && button == 0) {
            panelX = (int) (mx - dragOffsetX);
            panelY = (int) (my - dragOffsetY);
            // Clamp
            panelX = Math.max(0, Math.min(this.width - PANEL_W, panelX));
            panelY = Math.max(0, Math.min(this.height - PANEL_H, panelY));
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public void close() {
        DapunDLC.config.save();
        super.close();
    }
}
