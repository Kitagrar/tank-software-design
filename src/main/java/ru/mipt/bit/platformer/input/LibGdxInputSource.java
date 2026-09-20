package ru.mipt.bit.platformer.input;

import com.badlogic.gdx.Gdx;
import ru.mipt.bit.platformer.model.Direction;

import static com.badlogic.gdx.Input.Keys.A;
import static com.badlogic.gdx.Input.Keys.D;
import static com.badlogic.gdx.Input.Keys.DOWN;
import static com.badlogic.gdx.Input.Keys.LEFT;
import static com.badlogic.gdx.Input.Keys.RIGHT;
import static com.badlogic.gdx.Input.Keys.S;
import static com.badlogic.gdx.Input.Keys.UP;
import static com.badlogic.gdx.Input.Keys.W;

public class LibGdxInputSource implements InputSource {
    @Override
    public boolean isPressed(Direction direction) {
        switch (direction) {
            case UP:
                return isPressed(UP, W);
            case RIGHT:
                return isPressed(RIGHT, D);
            case DOWN:
                return isPressed(DOWN, S);
            case LEFT:
                return isPressed(LEFT, A);
            default:
                throw new IllegalStateException("Unknown direction: " + direction);
        }
    }

    private boolean isPressed(int firstKey, int secondKey) {
        return Gdx.input.isKeyPressed(firstKey) || Gdx.input.isKeyPressed(secondKey);
    }
}
