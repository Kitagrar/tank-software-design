package ru.mipt.bit.platformer.graphics;

import com.badlogic.gdx.utils.Disposable;

public interface EntityRenderer<T> extends Disposable {
    void render(T entity);
}
