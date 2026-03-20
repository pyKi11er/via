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
        GameState gameState = MapInitializer.createNewGame();
        this.setScreen(new GameScreen(this, gameState));
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
        // setScreen(new MainMenuScreen(this));
        // For now just exit cleanly:
        Gdx.app.exit();
    }
}
