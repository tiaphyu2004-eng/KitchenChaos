package io.github.projectgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import java.util.List;

public class VaccumCleaner {

    private final Texture texture;
    private final Sprite sprite;

    private float speed = 110f;

    private boolean broken = false;
    private float breakdownTimer = 0f;
    private float breakdownCheckTimer = 5f;

    private static final float BREAKDOWN_CHECK_INTERVAL = 5f;
    private static final float BREAKDOWN_CHANCE = 0.10f;
    private static final float RECOVERY_TIME = 15f;

    private float wanderTimer = 0f;
    private float wanderAngle = 0f;

    public VaccumCleaner(float startX, float startY) {
        texture = new Texture(
            Gdx.files.internal("assets/VaccumCleanerPTF.png")
        );

        sprite = new Sprite(texture);
        sprite.setSize(55, 55);
        sprite.setPosition(startX, startY);
    }

    public void update(float delta, List<Dust> dustPiles) {

        // A broken vacuum cannot move or clean.
        if (broken) {
            breakdownTimer -= delta;

            if (breakdownTimer <= 0f) {
                breakdownTimer = 0f;
                broken = false;
                breakdownCheckTimer = BREAKDOWN_CHECK_INTERVAL;
            }

            return;
        }

        // Check for a random breakdown every 5 seconds.
        breakdownCheckTimer -= delta;

        if (breakdownCheckTimer <= 0f) {
            breakdownCheckTimer = BREAKDOWN_CHECK_INTERVAL;

            if (MathUtils.random() < BREAKDOWN_CHANCE) {
                broken = true;
                breakdownTimer = RECOVERY_TIME;
                return;
            }
        }

        // Find the nearest dust pile.
        Dust nearestDust = null;
        float nearestDistance = Float.MAX_VALUE;

        float cleanerCenterX =
            sprite.getX() + sprite.getWidth() / 2f;
        float cleanerCenterY =
            sprite.getY() + sprite.getHeight() / 2f;

        for (Dust dust : dustPiles) {
            if (dust.isCleaned()) {
                continue;
            }

            float dustCenterX =
                dust.getX() + dust.getWidth() / 2f;
            float dustCenterY =
                dust.getY() + dust.getHeight() / 2f;

            float dx = dustCenterX - cleanerCenterX;
            float dy = dustCenterY - cleanerCenterY;

            float distance = (float) Math.sqrt(
                dx * dx + dy * dy
            );

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestDust = dust;
            }
        }

        if (nearestDust != null) {
            // Move toward the nearest dust pile.
            float targetX =
                nearestDust.getX() + nearestDust.getWidth() / 2f;
            float targetY =
                nearestDust.getY() + nearestDust.getHeight() / 2f;

            float dx = targetX - cleanerCenterX;
            float dy = targetY - cleanerCenterY;

            float distance = (float) Math.sqrt(
                dx * dx + dy * dy
            );

            if (distance > 1f) {
                sprite.translateX(
                    dx / distance * speed * delta
                );
                sprite.translateY(
                    dy / distance * speed * delta
                );
            }

            // Clean the dust when the sprites overlap.
            if (overlaps(nearestDust)) {
                nearestDust.clean();
            }

            wanderTimer = 0f;

        } else {
            // No dust nearby: wander around automatically.
            wanderTimer -= delta;

            if (wanderTimer <= 0f) {
                wanderAngle = MathUtils.random(0f, 360f);
                wanderTimer = 2f;
            }

            float radians = wanderAngle * MathUtils.degreesToRadians;

            sprite.translateX(
                MathUtils.cos(radians) * speed * delta
            );
            sprite.translateY(
                MathUtils.sin(radians) * speed * delta
            );
        }

        // Keep the vacuum inside the screen.
        float maxX = Gdx.graphics.getWidth() - sprite.getWidth();
        float maxY = Gdx.graphics.getHeight() - sprite.getHeight();

        sprite.setX(MathUtils.clamp(sprite.getX(), 0f, maxX));
        sprite.setY(MathUtils.clamp(sprite.getY(), 0f, maxY));
    }

    private boolean overlaps(Dust dust) {
        return sprite.getX() < dust.getX() + dust.getWidth()
            && sprite.getX() + sprite.getWidth() > dust.getX()
            && sprite.getY() < dust.getY() + dust.getHeight()
            && sprite.getY() + sprite.getHeight() > dust.getY();
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public boolean isBroken() {
        return broken;
    }

    public float getBreakdownTimeRemaining() {
        return breakdownTimer;
    }

    public void dispose() {
        texture.dispose();
    }
}
