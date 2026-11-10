package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.util.TileMovement;

import java.util.Objects;

/** Owns both the bar texture and the wrapped renderer. */
public final class HealthBarRendererDecorator implements EntityRenderer<Tank> {
    private static final float BAR_WIDTH = 64f;
    private static final float BAR_HEIGHT = 6f;
    private static final float VERTICAL_OFFSET = 52f;

    private final EntityRenderer<Tank> delegate;
    private final Batch batch;
    private final TileMovement tileMovement;
    private final Texture texture;
    private final Rectangle rectangle = new Rectangle(0, 0, BAR_WIDTH, BAR_HEIGHT);

    public HealthBarRendererDecorator(EntityRenderer<Tank> delegate, Batch batch,
                                      TiledMapTileLayer tileLayer) {
        this.delegate = Objects.requireNonNull(delegate);
        this.batch = Objects.requireNonNull(batch);
        tileMovement = new TileMovement(tileLayer, Interpolation.smooth);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        try {
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            texture = new Texture(pixmap);
        } finally {
            pixmap.dispose();
        }
    }

    @Override
    public void render(Tank tank) {
        delegate.render(tank);
        tileMovement.moveRectangleBetweenTileCenters(rectangle, tank.getCoordinates(),
                tank.getDestinationCoordinates(), tank.getMovementProgress());
        float y = rectangle.y + VERTICAL_OFFSET;
        float fraction = MathUtils.clamp((float) tank.getHealth() / tank.getMaxHealth(), 0f, 1f);
        Color previous = batch.getColor();
        float r = previous.r;
        float g = previous.g;
        float b = previous.b;
        float a = previous.a;
        try {
            batch.setColor(Color.DARK_GRAY);
            batch.draw(texture, rectangle.x, y, BAR_WIDTH, BAR_HEIGHT);
            batch.setColor(Color.GREEN);
            batch.draw(texture, rectangle.x, y, BAR_WIDTH * fraction, BAR_HEIGHT);
        } finally {
            batch.setColor(r, g, b, a);
        }
    }

    @Override
    public void dispose() {
        texture.dispose();
        delegate.dispose();
    }
}
