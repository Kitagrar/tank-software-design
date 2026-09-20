package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import ru.mipt.bit.platformer.model.Tree;

import static ru.mipt.bit.platformer.util.GdxGameUtils.createBoundingRectangle;
import static ru.mipt.bit.platformer.util.GdxGameUtils.drawTextureRegionUnscaled;
import static ru.mipt.bit.platformer.util.GdxGameUtils.moveRectangleAtTileCenter;

public class TreeRenderer implements EntityRenderer<Tree> {
    private final Batch batch;
    private final TiledMapTileLayer tileLayer;
    private final Texture texture;
    private final TextureRegion graphics;
    private final Rectangle rectangle;

    public TreeRenderer(Batch batch, TiledMapTileLayer tileLayer) {
        this.batch = batch;
        this.tileLayer = tileLayer;
        this.texture = new Texture("images/greenTree.png");
        this.graphics = new TextureRegion(texture);
        this.rectangle = createBoundingRectangle(graphics);
    }

    @Override
    public void render(Tree tree) {
        moveRectangleAtTileCenter(tileLayer, rectangle, tree.getCoordinates());
        drawTextureRegionUnscaled(batch, graphics, rectangle, 0f);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
