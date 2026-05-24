package com.dapun.dlc.modules;

import com.dapun.dlc.DapunDLC;
import com.dapun.dlc.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DamageNumbers {

    public static class DamageEntry {
        public float amount;
        public double x, y, z;
        public boolean isCrit;
        public boolean isHeal;
        public int age;
        public int maxAge;
        public float vy; // вертикальная скорость анимации

        public DamageEntry(float amount, double x, double y, double z,
                           boolean isCrit, boolean isHeal, int lifetime) {
            this.amount = amount;
            this.x = x;
            this.y = y;
            this.z = z;
            this.isCrit = isCrit;
            this.isHeal = isHeal;
            this.maxAge = lifetime;
            this.age = 0;
            this.vy = 0.04f;
        }
    }

    private final List<DamageEntry> entries = new ArrayList<>();

    public void addNumber(float amount, double x, double y, double z,
                          boolean isCrit, boolean isHeal) {
        if (!DapunDLC.config.damageNumbersEnabled) return;
        entries.add(new DamageEntry(amount, x, y, z, isCrit, isHeal,
                DapunDLC.config.hitParticleLifetime));
    }

    public void tick(MinecraftClient client) {
        Iterator<DamageEntry> iter = entries.iterator();
        while (iter.hasNext()) {
            DamageEntry e = iter.next();
            e.y += e.vy;
            e.vy *= 0.92f;
            e.age++;
            if (e.age >= e.maxAge) iter.remove();
        }
    }

    public void render(DrawContext context, MinecraftClient client, float tickDelta) {
        if (!DapunDLC.config.damageNumbersEnabled) return;
        if (client.player == null || client.world == null) return;

        MatrixStack matrices = context.getMatrices();

        for (DamageEntry e : entries) {
            float progress = (float) e.age / e.maxAge;
            float alpha = progress < 0.2f
                    ? progress / 0.2f
                    : 1f - ((progress - 0.2f) / 0.8f);
            alpha = Math.max(0f, Math.min(1f, alpha));

            int aInt = (int) (alpha * 255);

            int color;
            String text;
            if (e.isHeal) {
                color = RenderUtils.colorWithAlpha(0x55FF55, aInt);
                text = String.format("+%.1f", e.amount);
            } else if (e.isCrit) {
                color = RenderUtils.colorWithAlpha(0xFF3333, aInt);
                text = String.format("%.1f!", e.amount);
            } else {
                color = RenderUtils.colorWithAlpha(0xFFAA00, aInt);
                text = String.format("%.1f", e.amount);
            }

            // Проецируем 3D → 2D
            Vec3d pos = new Vec3d(e.x, e.y, e.z);
            int[] screen = worldToScreen(client, pos);
            if (screen == null) continue;

            float scale = DapunDLC.config.damageNumbersScale;
            if (e.isCrit) scale *= 1.3f;

            matrices.push();
            matrices.translate(screen[0], screen[1], 0);
            matrices.scale(scale, scale, 1f);

            int textW = client.textRenderer.getWidth(text);
            context.drawText(client.textRenderer, text,
                    -textW / 2, 0, color, true);

            matrices.pop();
        }
    }

    private int[] worldToScreen(MinecraftClient client, Vec3d worldPos) {
        if (client.gameRenderer == null || client.player == null) return null;

        org.joml.Matrix4f proj = client.gameRenderer.getBasicProjectionMatrix(
                client.options.getFov().getValue());
        net.minecraft.util.math.Vec3d cam = client.gameRenderer.getCamera().getPos();

        double rx = worldPos.x - cam.x;
        double ry = worldPos.y - cam.y;
        double rz = worldPos.z - cam.z;

        // Используем матрицы из WorldRenderer
        Matrix4f modelView = new Matrix4f(
                client.getEntityRenderDispatcher().getRotation().toMatrix(new Matrix4f())
        );

        Vector4f vec = new Vector4f((float) rx, (float) ry, (float) rz, 1.0f);
        vec.mul(proj);

        if (vec.w <= 0f) return null;

        float ndcX = vec.x / vec.w;
        float ndcY = vec.y / vec.w;

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        int screenX = (int) ((ndcX + 1f) / 2f * screenW);
        int screenY = (int) ((1f - ndcY) / 2f * screenH);

        if (screenX < -200 || screenX > screenW + 200
                || screenY < -200 || screenY > screenH + 200) return null;

        return new int[]{screenX, screenY};
    }
}
