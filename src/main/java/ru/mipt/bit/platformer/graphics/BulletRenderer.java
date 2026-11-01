package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import ru.mipt.bit.platformer.model.Bullet;

import static ru.mipt.bit.platformer.util.GdxGameUtils.moveRectangleAtTileCenter;

public final class BulletRenderer implements EntityRenderer<Bullet> {
    private static final int SIZE = 12;
    private final Batch batch;
    private final TiledMapTileLayer tileLayer;
    private final Texture texture;
    private final Rectangle rectangle = new Rectangle(0, 0, SIZE, SIZE);

    public BulletRenderer(Batch batch, TiledMapTileLayer tileLayer) {
        this.batch = batch;
        this.tileLayer = tileLayer;
        Pixmap pixmap = new Pixmap(SIZE, SIZE, Pixmap.Format.RGBA8888);
        try {
            pixmap.setColor(Color.YELLOW);
            pixmap.fill();
            texture = new Texture(pixmap);
        } finally {
            pixmap.dispose();
        }
    }

    @Override
    public void render(Bullet bullet) {
        moveRectangleAtTileCenter(tileLayer, rectangle, bullet.getCoordinates());
        batch.draw(texture, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
