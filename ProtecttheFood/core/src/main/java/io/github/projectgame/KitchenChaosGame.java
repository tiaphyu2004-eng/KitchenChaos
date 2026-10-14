package io.github.projectgame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KitchenChaosGame extends ApplicationAdapter {

    private OrthographicCamera camera;
    private Viewport viewport;
    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 600f;

    private SpriteBatch batch;
    private Texture kitchen;
    private BitmapFont font;
    private ShapeRenderer attackShape;

    private LiquidCat cat;
    private Sword sword;
    private boolean usingSword = false;

    private List<Rat> rats;
    private List<Cockroach> cockroaches;
    private List<Dust> dustPiles;

    private VaccumCleaner vaccumCleaner;
    private Food[] foods;

    private int foodCount = 0;

    // Three levels: 30, 45, and 60 seconds.
    private int currentLevel = 1;
    private float levelTimer = 20f;

    private boolean gameWon = false;
    private boolean gameLost = false;

    private float cockroachTimer = 0f;
    private float ratBiteTimer = 0f;
    private static final float RAT_BITE_COOLDOWN = 1.5f;

    private float dustSpawnTimer = 0f;
    private static final float DUST_SPAWN_INTERVAL = 5f;

    private String compliment = "";
    private float complimentTimer = 0f;

    private float ratSpawnTimer = 0f;
    private boolean ratsSpawned = false;

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(camera.combined);
        attackShape.setProjectionMatrix(camera.combined);
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();

        batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);

        attackShape = new ShapeRenderer();
        attackShape.setProjectionMatrix(camera.combined);
        kitchen = new Texture(
            Gdx.files.internal("assets/KitchenPTF.png")
        );

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.3f);

        cat = new LiquidCat();
        sword = new Sword();

        rats = new ArrayList<>();
        cockroaches = new ArrayList<>();
        dustPiles = new ArrayList<>();

        vaccumCleaner = new VaccumCleaner(
            Gdx.graphics.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f
        );

        resetFood();

        // Level 1 starts with one rat.
        spawnRat();
    }

    private void resetFood() {
        if (foods != null) {
            for (Food food : foods) {
                if (food != null) {
                    food.dispose();
                }
            }
        }

        foods = new Food[4];

        foods[0] = new Food("BreadPTF.png", 110, 110);
        foods[1] = new Food("BurgerPTF.png", 125, 130);
        foods[2] = new Food("CheesePTF.png", 150, 140);
        foods[3] = new Food("ChickenPTF.png", 145, 110);

        foodCount = 0;
    }

    private void spawnRat() {
        rats.add(new Rat(
            Gdx.graphics.getWidth() * 0.92f,
            MathUtils.random(
                50f,
                Math.max(50f, Gdx.graphics.getHeight() - 80f)
            )
        ));
    }

    private void spawnCockroach() {
        cockroaches.add(new Cockroach(
            Gdx.graphics.getWidth() * 0.75f,
            MathUtils.random(
                50f,
                Math.max(50f, Gdx.graphics.getHeight() - 80f)
            )
        ));
    }

    private Food findNearestFood(float enemyX, float enemyY) {
        Food nearest = null;
        float shortestDistance = Float.MAX_VALUE;

        if (foods == null) {
            return null;
        }

        for (Food food : foods) {
            if (food == null || food.isCollected()) {
                continue;
            }

            float foodX = food.getX() + food.getWidth() / 2f;
            float foodY = food.getY() + food.getHeight() / 2f;

            float dx = foodX - enemyX;
            float dy = foodY - enemyY;

            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearest = food;
            }
        }

        return nearest;
    }

    private void updateLevel(float delta) {
        levelTimer -= delta;

        if (levelTimer > 0f) {
            return;
        }

        if (currentLevel >= 3) {
            gameWon = true;
            return;
        }

        // Count dust piles that the vacuum did not clean.
        int leftoverDust = dustPiles.size();

        // Remove the old dust before the next level.
        for (Dust dust : dustPiles) {
            dust.dispose();
        }
        dustPiles.clear();

        // Each leftover dust pile adds two of each pest
        // to the next level.
        int bonusPests = leftoverDust * 2;

        currentLevel++;
        if (currentLevel == 2) {
            levelTimer = 30f;
        } else if (currentLevel == 3) {
            levelTimer = 45f;
        }

        resetFood();

        // Remove old enemies.
        for (Rat rat : rats) {
            rat.dispose();
        }
        rats.clear();

        for (Cockroach cockroach : cockroaches) {
            cockroach.dispose();
        }
        cockroaches.clear();

        // Each new level starts with one rat and one cockroach.
        int ratsToSpawn;
        int cockroachesToSpawn;

        if (currentLevel == 2) {
            ratsToSpawn = 3;
            cockroachesToSpawn = 3;
        } else {
            ratsToSpawn = 5;
            cockroachesToSpawn = 5;
        }

        for (int i = 0; i < ratsToSpawn; i++) {
            spawnRat();
        }

        for (int i = 0; i < cockroachesToSpawn; i++) {
            spawnCockroach();
        }

        // Apply the bonus from leftover dust.
        for (int i = 0; i < bonusPests; i++) {
            spawnRat();
            spawnCockroach();
        }

        ratBiteTimer = 0f;
        cockroachTimer = 0f;
        dustSpawnTimer = 0f;
    }

    @Override
    public void render() {

        viewport.apply();
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        attackShape.setProjectionMatrix(camera.combined);

        float delta = Gdx.graphics.getDeltaTime();

        if (!gameWon && !gameLost) {

            // Switch between Liquid Cat and Sword.
            if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                usingSword = !usingSword;
            }

            if (usingSword) {
                sword.update(delta);

                if (sword.consumeRechargeCompleted()) {
                    compliment = "You're amazing! Sword is ready!";
                    complimentTimer = 3.5f;
                }
            } else {
                cat.update(delta);
            }

            // Update the compliment message.
            if (complimentTimer > 0f) {
                complimentTimer -= delta;

                if (complimentTimer <= 0f) {
                    complimentTimer = 0f;
                    compliment = "";
                }
            }

            // Spawn the first cockroach after 10 seconds.
            if (currentLevel == 1 && cockroaches.isEmpty()) {
                cockroachTimer += delta;

                if (cockroachTimer >= 10f) {
                    spawnCockroach();
                }
            }

            // Update rats: chase food, steal food, and get attacked.
            for (Rat rat : rats) {
                if (!rat.isAlive()) {
                    continue;
                }

                Food target = findNearestFood(rat.getX(), rat.getY());
                rat.update(delta, target);

                if (target != null && target.checkCollection(
                    rat.getX(), rat.getY(),
                    rat.getWidth(), rat.getHeight())) {
                    foodCount++;
                }

                // Liquid Cat attacks rats.
                if (!usingSword && cat.canHitRat(rat)) {
                    rat.takeHit();
                    cat.markAttackHit();
                }

                // Rat bites the cat when close enough.
                ratBiteTimer -= delta;

                float catX = cat.getX() + cat.getWidth() / 2f;
                float catY = cat.getY() + cat.getHeight() / 2f;
                float ratX = rat.getX() + rat.getWidth() / 2f;
                float ratY = rat.getY() + rat.getHeight() / 2f;

                float dx = catX - ratX;
                float dy = catY - ratY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (currentLevel == 3
                    && distance < 55f
                    && ratBiteTimer <= 0f) {

                    cat.takeDamage();
                    ratBiteTimer = RAT_BITE_COOLDOWN;
                }
            }

            // Update cockroaches: chase food and get attacked.
            for (Cockroach cockroach : cockroaches) {
                if (!cockroach.isAlive()) {
                    continue;
                }

                Food target = findNearestFood(
                    cockroach.getX(), cockroach.getY()
                );

                cockroach.update(delta, target);

                if (target != null && target.checkCollection(
                    cockroach.getX(), cockroach.getY(),
                    cockroach.getWidth(),
                    cockroach.getHeight())) {
                    foodCount++;
                }

                // Sword attacks cockroaches in front of the player.
                if (usingSword && sword.isAttacking()
                    && !sword.hasAttackHit()) {

                    float swordX =
                        sword.getX() + sword.getWidth() / 2f;
                    float swordY =
                        sword.getY() + sword.getHeight() / 2f;

                    float roachX =
                        cockroach.getX() + cockroach.getWidth() / 2f;
                    float roachY =
                        cockroach.getY() + cockroach.getHeight() / 2f;

                    float dx = roachX - swordX;
                    float dy = roachY - swordY;

                    boolean inFront = sword.isFacingRight()
                        ? dx > 0f : dx < 0f;

                    if (inFront && Math.abs(dx) <= 65f
                        && Math.abs(dy) <= 35f) {
                        cockroach.takeHit();
                        sword.markAttackHit();
                    }
                }
            }

            // Dust begins spawning at Level 2.
            if (currentLevel >= 2) {
                dustSpawnTimer += delta;

                if (dustSpawnTimer >= DUST_SPAWN_INTERVAL) {
                    dustSpawnTimer -= DUST_SPAWN_INTERVAL;

                    float x = MathUtils.random(
                        0f, Math.max(
                            0f, Gdx.graphics.getWidth() - 30f
                        )
                    );

                    float y = MathUtils.random(
                        0f, Math.max(
                            0f, Gdx.graphics.getHeight() - 30f
                        )
                    );

                    dustPiles.add(new Dust(x, y));
                }
            }

            // The vacuum automatically cleans dust.
            vaccumCleaner.update(delta, dustPiles);

            Iterator<Dust> iterator = dustPiles.iterator();

            while (iterator.hasNext()) {
                Dust dust = iterator.next();

                if (dust.isCleaned()) {
                    dust.dispose();
                    iterator.remove();
                }
            }

            // Advance the level timer.
            updateLevel(delta);

            // Check losing conditions.
            if (foodCount >= foods.length || cat.getHealth() <= 0) {
                gameLost = true;
            }
        }

        // Clear the screen.
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the background and game objects.
        batch.begin();

        batch.draw(
            kitchen, 0, 0,
            WORLD_WIDTH,
            WORLD_HEIGHT
        );

        for (Food food : foods) {
            if (food != null) {
                food.draw(batch);
            }
        }

        for (Rat rat : rats) {
            if (rat.isAlive()) {
                rat.draw(batch);
            }
        }

        for (Cockroach cockroach : cockroaches) {
            if (cockroach.isAlive()) {
                cockroach.draw(batch);
            }
        }

        if (usingSword) {
            sword.draw(batch);
        } else {
            cat.draw(batch);
        }

        for (Dust dust : dustPiles) {
            dust.draw(batch);
        }

        vaccumCleaner.draw(batch);

        // HUD.
        font.setColor(Color.WHITE);

        font.draw(
            batch, "Level: " + currentLevel + " / 3",
            20, Gdx.graphics.getHeight() - 25
        );

        font.draw(
            batch,
            "Time: " + Math.max(0, (int) Math.ceil(levelTimer)) + "s",
            20, Gdx.graphics.getHeight() - 50
        );

        font.draw(
            batch,
            "Food remaining: " + Math.max(0, foods.length - foodCount),
            20, Gdx.graphics.getHeight() - 75
        );

        font.draw(
            batch, "Liquid Cat HP: " + cat.getHealth(),
            20, Gdx.graphics.getHeight() - 100
        );

        if (usingSword) {
            if (sword.isRecharging()) {
                font.setColor(Color.YELLOW);

                font.draw(
                    batch,
                    "Sword Recharging: "
                        + (int) Math.ceil(
                        sword.getRechargeTimeRemaining()
                    ) + "s",
                    20, Gdx.graphics.getHeight() - 130
                );
            } else {
                font.setColor(Color.GREEN);

                font.draw(
                    batch, "Sword Ready! Press SPACE",
                    20, Gdx.graphics.getHeight() - 130
                );
            }

            if (complimentTimer > 0f) {
                font.setColor(Color.PINK);

                font.draw(
                    batch, compliment,
                    20, Gdx.graphics.getHeight() - 155
                );
            }
        }

        // End screens.
        if (gameWon) {
            font.setColor(Color.GREEN);

            font.draw(
                batch, "YOU WIN! Kitchen protected!",
                Gdx.graphics.getWidth() / 2f - 130,
                Gdx.graphics.getHeight() / 2f
            );
        } else if (gameLost) {
            font.setColor(Color.RED);

            String message = cat.getHealth() <= 0
                ? "GAME OVER! Liquid Cat was defeated!"
                : "GAME OVER! All food was stolen!";

            font.draw(
                batch, message,
                Gdx.graphics.getWidth() / 2f - 155,
                Gdx.graphics.getHeight() / 2f
            );
        }

        batch.end();

        // Draw attack effects outside SpriteBatch.
        attackShape.setProjectionMatrix(batch.getProjectionMatrix());

        if (!gameWon && !gameLost) {
            if (!usingSword && cat.isAttacking()) {
                attackShape.begin(ShapeRenderer.ShapeType.Filled);
                attackShape.setColor(0.35f, 0.85f, 1f, 1f);

                float slashX = cat.isFacingRight()
                    ? cat.getX() + cat.getWidth()
                    : cat.getX() - 22f;

                attackShape.rect(
                    slashX, cat.getY() + 27f, 22f, 3f
                );

                attackShape.end();

            } else if (usingSword && sword.isAttacking()) {
                attackShape.begin(ShapeRenderer.ShapeType.Filled);
                attackShape.setColor(1f, 0.85f, 0.2f, 1f);

                float slashX = sword.isFacingRight()
                    ? sword.getX() + sword.getWidth()
                    : sword.getX() - 28f;

                attackShape.rect(
                    slashX, sword.getY() + 27f, 28f, 5f
                );

                attackShape.end();
            }
        }
    }

    @Override
    public void dispose() {
        if (kitchen != null) kitchen.dispose();
        if (cat != null) cat.dispose();
        if (sword != null) sword.dispose();
        if (attackShape != null) attackShape.dispose();
        if (font != null) font.dispose();
        if (vaccumCleaner != null) vaccumCleaner.dispose();

        if (rats != null) {
            for (Rat rat : rats) {
                rat.dispose();
            }
            rats.clear();
        }

        if (cockroaches != null) {
            for (Cockroach cockroach : cockroaches) {
                cockroach.dispose();
            }
            cockroaches.clear();
        }

        if (foods != null) {
            for (Food food : foods) {
                if (food != null) food.dispose();
            }
        }

        if (dustPiles != null) {
            for (Dust dust : dustPiles) {
                dust.dispose();
            }
            dustPiles.clear();
        }

        if (batch != null) batch.dispose();
    }
}
