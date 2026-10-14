package io.github.projectgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class LiquidCat {

    private final Texture texture;
    private final Sprite sprite;

    private float speed = 250f;

    private boolean attacking = false;
    private boolean attackHitApplied = false;
    private float attackTimer = 0f;
    private final float attackDuration = 0.2f;

    private boolean facingRight = true;

    private int health = 5;

    public LiquidCat() {
        texture = new Texture(
            Gdx.files.internal("assets/liquidCat.png")
        );

        sprite = new Sprite(texture);
        sprite.setSize(55, 55);
        sprite.setPosition(200, 150);
    }

    public void update(float delta) {

        // Move with arrow keys
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            sprite.translateX(-speed * delta);
            facingRight = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            sprite.translateX(speed * delta);
            facingRight = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            sprite.translateY(speed * delta);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            sprite.translateY(-speed * delta);
        }

        // Keep cat inside the screen
        float maxX = Gdx.graphics.getWidth() - sprite.getWidth();
        float maxY = Gdx.graphics.getHeight() - sprite.getHeight();

        sprite.setX(Math.max(0, Math.min(sprite.getX(), maxX)));
        sprite.setY(Math.max(0, Math.min(sprite.getY(), maxY)));

        // Start an attack with SPACE
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            && !attacking) {
            attacking = true;
            attackHitApplied = false;
            attackTimer = attackDuration;
        }

        // End the attack after a short time
        if (attacking) {
            attackTimer -= delta;

            if (attackTimer <= 0) {
                attacking = false;
            }
        }
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    // Check whether the rat is within the cat's attack range
    public boolean canHitRat(Rat rat) {

        if (!attacking || attackHitApplied || !rat.isAlive()) {
            return false;
        }

        float attackRange = 65f;

        float catCenterX = sprite.getX() + sprite.getWidth() / 2f;
        float catCenterY = sprite.getY() + sprite.getHeight() / 2f;

        float ratCenterX = rat.getX() + rat.getWidth() / 2f;
        float ratCenterY = rat.getY() + rat.getHeight() / 2f;

        float dx = ratCenterX - catCenterX;
        float dy = ratCenterY - catCenterY;

        // Only attack in the direction the cat is facing
        if (facingRight && dx < 0) return false;
        if (!facingRight && dx > 0) return false;

        return Math.abs(dx) < attackRange
            && Math.abs(dy) < 45f;
    }

    public void markAttackHit() {
        attackHitApplied = true;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    // Rat damage
    public void takeDamage() {
        if (health > 0) {
            health--;
        }
    }

    public int getHealth() {
        return health;
    }

    // Position and size getters
    public float getX() {
        return sprite.getX();
    }

    public float getY() {
        return sprite.getY();
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
