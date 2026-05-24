package com.dapun.dlc.events;

import com.dapun.dlc.DapunDLC;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.ActionResult;

public class EventHandler {

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient) return ActionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;

            boolean isCrit = player.fallDistance > 0.0f
                    && !player.isOnGround()
                    && !player.isClimbing()
                    && !player.isTouchingWater()
                    && !player.hasStatusEffect(StatusEffects.BLINDNESS)
                    && player.getAttacker() == null
                    && player.getVelocity().y < 0;

            float dmg = (float) player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
            if (isCrit) dmg *= 1.5f;

            float tx = (float) target.getX();
            float ty = (float)(target.getY() + target.getHeight());
            float tz = (float) target.getZ();

            MinecraftClient client = MinecraftClient.getInstance();

            if (DapunDLC.config.hitParticlesEnabled)
                DapunDLC.hitParticles.spawnParticles(tx, ty, tz, isCrit);

            if (DapunDLC.config.damageNumbersEnabled)
                DapunDLC.damageNumbers.addNumber(dmg, tx, ty, tz, isCrit, false);

            DapunDLC.targetHUD.setTarget(target);
            DapunDLC.targetESP.setTarget(target);
            DapunDLC.hitSound.play(client, isCrit);

            return ActionResult.PASS;
        });
    }
}
