package com.dapun.dlc.events;

import com.dapun.dlc.DapunDLC;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
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
                    && !player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS)
                    && player.getAttacker() == null
                    && player.getVelocity().y < 0;

            float damage = (float) player.getAttributeValue(
                    net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE);

            if (isCrit) damage *= 1.5f;

            float targetX = (float) target.getX();
            float targetY = (float) (target.getY() + target.getHeight());
            float targetZ = (float) target.getZ();

            if (DapunDLC.config.hitParticlesEnabled) {
                DapunDLC.hitParticles.spawnParticles(targetX, targetY, targetZ, isCrit);
            }

            if (DapunDLC.config.damageNumbersEnabled) {
                DapunDLC.damageNumbers.addNumber(damage, targetX, targetY, targetZ, isCrit, false);
            }

            DapunDLC.targetHUD.setTarget(target);

            return ActionResult.PASS;
        });
    }
}
