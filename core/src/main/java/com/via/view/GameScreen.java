package com.via.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.via.model.Constants;
import com.via.model.GameState;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;

public class GameScreen implements Screen {
   private final OrthographicCamera camera;
   private final SpriteBatch batch;
   private final GameState gameState;
   private final WorldMapRenderer renderer;
   private static final float CAMERA_SPEED = 200f;
   public GameScreen(GameState gameState){
       this.gameState = gameState;
       this.camera = new OrthographicCamera();
       this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
       this.batch = new SpriteBatch();
       this.renderer = new WorldMapRenderer(batch, camera);

   }

   @Override
    public void render(float delta){
       ScreenUtils.clear(0.2f, 0.5f, 0.2f, 1f);
       handleInput(delta);
       camera.update();
       renderer.render(gameState);
   }

   private void handleInput(float delta){
       if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
           camera.translate(-CAMERA_SPEED * delta, 0);
       if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
           camera.translate(CAMERA_SPEED * delta, 0);
       if(Gdx.input.isKeyPressed(Input.Keys.UP))
           camera.translate(0, CAMERA_SPEED * delta);
       if(Gdx.input.isKeyPressed(Input.Keys.DOWN))
           camera.translate(0, -CAMERA_SPEED * delta);
       if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
           camera.translate(-Gdx.input.getDeltaX(), Gdx.input.getDeltaY());
       }
       if (Gdx.input.justTouched()) {
           Vector3 worldCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
           camera.unproject(worldCoords);
           int tileX = (int)(worldCoords.x / Constants.TILE_SIZE);
           int tileY = (int)(worldCoords.y / Constants.TILE_SIZE);
           TileCoord coord = new TileCoord(tileX, tileY);
           if (gameState.getMap().isWithinBounds(coord)) {
               Tile tile = gameState.getMap().getTile(coord);
               System.out.println("Tile: " + tileX + "," + tileY + " | Terrain: " + tile.getTerrain().getType() + " | Structure: " + tile.getStructure());
           }
       }
   }


    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
