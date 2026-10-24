package ru.mipt.bit.platformer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import ru.mipt.bit.platformer.graphics.TankRenderer;
import ru.mipt.bit.platformer.graphics.TreeRenderer;
import ru.mipt.bit.platformer.control.KeyboardControlStrategy;
import ru.mipt.bit.platformer.control.RandomAiControlStrategy;
import ru.mipt.bit.platformer.control.TankController;
import ru.mipt.bit.platformer.input.LibGdxInputSource;
import ru.mipt.bit.platformer.level.FileLevelCreationStrategy;
import ru.mipt.bit.platformer.level.LevelCreationStrategy;
import ru.mipt.bit.platformer.level.LevelValidator;
import ru.mipt.bit.platformer.level.RandomTankGenerator;
import ru.mipt.bit.platformer.model.GameLevel;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

public class GameDesktopLauncher implements ApplicationListener {

    private final LevelCreationStrategy levelStrategy;
    private final int aiTankCount;
    private final RandomTankGenerator tankGenerator;
    private final Random aiSeeds;
    private Batch batch;

    private TiledMap level;
    private MapRenderer levelRenderer;
    private GameLevel gameLevel;
    private TankController playerController;
    private final List<TankController> aiControllers = new ArrayList<>();
    private TankRenderer tankRenderer;
    private TreeRenderer treeRenderer;

    public GameDesktopLauncher(LevelCreationStrategy levelStrategy, int aiTankCount,
                               RandomTankGenerator tankGenerator, Random aiSeeds) {
        if (aiTankCount < 0) {
            throw new IllegalArgumentException("AI tank count must be non-negative");
        }
        this.levelStrategy = Objects.requireNonNull(levelStrategy);
        this.aiTankCount = aiTankCount;
        this.tankGenerator = Objects.requireNonNull(tankGenerator);
        this.aiSeeds = Objects.requireNonNull(aiSeeds);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();

        // load level tiles
        level = new TmxMapLoader().load("level.tmx");
        levelRenderer = createSingleLayerMapRenderer(level, batch);
        TiledMapTileLayer groundLayer = getSingleLayer(level);
        gameLevel = levelStrategy.create(groundLayer.getWidth(), groundLayer.getHeight());
        gameLevel = gameLevel.withAdditionalTanks(tankGenerator.generatePositions(gameLevel, aiTankCount));
        playerController = new TankController(gameLevel.getPlayerTank(),
                new KeyboardControlStrategy(new LibGdxInputSource()), gameLevel.getCollisionObstacle());
        aiControllers.clear();
        for (Tank tank : gameLevel.getTanks()) {
            if (tank != gameLevel.getPlayerTank()) {
                aiControllers.add(new TankController(tank,
                        new RandomAiControlStrategy(new Random(aiSeeds.nextLong())), gameLevel.getCollisionObstacle()));
            }
        }
        tankRenderer = new TankRenderer(batch, groundLayer);
        treeRenderer = new TreeRenderer(batch, groundLayer);
    }

    @Override
    public void render() {
        // clear the screen
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);

        // get time passed since the last render
        float deltaTime = Gdx.graphics.getDeltaTime();

        playerController.update();
        for (TankController controller : aiControllers) {
            controller.update();
        }
        for (Tank tank : gameLevel.getTanks()) {
            tank.updateMovement(deltaTime);
        }

        // render each tile of the level
        levelRenderer.render();

        // start recording all drawing commands
        batch.begin();

        for (Tank tank : gameLevel.getTanks()) {
            tankRenderer.render(tank);
        }
        for (Tree tree : gameLevel.getTrees()) {
            treeRenderer.render(tree);
        }

        // submit all drawing requests
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // do not react to window resizing
    }

    @Override
    public void pause() {
        // game doesn't get paused
    }

    @Override
    public void resume() {
        // game doesn't get paused
    }

    @Override
    public void dispose() {
        // dispose of all the native resources (classes which implement com.badlogic.gdx.utils.Disposable)
        treeRenderer.dispose();
        tankRenderer.dispose();
        level.dispose();
        batch.dispose();
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        // level width: 10 tiles x 128px, height: 8 tiles x 128px
        config.setWindowedMode(1280, 1024);
        LevelCreationStrategy strategy = new FileLevelCreationStrategy("levels/level1.txt", new LevelValidator());
        int aiTankCount = 3;
        new Lwjgl3Application(new GameDesktopLauncher(strategy, aiTankCount,
                new RandomTankGenerator(new Random(12345)), new Random(67890)), config);
    }
}
