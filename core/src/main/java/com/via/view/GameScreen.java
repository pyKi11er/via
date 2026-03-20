package com.via.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.via.VIA;
import com.via.controller.GameEngine;
import com.via.model.Constants;
import com.via.model.GameState;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;

public class GameScreen implements Screen {

    private final VIA game;
    private final GameState gameState;
    private final GameEngine gameEngine;
    private final OrthographicCamera mapCamera;
    private final OrthographicCamera hudCamera;
    private final SpriteBatch batch;
    private final WorldMapRenderer renderer;
    private final ShapeRenderer sr;
    private final BitmapFont font;
    private final GlyphLayout gl;
    private final Texture texButton;
    private final Texture texPanel;

    private static final float CAMERA_SPEED = 200f;
    private static final float TOP_BAR_H    = 36f;
    private static final float BOT_BAR_H    = 48f;

    // Map dimensions in pixels
    private static final float MAP_PX_W = Constants.MAP_WIDTH  * Constants.TILE_SIZE;
    private static final float MAP_PX_H = Constants.MAP_HEIGHT * Constants.TILE_SIZE;

    // HUD
    private Rectangle btnExit;

    // Exit dialog
    private boolean showExitDialog = false;
    private Rectangle rDialog;
    private Rectangle rBtnMainMenu;
    private Rectangle rBtnExitGame;
    private Rectangle rBtnClose;

    // Speed controls
    private final Rectangle[] speedBtns  = new Rectangle[4];
    private final String[]     speedLabels = {"||", ">", ">>", ">>>"};
    private int activeSpeed = 1;

    // Tabs
    private final Rectangle[] tabBtns   = new Rectangle[4];
    private final String[]     tabLabels = {"Build", "Roads", "Buy", "..."};
    private int activeTab = -1;

    // Popup
    private boolean   showPopup   = false;
    private Rectangle rPopup;
    private Rectangle rPopupClose;

    // Constructor

    public GameScreen(VIA game, GameState gameState) {
        this.game      = game;
        this.gameState = gameState;
        this.gameEngine = new GameEngine(gameState);

        // Map camera — starts centred on the map
        mapCamera = new OrthographicCamera();
        mapCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Move camera so it looks at the centre of the map instead of 0,0
        mapCamera.position.set(MAP_PX_W / 2f, MAP_PX_H / 2f, 0);
        mapCamera.update();

        // HUD camera — fixed, always looks at screen 0,0
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch    = new SpriteBatch();
        renderer = new WorldMapRenderer(batch, mapCamera);
        sr       = new ShapeRenderer();
        font     = new BitmapFont();
        font.getData().setScale(1.4f);
        gl = new GlyphLayout();

        texButton = new Texture(Gdx.files.internal("button.png"));
        texPanel  = new Texture(Gdx.files.internal("panel.png"));
        texButton.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texPanel .setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        buildLayout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    // Layout helper

    private void buildLayout(int w, int h) {
        hudCamera.setToOrtho(false, w, h);
        hudCamera.update();

        // Exit button — top-left
        btnExit = new Rectangle(8f, h - TOP_BAR_H + 3f, 80f, TOP_BAR_H - 6f);

        // Exit dialog — centred
        float dW = 420f, dH = 230f;
        float dX = w / 2f - dW / 2f;
        float dY = h / 2f - dH / 2f;
        rDialog = new Rectangle(dX, dY, dW, dH);

        float bW = 110f, bH = 46f;
        float bY = dY + 20f;
        float spacing = (dW - bW * 3f) / 4f;
        rBtnMainMenu = new Rectangle(dX + spacing,              bY, bW, bH);
        rBtnExitGame = new Rectangle(dX + spacing * 2f + bW,    bY, bW, bH);
        rBtnClose    = new Rectangle(dX + spacing * 3f + bW*2f, bY, bW, bH);

        // Speed buttons — bottom-right
        float btnW = 40f, btnH = 30f, gap = 4f;
        float startX = w - (btnW + gap) * 4 - 8f;
        for (int i = 0; i < 4; i++) {
            speedBtns[i] = new Rectangle(startX + i * (btnW + gap), 8f, btnW, btnH);
        }

        // Tab buttons — bottom-left
        float tabW = 200f, tabH = BOT_BAR_H - 10f;
        for (int i = 0; i < 4; i++) {
            tabBtns[i] = new Rectangle(8f + i * (tabW + 6f), 5f, tabW, tabH);
        }

        // Popup
        rPopup = new Rectangle(8f, BOT_BAR_H + 5f, 200f, 150f);
        rPopupClose = new Rectangle(
            rPopup.x + rPopup.width  - 24f,
            rPopup.y + rPopup.height - 24f,
            20f, 20f);
    }

    // Screen lifecycle

    @Override public void show() {}

    @Override
    public void resize(int width, int height) {
        mapCamera.setToOrtho(false, width, height);
        mapCamera.position.set(MAP_PX_W / 2f, MAP_PX_H / 2f, 0);
        mapCamera.update();
        buildLayout(width, height);
    }

    @Override
    public void render(float delta) {
        // 1. Advance simulation
        gameEngine.update();

        // 2. Clear screen
        ScreenUtils.clear(0.2f, 0.5f, 0.2f, 1f);

        // 3. Map layer
        if (!showExitDialog) handleMapInput(delta);
        mapCamera.update();
        batch.setProjectionMatrix(mapCamera.combined);
        renderer.render(gameState);

        // 4. HUD layer
        batch.setProjectionMatrix(hudCamera.combined);
        sr.setProjectionMatrix(hudCamera.combined);
        drawTopBar();
        drawBotBar();
        drawMinimap();

        if (showExitDialog) drawExitDialog();
        if (showPopup)      drawPopup();
    }


    // Input


    private void handleMapInput(float delta) {
        // Keyboard scroll
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  mapCamera.translate(-CAMERA_SPEED * delta, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) mapCamera.translate( CAMERA_SPEED * delta, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    mapCamera.translate(0,  CAMERA_SPEED * delta);
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  mapCamera.translate(0, -CAMERA_SPEED * delta);

        // Mouse drag scroll (right button so it doesn't conflict with click)
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            mapCamera.translate(-Gdx.input.getDeltaX(), Gdx.input.getDeltaY());
        }

        if (!Gdx.input.justTouched()) return;

        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY(); // flip Y

        // HUD buttons take priority
        if (btnExit.contains(mx, my)) { showExitDialog = true; return; }

        for (int i = 0; i < 4; i++) {
            if (speedBtns[i].contains(mx, my)) {
                activeSpeed = i;
                applySpeed(i);
                return;
            }
        }

        for (int i = 0; i < 4; i++) {
            if (tabBtns[i].contains(mx, my)) {
                if (activeTab == i && showPopup) {
                    showPopup = false;
                    activeTab = -1;
                } else {
                    activeTab = i;
                    showPopup = true;
                }
                return;
            }
        }

        // Tile click — unproject to world
        Vector3 world = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        mapCamera.unproject(world);
        int tileX = (int)(world.x / Constants.TILE_SIZE);
        int tileY = (int)(world.y / Constants.TILE_SIZE);
        TileCoord coord = new TileCoord(tileX, tileY);
        if (gameState.getMap().isWithinBounds(coord)) {
            Tile tile = gameState.getMap().getTile(coord);
            if (tile != null) {
                System.out.println("Tile [" + tileX + "," + tileY + "]"
                    + "  terrain=" + tile.getTerrain().getType()
                    + "  structure=" + (tile.getStructure() == null ? "null"
                    : tile.getStructure().getClass().getSimpleName())
                    + "  overlay=" + (tile.getOverlay() == null ? "null"
                    : tile.getOverlay().getClass().getSimpleName()));
            }
        }
    }

    private void applySpeed(int index) {
        switch (index) {
            case 0 -> gameEngine.getTimeController().setSpeed(com.via.model.enums.SpeedSetting.PAUSED);
            case 1 -> gameEngine.getTimeController().setSpeed(com.via.model.enums.SpeedSetting.NORMAL);
            case 2 -> gameEngine.getTimeController().setSpeed(com.via.model.enums.SpeedSetting.FAST);
            case 3 -> gameEngine.getTimeController().setSpeed(com.via.model.enums.SpeedSetting.VERY_FAST);
        }
    }

    private void handleDialogInput() {
        if (!Gdx.input.justTouched()) return;
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        if (rBtnMainMenu.contains(mx, my)) { showExitDialog = false; game.showMainMenu(); }
        if (rBtnExitGame.contains(mx, my)) { Gdx.app.exit(); }
        if (rBtnClose   .contains(mx, my)) { showExitDialog = false; }
    }


    // Drawing

    private void drawTopBar() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.08f, 0.11f, 0.18f, 1f);
        sr.rect(0, h - TOP_BAR_H, w, TOP_BAR_H);
        sr.setColor(0.20f, 0.47f, 0.80f, 1f);
        sr.rect(0, h - TOP_BAR_H, w, 2f);
        sr.end();

        drawBtn(btnExit, "Exit", new Color(0.70f, 0.18f, 0.18f, 1f));

        long capital = gameState.getEconomy().getCapital();
        String balStr = String.format("$%,d", capital);
        gl.setText(font, balStr);
        batch.begin();
        font.setColor(capital >= 0
            ? new Color(0.30f, 0.90f, 0.40f, 1f)
            : new Color(1f, 0.25f, 0.25f, 1f));
        font.draw(batch, balStr, w - gl.width - 12f, h - TOP_BAR_H / 2f + gl.height / 2f);
        batch.end();
    }

    private void drawBotBar() {
        float w = Gdx.graphics.getWidth();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.08f, 0.11f, 0.18f, 1f);
        sr.rect(0, 0, w, BOT_BAR_H);
        sr.setColor(0.20f, 0.47f, 0.80f, 1f);
        sr.rect(0, BOT_BAR_H, w, 2f);
        sr.end();

        batch.begin();
        for (int i = 0; i < 4; i++) {
            batch.setColor(i == activeSpeed ? Color.YELLOW : Color.WHITE);
            batch.draw(texButton,
                speedBtns[i].x, speedBtns[i].y,
                speedBtns[i].width, speedBtns[i].height);
            gl.setText(font, speedLabels[i]);
            font.setColor(Color.WHITE);
            font.draw(batch, speedLabels[i],
                speedBtns[i].x + (speedBtns[i].width  - gl.width)  / 2f,
                speedBtns[i].y + (speedBtns[i].height + gl.height) / 2f);
        }
        batch.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 4; i++) {
            sr.setColor(i == activeTab
                ? new Color(0.20f, 0.47f, 0.80f, 1f)
                : new Color(0.18f, 0.22f, 0.32f, 1f));
            sr.rect(tabBtns[i].x, tabBtns[i].y, tabBtns[i].width, tabBtns[i].height);
        }
        sr.end();

        batch.begin();
        for (int i = 0; i < 4; i++) {
            gl.setText(font, tabLabels[i]);
            font.setColor(Color.WHITE);
            font.draw(batch, tabLabels[i],
                tabBtns[i].x + (tabBtns[i].width  - gl.width)  / 2f,
                tabBtns[i].y + (tabBtns[i].height + gl.height) / 2f);
        }
        batch.end();
    }

    private void drawMinimap() {
        float w = Gdx.graphics.getWidth();
        float size = 320f;
        float x = w - size - 10f;
        float y = BOT_BAR_H + 10f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.07f, 0.10f, 0.17f, 1f);
        sr.rect(x, y, size, size);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.20f, 0.47f, 0.80f, 1f);
        sr.rect(x, y, size, size);
        sr.end();

        batch.begin();
        font.setColor(Color.WHITE);
        gl.setText(font, "Minimap");
        font.draw(batch, "Minimap",
            x + (size - gl.width)  / 2f,
            y + (size + gl.height) / 2f);
        batch.end();
    }

    private void drawPopup() {
        rPopup = new Rectangle(tabBtns[activeTab].x, BOT_BAR_H + 5f, 200f, 150f);
        rPopupClose = new Rectangle(
            rPopup.x + rPopup.width  - 24f,
            rPopup.y + rPopup.height - 24f,
            20f, 20f);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.07f, 0.10f, 0.17f, 1f);
        sr.rect(rPopup.x, rPopup.y, rPopup.width, rPopup.height);
        sr.setColor(0.20f, 0.47f, 0.80f, 1f);
        sr.rect(rPopup.x, rPopup.y + rPopup.height - 3f, rPopup.width, 3f);
        sr.setColor(0.60f, 0.15f, 0.15f, 1f);
        sr.rect(rPopupClose.x, rPopupClose.y, rPopupClose.width, rPopupClose.height);
        sr.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, tabLabels[activeTab], rPopup.x + 8f, rPopup.y + rPopup.height - 8f);
        gl.setText(font, "X");
        font.draw(batch, "X",
            rPopupClose.x + (rPopupClose.width  - gl.width)  / 2f,
            rPopupClose.y + (rPopupClose.height + gl.height) / 2f);
        batch.end();

        if (Gdx.input.justTouched()) {
            float mx = Gdx.input.getX();
            float my = Gdx.graphics.getHeight() - Gdx.input.getY();
            if (rPopupClose.contains(mx, my)) { showPopup = false; activeTab = -1; }
        }
    }

    private void drawExitDialog() {
        handleDialogInput();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.68f);
        sr.rect(0, 0, w, h);
        sr.setColor(0.07f, 0.10f, 0.17f, 0.97f);
        sr.rect(rDialog.x, rDialog.y, rDialog.width, rDialog.height);
        sr.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        batch.setColor(1f, 1f, 1f, 0.25f);
        batch.draw(texPanel, rDialog.x, rDialog.y, rDialog.width, rDialog.height);
        batch.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.20f, 0.47f, 0.80f, 1f);
        sr.rect(rDialog.x, rDialog.y + rDialog.height - 4f, rDialog.width, 4f);
        sr.end();

        float cx = rDialog.x + rDialog.width / 2f;
        drawCentred("Exit", cx, rDialog.y + rDialog.height - 16f, Color.WHITE);
        drawCentred("What do you want to do?",
            cx, rDialog.y + rDialog.height * 0.65f,
            new Color(0.80f, 0.85f, 0.95f, 1f));

        drawBtn(rBtnMainMenu, "Main Menu", new Color(0.20f, 0.47f, 0.80f, 1f));
        drawBtn(rBtnExitGame, "Exit Game", new Color(0.72f, 0.18f, 0.18f, 1f));
        drawBtn(rBtnClose,    "Close",     new Color(0.35f, 0.38f, 0.48f, 1f));
    }

    // ── Helpers

    private void drawBtn(Rectangle r, String label, Color tint) {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        float br = r.contains(mx, my) ? 1.28f : 1f;

        batch.begin();
        batch.setColor(
            Math.min(tint.r * br, 1f),
            Math.min(tint.g * br, 1f),
            Math.min(tint.b * br, 1f), 1f);
        batch.draw(texButton, r.x, r.y, r.width, r.height);
        gl.setText(font, label);
        float tx = r.x + (r.width  - gl.width)  / 2f;
        float ty = r.y + (r.height + gl.height) / 2f;
        font.setColor(0f, 0f, 0f, 0.85f); font.draw(batch, label, tx + 2, ty - 2);
        font.setColor(Color.WHITE);        font.draw(batch, label, tx,     ty);
        batch.end();
    }

    private void drawCentred(String text, float cx, float y, Color color) {
        gl.setText(font, text);
        float x = cx - gl.width / 2f;
        batch.begin();
        font.setColor(0f, 0f, 0f, 0.85f); font.draw(batch, text, x + 2, y - 2);
        font.setColor(color);              font.draw(batch, text, x,     y);
        batch.end();
    }


    // Dispose


    @Override
    public void dispose() {
        batch.dispose();
        sr.dispose();
        font.dispose();
        texButton.dispose();
        texPanel.dispose();
        renderer.dispose();
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
}
