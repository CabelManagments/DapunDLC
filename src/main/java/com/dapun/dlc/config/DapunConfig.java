package com.dapun.dlc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class DapunConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("dapundlc.json");

    // --- TargetHUD ---
    public boolean targetHudEnabled = true;
    public float targetHudX = 10f;
    public float targetHudY = 10f;
    public float targetHudScale = 1.0f;

    // --- HitParticles ---
    public boolean hitParticlesEnabled = true;
    public float hitParticleSize = 1.0f;
    public int hitParticleLifetime = 20;

    // --- DamageNumbers ---
    public boolean damageNumbersEnabled = true;
    public float damageNumbersScale = 1.0f;
    public boolean showHealNumbers = true;

    // --- SmoothAnimations ---
    public boolean smoothAnimationsEnabled = true;
    public float itemSwingSpeed = 1.0f;

    public static DapunConfig load() {
        if (CONFIG_PATH.toFile().exists()) {
            try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
                return GSON.fromJson(reader, DapunConfig.class);
            } catch (IOException e) {
                System.err.println("[DapunDLC] Failed to load config, using defaults: " + e.getMessage());
            }
        }
        DapunConfig cfg = new DapunConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            System.err.println("[DapunDLC] Failed to save config: " + e.getMessage());
        }
    }
}
