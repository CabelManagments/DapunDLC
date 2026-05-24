package com.dapun.dlc.modules;

import com.dapun.dlc.DapunDLC;
import com.dapun.dlc.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ArmorItem;

public class TargetHUD {

    private LivingEntity target;
    private int displayTimer = 0;
    private static final int DISPLAY_TICKS = 100;

    private float animAlpha = 0f;

    public void setTarget(LivingEntity entity) {
        this.target = entity;
        this.displayTimer = DISPLAY_TICKS;
    }

    public void tick(MinecraftClient client) {
        if (!DapunDLC.config.targetHudEnabled) return;
        if (displayTimer > 0) {
            displayTimer--;
            animAlpha = Math.min(1f, animAlpha + 0.1f);
        } else {
            animAlpha = Math.max(0f, animAlpha - 0.05f);
            if (animAlpha <= 0f) target = null;
        }
    }

    public void render(DrawContext context, MinecraftClient client, float tickDelta) {
        if (!DapunDLC.config.targetHudEnabled) return;
        if (target == null || animAlpha <= 0f) return;
        if (!target.isAlive()) {
            target = null;
            return;
        }

        float scale = DapunDLC.config.targetHudScale;
        int x = (int) DapunDLC.config.targetHudX;
        int y = (int) DapunDLC.config.targetHudY;

        int panelW = 160;
        int panelH = 60;

        int alpha = (int) (animAlpha * 200);

        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1f);

        // Фон панели
        RenderUtils.drawRoundedRect(context, x, y, panelW, panelH, 5,
                RenderUtils.colorWithAlpha(0x101010, alpha));
        // Обводка
        RenderUtils.drawRoundedRectOutline(context, x, y, panelW, panelH, 5, 1,
                RenderUtils.colorWithAlpha(0x4A90D9, alpha));

        // Имя цели
        String name = target.getName().getString();
        context.drawText(client.textRenderer, name,
                x + 6, y + 5, RenderUtils.colorWithAlpha(0xFFFFFF, alpha), true);

        // HP
        float hp = target.getHealth();
        float maxHp = target.getMaxHealth();
        String hpText = String.format("HP: %.1f / %.1f", hp, maxHp);
        int hpColor = hp > maxHp * 0.5f ? 0x55FF55 : hp > maxHp * 0.25f ? 0xFFAA00 : 0xFF4444;
        context.drawText(client.textRenderer, hpText,
                x + 6, y + 17, RenderUtils.colorWithAlpha(hpColor, alpha), true);

        // Полоска HP
        int barW = panelW - 12;
        int barH = 4;
        int barX = x + 6;
        int barY = y + 27;
        float hpRatio = hp / maxHp;
        RenderUtils.drawRect(context, barX, barY, barW, barH,
                RenderUtils.colorWithAlpha(0x333333, alpha));
        RenderUtils.drawRect(context, barX, barY, (int) (barW * hpRatio), barH,
                RenderUtils.colorWithAlpha(hpColor, alpha));

        // Броня
        int armor = getArmorPoints(target);
        String armorText = "Armor: " + armor;
        context.drawText(client.textRenderer, armorText,
                x + 6, y + 35, RenderUtils.colorWithAlpha(0xAAAAAA, alpha), true);

        // Дистанция
        if (client.player != null) {
            double dist = client.player.distanceTo(target);
            String distText = String.format("Dist: %.1fm", dist);
            context.drawText(client.textRenderer, distText,
                    x + 6, y + 45, RenderUtils.colorWithAlpha(0x88CCFF, alpha), true);
        }

        context.getMatrices().pop();
    }

    private int getArmorPoints(LivingEntity entity) {
        int total = 0;
        for (ItemStack stack : entity.getArmorItems()) {
            if (stack.getItem() instanceof ArmorItem armor) {
                total += armor.getProtection();
            }
        }
        return total;
    }
}
