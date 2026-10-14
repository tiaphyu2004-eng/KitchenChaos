package io.github.projectgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Dust {

    private final Texture texture;
    private final Sprite sprite;
    private boolean cleaned = false;

    public Dust(float x, float y) {
        texture = new Texture(
            Gdx.files.internal("assets/DustPTF.png")
        );

        sprite = new Sprite(texture);
        sprite.setSize(30, 30);
        sprite.setPosition(x, y);
    }

    public void draw(SpriteBatch batch) {
        if (!cleaned) {
            sprite.draw(batch);
        }
    }

    public void clean() {
        cleaned = true;
    }

    public boolean isCleaned() {
        return cleaned;
    }

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
