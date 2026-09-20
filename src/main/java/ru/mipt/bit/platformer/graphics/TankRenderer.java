package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.util.TileMovement;

import static ru.mipt.bit.platformer.util.GdxGameUtils.createBoundingRectangle;
import static ru.mipt.bit.platformer.util.GdxGameUtils.drawTextureRegionUnscaled;

public class TankRenderer implements EntityRenderer<Tank> {
    private final Batch batch;
    private final TileMovement tileMovement;
    private final Texture texture;
    private final TextureRegion graphics;
    private final Rectangle rectangle;

    public TankRenderer(Batch batch, TiledMapTileLayer tileLayer) {
        this.batch = batch;
        this.tileMovement = new TileMovement(tileLayer, com.badlogic.gdx.math.Interpolation.smooth);
        this.texture = new Texture("images/tank_blue.png");
        this.graphics = new TextureRegion(texture);
        this.rectangle = createBoundingRectangle(graphics);
    }

    @Override
    public void render(Tank tank) {
        tileMovement.moveRectangleBetweenTileCenters(
                rectangle,
                tank.getCoordinates(),
                tank.getDestinationCoordinates(),
                tank.getMovementProgress());
        drawTextureRegionUnscaled(batch, graphics, rectangle, tank.getRotation());
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
