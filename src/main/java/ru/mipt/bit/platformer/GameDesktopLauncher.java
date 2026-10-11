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
import ru.mipt.bit.platformer.input.InputHandler;
import ru.mipt.bit.platformer.input.LibGdxInputSource;
import ru.mipt.bit.platformer.level.FileLevelCreationStrategy;
import ru.mipt.bit.platformer.level.LevelCreationStrategy;
import ru.mipt.bit.platformer.level.LevelValidator;
import ru.mipt.bit.platformer.model.GameLevel;
import ru.mipt.bit.platformer.model.Tree;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

public class GameDesktopLauncher implements ApplicationListener {

    private final LevelCreationStrategy levelStrategy;
    private Batch batch;

    private TiledMap level;
    private MapRenderer levelRenderer;
    private GameLevel gameLevel;
    private InputHandler inputHandler;
    private TankRenderer tankRenderer;
    private TreeRenderer treeRenderer;

    public GameDesktopLauncher(LevelCreationStrategy levelStrategy) {
        this.levelStrategy = java.util.Objects.requireNonNull(levelStrategy);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();

        // load level tiles
        level = new TmxMapLoader().load("level.tmx");
        levelRenderer = createSingleLayerMapRenderer(level, batch);
        TiledMapTileLayer groundLayer = getSingleLayer(level);
        gameLevel = levelStrategy.create(groundLayer.getWidth(), groundLayer.getHeight());
        inputHandler = new InputHandler(new LibGdxInputSource(), gameLevel.getTank(), gameLevel.getCollisionObstacle());
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

        inputHandler.update();
        gameLevel.getTank().updateMovement(deltaTime);

        // render each tile of the level
        levelRenderer.render();

        // start recording all drawing commands
        batch.begin();

        tankRenderer.render(gameLevel.getTank());
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
        new Lwjgl3Application(new GameDesktopLauncher(strategy), config);
    }
}
