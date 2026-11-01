package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Disposable;
import ru.mipt.bit.platformer.model.*;

import java.util.LinkedHashSet;
import java.util.Set;

/** Its visible membership is maintained exclusively by level lifecycle events. */
public final class GraphicalLevel implements LevelObserver, Disposable {
    private final Set<Tank> tanks = new LinkedHashSet<>();
    private final Set<Tree> trees = new LinkedHashSet<>();
    private final Set<Bullet> bullets = new LinkedHashSet<>();
    private final TankRenderer tankRenderer;
    private final TreeRenderer treeRenderer;
    private final BulletRenderer bulletRenderer;

    public GraphicalLevel(Batch batch, TiledMapTileLayer tileLayer) {
        tankRenderer = new TankRenderer(batch, tileLayer);
        treeRenderer = new TreeRenderer(batch, tileLayer);
        bulletRenderer = new BulletRenderer(batch, tileLayer);
    }

    @Override
    public void onObjectAdded(LevelEntity entity) {
        if (entity instanceof Tank tank) {
            tanks.add(tank);
        } else if (entity instanceof Tree tree) {
            trees.add(tree);
        } else if (entity instanceof Bullet bullet) {
            bullets.add(bullet);
        }
    }

    @Override
    public void onObjectRemoved(LevelEntity entity) {
        if (entity instanceof Tank tank) {
            tanks.remove(tank);
        } else if (entity instanceof Tree tree) {
            trees.remove(tree);
        } else if (entity instanceof Bullet bullet) {
            bullets.remove(bullet);
        }
    }

    public void render() {
        tanks.forEach(tankRenderer::render);
        trees.forEach(treeRenderer::render);
        bullets.forEach(bulletRenderer::render);
    }

    @Override
    public void dispose() {
        tankRenderer.dispose();
        treeRenderer.dispose();
        bulletRenderer.dispose();
        tanks.clear();
        trees.clear();
        bullets.clear();
    }
}
