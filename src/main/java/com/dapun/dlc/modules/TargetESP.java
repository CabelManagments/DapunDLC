package com.dapun.dlc.modules;

import com.dapun.dlc.DapunDLC;
import com.dapun.dlc.render.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TargetESP {

    private float ringAngle = 0f;      // текущий угол вращения
    private LivingEntity trackedTarget = null;

    public void setTarget(LivingEntity entity) {
        this.trackedTarget = entity;
    }

    public void tick() {
        if (!DapunDLC.config.targetEspEnabled) return;
        float speed = DapunDLC.config.espRingSpeed;
        ringAngle = (ringAngle + speed) % 360f;

        if (trackedTarget != null && !trackedTarget.isAlive()) {
            trackedTarget = null;
        }
    }

    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate consumers,
                       MinecraftClient client, float tickDelta) {
        if (!DapunDLC.config.targetEspEnabled) return;
        if (trackedTarget == null) return;
        if (client.world == null || client.player == null) return;

        double dist = client.player.distanceTo(trackedTarget);
        if (dist > DapunDLC.config.espMaxDistance) return;

        Vec3d cam = client.gameRenderer.getCamera().getPos();

        float ex = (float)(lerp(tickDelta, trackedTarget.lastRenderX, trackedTarget.getX()) - cam.x);
        float ey = (float)(lerp(tickDelta, trackedTarget.lastRenderY, trackedTarget.getY()) - cam.y);
        float ez = (float)(lerp(tickDelta, trackedTarget.lastRenderZ, trackedTarget.getZ()) - cam.z);

        float centerY = ey + trackedTarget.getHeight() / 2f;

        DapunConfig cfg = DapunDLC.config;
        float radius   = cfg.espRingRadius;
        float tiltDeg  = cfg.espRingTilt;
        float cr       = cfg.espColorR / 255f;
        float cg       = cfg.espColorG / 255f;
        float cb       = cfg.espColorB / 255f;
        float ca       = cfg.espAlpha;

        int segments   = 64;
        float tilt     = (float)Math.toRadians(tiltDeg);
        float sinTilt  = (float)Math.sin(tilt);
        float cosTilt  = (float)Math.cos(tilt);

        RenderUtils.setupRenderSystem();
        RenderSystem.lineWidth(cfg.espRingThickness);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.LINES,
                VertexFormats.LINES);

        matrices.push();
        Matrix4f mat = matrices.peek().getPositionMatrix();

        float rotRad = (float)Math.toRadians(ringAngle);

        // ── основное кольцо (наклонное) ───────────────────────────
        for (int i = 0; i < segments; i++) {
            double a0 = (double) i       / segments * Math.PI * 2 + rotRad;
            double a1 = (double)(i + 1)  / segments * Math.PI * 2 + rotRad;

            // точка на наклонном эллипсе
            float[] p0 = ringPoint(ex, centerY, ez, radius, a0, sinTilt, cosTilt);
            float[] p1 = ringPoint(ex, centerY, ez, radius, a1, sinTilt, cosTilt);

            // радужный сдвиг по кольцу для красоты
            float hue = (float)i / segments;
            float[] hsv = hsvToRgb(hue, 0.7f, 1.0f);
            // смешиваем с базовым цветом
            float fr = (cr + hsv[0]) / 2f;
            float fg = (cg + hsv[1]) / 2f;
            float fb = (cb + hsv[2]) / 2f;

            // пульсация
            float pulse = (float)(0.75 + 0.25 * Math.sin(a0 * 2 + ringAngle * 0.04));

            RenderUtils.drawLine3D(buf, mat, p0[0], p0[1], p0[2],
                    p1[0], p1[1], p1[2], fr, fg, fb, ca * pulse);
        }

        // ── второе кольцо (контра-вращение) ──────────────────────
        float rotRad2 = -rotRad * 0.6f;
        float radius2 = radius * 0.75f;
        float tilt2   = tilt * 0.5f;
        float sinT2   = (float)Math.sin(tilt2 + (float)Math.PI / 4);
        float cosT2   = (float)Math.cos(tilt2 + (float)Math.PI / 4);

        for (int i = 0; i < segments; i++) {
            double a0 = (double) i      / segments * Math.PI * 2 + rotRad2;
            double a1 = (double)(i + 1) / segments * Math.PI * 2 + rotRad2;

            float[] p0 = ringPoint(ex, centerY, ez, radius2, a0, sinT2, cosT2);
            float[] p1 = ringPoint(ex, centerY, ez, radius2, a1, sinT2, cosT2);

            float alpha2 = ca * 0.6f * (float)(0.7 + 0.3 * Math.sin(a0 * 3 - ringAngle * 0.05));
            RenderUtils.drawLine3D(buf, mat, p0[0], p0[1], p0[2],
                    p1[0], p1[1], p1[2], cr * 0.8f, cg * 0.8f, cb, alpha2);
        }

        // ── спицы (линии от центра к кольцу) ─────────────────────
        int spokeCount = 8;
        for (int i = 0; i < spokeCount; i++) {
            double a = (double) i / spokeCount * Math.PI * 2 + rotRad;
            float[] p = ringPoint(ex, centerY, ez, radius, a, sinTilt, cosTilt);
            float spokeA = ca * 0.4f;
            RenderUtils.drawLine3D(buf, mat, ex, centerY, ez,
                    p[0], p[1], p[2], cr, cg, cb, spokeA);
        }

        matrices.pop();

        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderUtils.resetRenderSystem();
    }

    // ── точка на наклонном кольце ─────────────────────────────────
    private float[] ringPoint(float cx, float cy, float cz, float r,
                               double angle, float sinTilt, float cosTilt) {
        float cosA = (float)Math.cos(angle);
        float sinA = (float)Math.sin(angle);

        float x = cx + cosA * r;
        float y = cy + sinA * r * sinTilt;
        float z = cz + sinA * r * cosTilt;
        return new float[]{x, y, z};
    }

    // ── HSV → RGB ─────────────────────────────────────────────────
    private float[] hsvToRgb(float h, float s, float v) {
        int   hi = (int)(h * 6) % 6;
        float f  = h * 6 - (int)(h * 6);
        float p  = v * (1 - s);
        float q  = v * (1 - f * s);
        float t  = v * (1 - (1 - f) * s);
        return switch (hi) {
            case 0  -> new float[]{v, t, p};
            case 1  -> new float[]{q, v, p};
            case 2  -> new float[]{p, v, t};
            case 3  -> new float[]{p, q, v};
            case 4  -> new float[]{t, p, v};
            default -> new float[]{v, p, q};
        };
    }

    private double lerp(float delta, double prev, double curr) {
        return prev + (curr - prev) * delta;
    }
}
