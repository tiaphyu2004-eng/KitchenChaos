
package io.github.projectgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Rat {

    private final Texture texture;
    private final Sprite sprite;

    private float x;
    private float y;
    private final float speed = 35f;

    private int health = 2;
    private boolean alive = true;

    public Rat(float startX, float startY) {
        texture = new Texture(
            Gdx.files.internal("assets/RatPTF.png")
        );

        sprite = new Sprite(texture);
        sprite.setSize(40, 40);

        x = startX;
        y = startY;
        sprite.setPosition(x, y);
    }

    // Move toward a food target
    public void update(float delta, Food targetFood) {
        if (!alive || targetFood == null || targetFood.isCollected()) {
            return;
        }

        float targetX = targetFood.getX() + targetFood.getWidth() / 2f;
        float targetY = targetFood.getY() + targetFood.getHeight() / 2f;

        float ratCenterX = x + sprite.getWidth() / 2f;
        float ratCenterY = y + sprite.getHeight() / 2f;

        float dx = targetX - ratCenterX;
        float dy = targetY - ratCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 1f) {
            x += (dx / distance) * speed * delta;
            y += (dy / distance) * speed * delta;
        }

        sprite.setPosition(x, y);
    }

    public void draw(SpriteBatch batch) {
        if (alive) {
            sprite.draw(batch);
        }
    }

    public void takeHit() {
        if (!alive) {
            return;
        }

        health--;

        if (health <= 0) {
            alive = false;
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public int getHealth() {
        return health;
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
