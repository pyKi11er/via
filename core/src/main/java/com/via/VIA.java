package com.via;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.via.controller.MapInitializer;
import com.via.model.GameState;
import com.via.view.GameScreen;
import com.via.view.MainMenuScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class VIA extends Game {
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (batch != null) batch.dispose();
    }

    public void showMainMenu() {
        this.setScreen(new MainMenuScreen(this));
//        Gdx.app.exit();
    }
}
