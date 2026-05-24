package com.dapun.dlc;

import com.dapun.dlc.config.DapunConfig;
import com.dapun.dlc.events.EventHandler;
import com.dapun.dlc.gui.ClickGui;
import com.dapun.dlc.modules.DamageNumbers;
import com.dapun.dlc.modules.HitParticles;
import com.dapun.dlc.modules.SmoothAnimations;
import com.dapun.dlc.modules.TargetHUD;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DapunDLC implements ClientModInitializer {

    public static final String MOD_ID = "dapundlc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static DapunConfig config;
    public static TargetHUD targetHUD;
    public static HitParticles hitParticles;
    public static DamageNumbers damageNumbers;
    public static SmoothAnimations smoothAnimations;
    public static ClickGui clickGui;

    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        config = DapunConfig.load();

        targetHUD = new TargetHUD();
        hitParticles = new HitParticles();
        damageNumbers = new DamageNumbers();
        smoothAnimations = new SmoothAnimations();
        clickGui = new ClickGui();

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dapundlc.opengui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.dapundlc"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(clickGui);
                }
            }
            targetHUD.tick(client);
            damageNumbers.tick(client);
            hitParticles.tick(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickDeltaManager) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            float tickDelta = tickDeltaManager.getTickDelta(true);
            if (client.player != null && client.world != null) {
                targetHUD.render(drawContext, client, tickDelta);
                damageNumbers.render(drawContext, client, tickDelta);
            }
        });

        EventHandler.register();
        LOGGER.info("[DapunDLC] Initialized successfully.");
    }
}
