package io.github.projectgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Sword {
    private final Texture texture;
    private final Sprite sprite;

    private float x = 300;
    private float y = 150;
    private final float speed = 250;

    private boolean facingRight = true;
    private boolean attacking = false;
    private boolean attackHitApplied = false;
    private float attackTimer = 0;

    private static final float ATTACK_DURATION = 0.2f;

    // Recharge system
    private static final float RECHARGE_DURATION = 0f;
    private float rechargeTimer = 0;
    private boolean rechargeJustCompleted = false;

    public Sword() {
        texture = new Texture(
            Gdx.files.internal("assets/SwordPTF.png")
        );

        sprite = new Sprite(texture);
        sprite.setSize(55, 55);
        sprite.setPosition(x, y);
    }

    public void update(float delta) {
        float movement = speed * delta;

        // Update recharge timer
        if (rechargeTimer > 0) {
            rechargeTimer -= delta;

            if (rechargeTimer <= 0) {
                rechargeTimer = 0;
                rechargeJustCompleted = true;
            }
        }

        // Movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= movement;
            facingRight = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += movement;
            facingRight = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            y += movement;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            y -= movement;
        }

        // Keep Sword inside the screen
        x = Math.max(0, Math.min(
            x, Gdx.graphics.getWidth() - sprite.getWidth()
        ));

        y = Math.max(0, Math.min(
            y, Gdx.graphics.getHeight() - sprite.getHeight()
        ));

        // Attack only when ready
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
        {

            attacking = true;
            attackTimer = ATTACK_DURATION;
            attackHitApplied = false;

            // Start 15-second recharge
            rechargeTimer = RECHARGE_DURATION;
        }

        // Attack duration
        if (attacking) {
            attackTimer -= delta;

            if (attackTimer <= 0) {
                attacking = false;
            }
        }

        sprite.setPosition(x, y);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public boolean isAttacking() {
        return attacking;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void markAttackHit() {
        attackHitApplied = true;
    }

    public boolean hasAttackHit() {
        return attackHitApplied;
    }

    // Recharge methods
    public boolean isRecharging() {
        return rechargeTimer > 0;
    }

    public float getRechargeTimeRemaining() {
        return rechargeTimer;
    }

    public boolean consumeRechargeCompleted() {
        if (rechargeJustCompleted) {
            rechargeJustCompleted = false;
            return true;
        }

        return false;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return sprite.getWidth();
    }

    public float getHeight() {
        return sprite.getHeight();
    }

    public void dispose() {
        texture.dispose();
    }
}
