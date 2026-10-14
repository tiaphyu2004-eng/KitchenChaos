package io.github.projectgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Food {

    private final Texture texture;
    private final Sprite sprite;
    private boolean collected = false;

    public Food(String fileName, float x, float y) {
        texture = new Texture(
            Gdx.files.internal("assets/" + fileName)
        );

        sprite = new Sprite(texture);
        sprite.setSize(35, 35);
        sprite.setPosition(x, y);
    }

    public void draw(SpriteBatch batch) {
        if (!collected) {
            sprite.draw(batch);
        }
    }

    // Check whether a character or enemy touches the food
    public boolean checkCollection(
        float characterX,
        float characterY,
        float characterWidth,
        float characterHeight
    ) {
        if (collected) {
            return false;
        }

        boolean touching =
            characterX < sprite.getX() + sprite.getWidth()
                && characterX + characterWidth > sprite.getX()
                && characterY < sprite.getY() + sprite.getHeight()
                && characterY + characterHeight > sprite.getY();

        if (touching) {
            collected = true;
            return true;
        }

        return false;
    }

    public boolean isCollected() {
        return collected;
    }

    // Get food position and size
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
