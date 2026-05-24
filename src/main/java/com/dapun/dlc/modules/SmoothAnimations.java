package com.dapun.dlc.modules;

import com.dapun.dlc.DapunDLC;

public class SmoothAnimations {

    private float swingProgress = 0f;
    private float prevSwingProgress = 0f;
    private boolean isSwinging = false;

    public float getSmoothedSwing(float rawSwing, float tickDelta) {
        if (!DapunDLC.config.smoothAnimationsEnabled) return rawSwing;
        float speed = DapunDLC.config.itemSwingSpeed;
        float target = rawSwing;
        swingProgress += (target - swingProgress) * 0.3f * speed;
        return swingProgress;
    }

    public void onSwing() {
        isSwinging = true;
    }
}
