package org.eris.utils.render.animation;

import lombok.Setter;

public class AnimationUtil {
    @Setter
    private static long delta;

    /**
     * @param current The raw number
     * @param target The target number
     * @param speed The animation speed (The speed increases as this value increases)
     * @return The animated number
     */
    public static float smooth(float current, float target, float speed) {
        long deltaTime = delta;

        speed = Math.abs(target - current) * speed;

        if (deltaTime < 1L) {
            deltaTime = 1L;
        }

        final float difference = current - target;
        final float smoothing = Math.max(speed * (deltaTime / 16F), .15F);

        if (difference > speed) {
            current = Math.max(current - smoothing, target);
        } else if (difference < -speed) {
            current = Math.min(current + smoothing, target);
        } else {
            current = target;
        }

        return current;
    }

    public static double smooth(double current, double target, double speed) {
        long deltaTime = delta;

        speed = Math.abs(target - current) * speed;

        if (deltaTime < 1L) {
            deltaTime = 1L;
        }

        final double difference = current - target;
        final double smoothing = Math.max(speed * (deltaTime / 16F), .15F);

        if (difference > speed) {
            current = Math.max(current - smoothing, target);
        } else if (difference < -speed) {
            current = Math.min(current + smoothing, target);
        } else {
            current = target;
        }

        return current;
    }
}
