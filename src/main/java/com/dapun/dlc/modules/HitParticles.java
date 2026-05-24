package com.dapun.dlc.modules;

import com.dapun.dlc.DapunDLC;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Random;

public class HitParticles {

    private final Random random = new Random();

    public void spawnParticles(float x, float y, float z, boolean isCrit) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        int count = isCrit ? 10 : 5;
        float size = DapunDLC.config.hitParticleSize;

        // Золотой для обычного, красный для крита
        Vector3f color = isCrit
                ? new Vector3f(1.0f, 0.1f, 0.1f)
                : new Vector3f(1.0f, 0.8f, 0.0f);

        DustParticleEffect particle = new DustParticleEffect(color, size);

        for (int i = 0; i < count; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.5;
            double offsetY = (random.nextDouble()) * 0.5;
            double offsetZ = (random.nextDouble() - 0.5) * 0.5;

            double velX = (random.nextDouble() - 0.5) * 0.15;
            double velY = random.nextDouble() * 0.2;
            double velZ = (random.nextDouble() - 0.5) * 0.15;

            client.world.addParticle(particle,
                    x + offsetX, y + offsetY, z + offsetZ,
                    velX, velY, velZ);
        }
    }

    public void tick(MinecraftClient client) {
        // Частицы управляются движком, tick не требуется
    }
}
