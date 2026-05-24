package com.dapun.dlc.modules;

import com.dapun.dlc.DapunDLC;
import com.dapun.dlc.render.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;

public class ChinaHat {

    private float rotation = 0f;

    public void tick() {
        rotation = (rotation + 1.5f) % 360f;
    }

    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate consumers,
                       MinecraftClient client, float tickDelta) {
        if (!DapunDLC.config.chinaHatEnabled) return;
        if (client.world == null || client.player == null) return;

        Vec3d camPos = client.gameRenderer.getCamera().getPos();

        List<LivingEntity> entities = client.world.getEntitiesByClass(
                LivingEntity.class,
                client.player.getBoundingBox().expand(48),
                e -> e != client.player && e.isAlive()
        );

        DapunConfig cfg = DapunDLC.config;
        float r = cfg.chinaHatColorR / 255f;
        float g = cfg.chinaHatColorG / 255f;
        float b = cfg.chinaHatColorB / 255f;
        float a = cfg.chinaHatAlpha;

        RenderUtils.setupRenderSystem();
        RenderSystem.lineWidth(1.5f);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.LINES,
                VertexFormats.LINES);

        for (LivingEntity entity : entities) {
            float ex = (float)(entity.lerp(tickDelta, entity.lastRenderX, entity.getX()) - camPos.x);
            float ey = (float)(entity.lerp(tickDelta, entity.lastRenderY, entity.getY()) - camPos.y);
            float ez = (float)(entity.lerp(tickDelta, entity.lastRenderZ, entity.getZ()) - camPos.z);

            float hatTop  = ey + entity.getHeight() + cfg.chinaHatHeight;
            float hatBase = ey + entity.getHeight();
            float radius  = cfg.chinaHatRadius;
            int   segs    = cfg.chinaHatSegments;

            matrices.push();
            Matrix4f mat = matrices.peek().getPositionMatrix();

            // окружность основания шляпы
            for (int i = 0; i < segs; i++) {
                double a0 = Math.toRadians((double) i / segs * 360.0);
                double a1 = Math.toRadians((double)(i + 1) / segs * 360.0);

                float x0 = ex + (float)(Math.cos(a0) * radius);
                float z0 = ez + (float)(Math.sin(a0) * radius);
                float x1 = ex + (float)(Math.cos(a1) * radius);
                float z1 = ez + (float)(Math.sin(a1) * radius);

                // ребро основания
                RenderUtils.drawLine3D(buf, mat, x0, hatBase, z0, x1, hatBase, z1, r, g, b, a);
                // ребро от основания к вершине
                RenderUtils.drawLine3D(buf, mat, x0, hatBase, z0, ex, hatTop, ez, r, g, b, a);
            }

            // вращающийся ободок
            float rotRad = (float)Math.toRadians(rotation);
            for (int i = 0; i < segs; i++) {
                double a0 = Math.toRadians((double) i / segs * 360.0) + rotRad;
                double a1 = Math.toRadians((double)(i + 1) / segs * 360.0) + rotRad;
                float rimR = radius * 1.25f;
                float rimY = hatBase - 0.05f;

                float x0 = ex + (float)(Math.cos(a0) * rimR);
                float z0 = ez + (float)(Math.sin(a0) * rimR);
                float x1 = ex + (float)(Math.cos(a1) * rimR);
                float z1 = ez + (float)(Math.sin(a1) * rimR);

                // пульсация альфа по ободку
                float pulse = (float)(0.6 + 0.4 * Math.sin(a0 * 3 + rotation * 0.05));
                RenderUtils.drawLine3D(buf, mat, x0, rimY, z0, x1, rimY, z1,
                        r, g, b, a * pulse);
            }

            matrices.pop();
        }

        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderUtils.resetRenderSystem();
    }

    // lerp-хелпер
    private double lerp(float delta, double prev, double curr) {
        return prev + (curr - prev) * delta;
    }
}
