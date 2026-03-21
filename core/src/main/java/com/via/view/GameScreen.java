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
import com.via.controller.RoadBuilder;
import com.via.controller.StopBuilder;
import com.via.model.Constants;
import com.via.model.GameState;
import com.via.model.building.StopAccess;
import com.via.model.economy.Storage;
import com.via.model.enums.AccessType;
import com.via.model.enums.Direction;
import com.via.model.enums.SpeedSetting;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;

public class GameScreen implements Screen {

    private final VIA game;
    private final GameState gameState;
    private final GameEngine gameEngine;
    private final RoadBuilder roadBuilder;
    private final StopBuilder stopBuilder;
    /** Increments for player-placed stop names ("Stop 1", …). */
    private int playerStopSequence = 1;

    // two cameras: one follows the map, one is fixed for HUD
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
    private static final float TOP_BAR_H = 36f;
    private static final float BOT_BAR_H = 48f;
    private static final float MAP_PX_W = Constants.MAP_WIDTH * Constants.TILE_SIZE;
    private static final float MAP_PX_H = Constants.MAP_HEIGHT * Constants.TILE_SIZE;

    private Rectangle btnExit;
    private boolean showExitDialog = false;
    private Rectangle rDialog;
    private Rectangle rBtnMainMenu;
    private Rectangle rBtnExitGame;
    private Rectangle rBtnClose;

    // bottom-right speed buttons
    private final Rectangle[] speedBtns = new Rectangle[4];
    private final String[] speedLabels = {"||", ">", ">>", ">>>"};
    private int activeSpeed = 1;

    // bottom-left tabs
    private final Rectangle[] tabBtns = new Rectangle[4];
    private final String[] tabLabels = {"Build", "Roads", "Buy", "..."};
    private int activeTab = -1;

    private boolean showPopup = false;
    private Rectangle rPopup;
    private Rectangle rPopupClose;

    // build mode options inside the Build popup
    private final Rectangle[] buildOptions = new Rectangle[4];
    private final String[] buildLabels = {"Road", "Stop", "Garage", "Terraform"};
    private int activeBuildOption = -1;

    // short message shown at the top after player actions
    private String feedbackMsg = "";
    private float feedbackTimer = 0f;
    private Color feedbackColor = Color.WHITE;

    // tile under the cursor in build mode
    private TileCoord hoveredTile = null;

    public GameScreen(VIA game, GameState gameState) {
        this.game = game;
        this.gameState = gameState;
        this.gameEngine = new GameEngine(gameState);
        this.roadBuilder = new RoadBuilder(gameState.getMap(), gameState.getEconomy(), 100, 50);
        this.stopBuilder = new StopBuilder(gameState.getMap(), gameState.getEconomy(), Constants.STOP_PLACEMENT_COST);

        mapCamera = new OrthographicCamera();
        mapCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mapCamera.position.set(MAP_PX_W / 2f, MAP_PX_H / 2f, 0);
        mapCamera.update();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        renderer = new WorldMapRenderer(batch, mapCamera);
        sr = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.4f);
        gl = new GlyphLayout();

        texButton = new Texture(Gdx.files.internal("button.png"));
        texPanel = new Texture(Gdx.files.internal("panel.png"));
        texButton.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texPanel.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        buildLayout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    // recalculates all HUD rectangles when the window resizes
    private void buildLayout(int w, int h) {
        hudCamera.setToOrtho(false, w, h);
        hudCamera.update();

        btnExit = new Rectangle(8f, h - TOP_BAR_H + 3f, 80f, TOP_BAR_H - 6f);

        float dW = 420f, dH = 230f;
        float dX = w / 2f - dW / 2f, dY = h / 2f - dH / 2f;
        rDialog = new Rectangle(dX, dY, dW, dH);
        float bW = 110f, bH = 46f, bY = dY + 20f;
        float spacing = (dW - bW * 3f) / 4f;
        rBtnMainMenu = new Rectangle(dX + spacing, bY, bW, bH);
        rBtnExitGame = new Rectangle(dX + spacing * 2f + bW, bY, bW, bH);
        rBtnClose = new Rectangle(dX + spacing * 3f + bW * 2f, bY, bW, bH);

        float btnW = 40f, btnH = 30f, gap = 4f;
        float startX = w - (btnW + gap) * 4 - 8f;
        for (int i = 0; i < 4; i++)
            speedBtns[i] = new Rectangle(startX + i * (btnW + gap), 8f, btnW, btnH);

        float tabW = 200f, tabH = BOT_BAR_H - 10f;
        for (int i = 0; i < 4; i++)
            tabBtns[i] = new Rectangle(8f + i * (tabW + 6f), 5f, tabW, tabH);

        rPopup = new Rectangle(8f, BOT_BAR_H + 5f, 600f, 60f);
        rPopupClose = new Rectangle(
            rPopup.x + rPopup.width - 24f,
            rPopup.y + rPopup.height - 24f,
            20f, 20f);
    }

    @Override
    public void show() {}

    @Override
    public void resize(int width, int height) {
        mapCamera.setToOrtho(false, width, height);
        mapCamera.position.set(MAP_PX_W / 2f, MAP_PX_H / 2f, 0);
        mapCamera.update();
        buildLayout(width, height);
    }

    @Override
    public void render(float delta) {
        if (feedbackTimer > 0) feedbackTimer -= delta;

        // advance simulation by one tick
        gameEngine.update();

        // figure out which tile the mouse is hovering over
        updateHoveredTile();

        ScreenUtils.clear(0.2f, 0.5f, 0.2f, 1f);

        if (!showExitDialog) handleMapInput(delta);

        // draw the world map
        mapCamera.update();
        batch.setProjectionMatrix(mapCamera.combined);
        renderer.render(gameState);

        // draw green/red highlight on hovered tile
        drawTileHighlight();

        // switch to HUD camera for all UI elements
        batch.setProjectionMatrix(hudCamera.combined);
        sr.setProjectionMatrix(hudCamera.combined);

        drawTopBar();
        drawBotBar();
        drawMinimap();

        if (showPopup) drawPopup();
        if (showExitDialog) drawExitDialog();

        drawFeedback();
    }

    // converts mouse position to world tile coordinates
    private void updateHoveredTile() {
        if (activeBuildOption < 0) {
            hoveredTile = null;
            return;
        }
        Vector3 world = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        mapCamera.unproject(world);
        int tx = (int)(world.x / Constants.TILE_SIZE);
        int ty = (int)(world.y / Constants.TILE_SIZE);
        TileCoord c = new TileCoord(tx, ty);
        hoveredTile = gameState.getMap().isWithinBounds(c) ? c : null;
    }

    // draws a coloured overlay on the tile under the cursor
    private void drawTileHighlight() {
        if (hoveredTile == null) return;

        sr.setProjectionMatrix(mapCamera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Tile tile = gameState.getMap().getTile(hoveredTile);
        boolean canPlace = switch (activeBuildOption) {
            case 0 -> tile != null && tile.isEmpty();
            case 1 -> stopBuilder.canPlaceStopAt(hoveredTile);
            default -> false;
        };

        // green = can build here, red = blocked
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(canPlace ? new Color(0.2f, 0.8f, 0.2f, 0.35f) : new Color(0.9f, 0.2f, 0.2f, 0.35f));
        sr.rect(hoveredTile.x * Constants.TILE_SIZE, hoveredTile.y * Constants.TILE_SIZE,
            Constants.TILE_SIZE, Constants.TILE_SIZE);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(canPlace ? new Color(0.3f, 1.0f, 0.3f, 0.9f) : new Color(1.0f, 0.3f, 0.3f, 0.9f));
        sr.rect(hoveredTile.x * Constants.TILE_SIZE, hoveredTile.y * Constants.TILE_SIZE,
            Constants.TILE_SIZE, Constants.TILE_SIZE);
        sr.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
        sr.setProjectionMatrix(hudCamera.combined);
    }

    private void handleMapInput(float delta) {
        // keyboard camera movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  mapCamera.translate(-CAMERA_SPEED * delta, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) mapCamera.translate(CAMERA_SPEED * delta, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    mapCamera.translate(0, CAMERA_SPEED * delta);
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  mapCamera.translate(0, -CAMERA_SPEED * delta);

        // Mouse drag scroll (right button so it doesn't conflict with click)
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT))
            mapCamera.translate(-Gdx.input.getDeltaX(), Gdx.input.getDeltaY());

        if (!Gdx.input.justTouched()) return;

        float mx = Gdx.input.getX();
        float myHud = Gdx.graphics.getHeight() - Gdx.input.getY(); // flipped Y for HUD
        float myRaw = Gdx.input.getY();                            // raw Y for world unproject

        // HUD buttons come first so they always respond
        if (btnExit.contains(mx, myHud)) { showExitDialog = true; return; }

        for (int i = 0; i < 4; i++) {
            if (speedBtns[i].contains(mx, myHud)) { activeSpeed = i; applySpeed(i); return; }
        }

        for (int i = 0; i < 4; i++) {
            if (tabBtns[i].contains(mx, myHud)) {
                if (activeTab == i && showPopup) {
                    showPopup = false;
                    activeTab = -1;
                    activeBuildOption = -1;
                } else {
                    activeTab = i;
                    showPopup = true;
                }
                return;
            }
        }

        // map click — convert screen coords to tile coords
        Vector3 world = new Vector3(mx, myRaw, 0);
        mapCamera.unproject(world);
        int tileX = (int)(world.x / Constants.TILE_SIZE);
        int tileY = (int)(world.y / Constants.TILE_SIZE);
        TileCoord coord = new TileCoord(tileX, tileY);

        if (!gameState.getMap().isWithinBounds(coord)) return;

        if (activeBuildOption == 0) {
            try {
                boolean placed = roadBuilder.buildRoad(coord);
                if (placed) showFeedback("Road built!", Color.GREEN);
                else showFeedback("Tile is already occupied.", Color.RED);
            } catch (IllegalArgumentException e) {
                showFeedback(e.getMessage(), Color.RED);
            }
            return;
        }

        if (activeBuildOption == 1) {
            if (gameState.getEconomy().getCapital() < Constants.STOP_PLACEMENT_COST) {
                showFeedback("Not enough money (need $" + Constants.STOP_PLACEMENT_COST + ").", Color.RED);
                return;
            }
            if (!stopBuilder.canPlaceStopAt(coord)) {
                showFeedback("Place on an empty road tile with another road beside it.", Color.RED);
                return;
            }
            StopAccess access = new StopAccess(AccessType.ROAD_TILE, coord, Direction.N);
            String name = "Stop " + playerStopSequence++;
            if (stopBuilder.placeStop(name, coord, access, new Storage())) {
                showFeedback("Stop built: " + name, Color.GREEN);
            } else {
                showFeedback("Could not place stop.", Color.RED);
            }
            return;
        }
        if (activeBuildOption == 2) { showFeedback("Garage placement coming soon.", Color.YELLOW); return; }
        if (activeBuildOption == 3) { showFeedback("Terraform coming soon.", Color.YELLOW); return; }

        // no build mode active — just print tile info to console
        Tile tile = gameState.getMap().getTile(coord);
        if (tile != null) {
            System.out.println("Tile [" + tileX + "," + tileY + "]"
                + " terrain=" + tile.getTerrain().getType()
                + " structure=" + (tile.getStructure() == null ? "null" : tile.getStructure().getClass().getSimpleName()));
        }
    }

    private void applySpeed(int index) {
        switch (index) {
            case 0 -> gameEngine.getTimeController().setSpeed(SpeedSetting.PAUSED);
            case 1 -> gameEngine.getTimeController().setSpeed(SpeedSetting.NORMAL);
            case 2 -> gameEngine.getTimeController().setSpeed(SpeedSetting.FAST);
            case 3 -> gameEngine.getTimeController().setSpeed(SpeedSetting.VERY_FAST);
        }
    }

    private void handleDialogInput() {
        if (!Gdx.input.justTouched()) return;
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        if (rBtnMainMenu.contains(mx, my)) { showExitDialog = false; game.setScreen(new MainMenuScreen(game)); return; }
        if (rBtnExitGame.contains(mx, my)) { Gdx.app.exit(); return; }
        if (rBtnClose.contains(mx, my)) showExitDialog = false;
    }

    private void drawTopBar() {
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.08f, 0.11f, 0.18f, 1f);
        sr.rect(0, h - TOP_BAR_H, w, TOP_BAR_H);
        sr.setColor(0.20f, 0.47f, 0.80f, 1f);
        sr.rect(0, h - TOP_BAR_H, w, 2f);
        sr.end();

        drawBtn(btnExit, "Exit", new Color(0.70f, 0.18f, 0.18f, 1f));

        // show current balance, red if bankrupt
        long capital = gameState.getEconomy().getCapital();
        String balStr = String.format("$%,d", capital);
        gl.setText(font, balStr);
        batch.begin();
        font.setColor(capital >= 0 ? new Color(0.30f, 0.90f, 0.40f, 1f) : new Color(1f, 0.25f, 0.25f, 1f));
        font.draw(batch, balStr, w - gl.width - 12f, h - TOP_BAR_H / 2f + gl.height / 2f);
        batch.end();

        // show active build mode name in the centre
        if (activeBuildOption >= 0) {
            int modeCost = switch (activeBuildOption) {
                case 0 -> 100;
                case 1 -> Constants.STOP_PLACEMENT_COST;
                default -> 0;
            };
            String modeLabel = modeCost > 0
                ? "Mode: " + buildLabels[activeBuildOption] + "  [cost: $" + modeCost + "]"
                : "Mode: " + buildLabels[activeBuildOption];
            gl.setText(font, modeLabel);
            batch.begin();
            font.setColor(1.0f, 0.85f, 0.20f, 1f);
            font.draw(batch, modeLabel, w / 2f - gl.width / 2f, h - TOP_BAR_H / 2f + gl.height / 2f);
            batch.end();
        }
    }

    private void drawBotBar() {
        float w = Gdx.graphics.getWidth();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.08f, 0.11f, 0.18f, 1f);
        sr.rect(0, 0, w, BOT_BAR_H);
        sr.setColor(0.20f, 0.47f, 0.80f, 1f);
        sr.rect(0, BOT_BAR_H, w, 2f);
        sr.end();

        // speed buttons — yellow tint on active
        batch.begin();
        for (int i = 0; i < 4; i++) {
            batch.setColor(i == activeSpeed ? Color.YELLOW : Color.WHITE);
            batch.draw(texButton, speedBtns[i].x, speedBtns[i].y, speedBtns[i].width, speedBtns[i].height);
            gl.setText(font, speedLabels[i]);
            font.setColor(Color.WHITE);
            font.draw(batch, speedLabels[i],
                speedBtns[i].x + (speedBtns[i].width - gl.width) / 2f,
                speedBtns[i].y + (speedBtns[i].height + gl.height) / 2f);
        }
        batch.end();

        // tab buttons — blue tint on active
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 4; i++) {
            sr.setColor(i == activeTab ? new Color(0.20f, 0.47f, 0.80f, 1f) : new Color(0.18f, 0.22f, 0.32f, 1f));
            sr.rect(tabBtns[i].x, tabBtns[i].y, tabBtns[i].width, tabBtns[i].height);
        }
        sr.end();

        batch.begin();
        for (int i = 0; i < 4; i++) {
            gl.setText(font, tabLabels[i]);
            font.setColor(Color.WHITE);
            font.draw(batch, tabLabels[i],
                tabBtns[i].x + (tabBtns[i].width - gl.width) / 2f,
                tabBtns[i].y + (tabBtns[i].height + gl.height) / 2f);
        }
        batch.end();
    }

    private void drawMinimap() {
        float w = Gdx.graphics.getWidth();
        float size = 160f;
        float x = w - size - 10f, y = BOT_BAR_H + 10f;

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
        font.draw(batch, "Minimap", x + (size - gl.width) / 2f, y + (size + gl.height) / 2f);
        batch.end();
    }

    private void drawPopup() {
        // popup position follows the active tab
        rPopup = new Rectangle(tabBtns[activeTab].x, BOT_BAR_H + 5f, 600f, 60f);
        rPopupClose = new Rectangle(
            rPopup.x + rPopup.width - 24f,
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
        gl.setText(font, "X");
        font.draw(batch, "X",
            rPopupClose.x + (rPopupClose.width - gl.width) / 2f,
            rPopupClose.y + (rPopupClose.height + gl.height) / 2f);
        batch.end();

        // Build tab shows Road / Stop / Garage / Terraform options
        if (activeTab == 0) {
            float optW = 130f, optH = rPopup.height - 16f;
            float optY = rPopup.y + 8f, gap = 6f;
            float startX = rPopup.x + 8f;

            for (int i = 0; i < buildLabels.length; i++) {
                buildOptions[i] = new Rectangle(startX + i * (optW + gap), optY, optW, optH);

                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(i == activeBuildOption
                    ? new Color(0.20f, 0.47f, 0.80f, 1f)
                    : new Color(0.14f, 0.18f, 0.28f, 1f));
                sr.rect(buildOptions[i].x, buildOptions[i].y, buildOptions[i].width, buildOptions[i].height);
                sr.end();

                batch.begin();
                font.setColor(Color.WHITE);
                gl.setText(font, buildLabels[i]);
                font.draw(batch, buildLabels[i],
                    buildOptions[i].x + (buildOptions[i].width - gl.width) / 2f,
                    buildOptions[i].y + (buildOptions[i].height + gl.height) / 2f);
                batch.end();
            }
        }

        if (Gdx.input.justTouched()) {
            float mx = Gdx.input.getX();
            float my = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (rPopupClose.contains(mx, my)) {
                showPopup = false;
                activeTab = -1;
                activeBuildOption = -1;
                return;
            }

            if (activeTab == 0) {
                for (int i = 0; i < buildOptions.length; i++) {
                    if (buildOptions[i] != null && buildOptions[i].contains(mx, my)) {
                        // clicking same option again deselects it
                        activeBuildOption = (activeBuildOption == i) ? -1 : i;
                        if (activeBuildOption >= 0)
                            showFeedback("Click tiles to place: " + buildLabels[activeBuildOption], Color.YELLOW);
                        return;
                    }
                }
            }
        }
    }

    private void drawExitDialog() {
        handleDialogInput();
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();

        // dim overlay behind dialog
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
        drawCentred("What do you want to do?", cx, rDialog.y + rDialog.height * 0.65f,
            new Color(0.80f, 0.85f, 0.95f, 1f));

        drawBtn(rBtnMainMenu, "Main Menu", new Color(0.20f, 0.47f, 0.80f, 1f));
        drawBtn(rBtnExitGame, "Exit Game", new Color(0.72f, 0.18f, 0.18f, 1f));
        drawBtn(rBtnClose, "Close", new Color(0.35f, 0.38f, 0.48f, 1f));
    }

    private void showFeedback(String message, Color color) {
        feedbackMsg = message;
        feedbackColor = color;
        feedbackTimer = 3f;
    }

    private void drawFeedback() {
        if (feedbackTimer <= 0) return;
        gl.setText(font, feedbackMsg);
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        float x = (w - gl.width) / 2f;
        float y = h - TOP_BAR_H - 10f;
        batch.begin();
        font.setColor(0f, 0f, 0f, 0.85f);
        font.draw(batch, feedbackMsg, x + 1, y - 1);
        font.setColor(feedbackColor);
        font.draw(batch, feedbackMsg, x, y);
        batch.end();
    }

    private void drawBtn(Rectangle r, String label, Color tint) {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        float br = r.contains(mx, my) ? 1.28f : 1f; // brighten on hover

        batch.begin();
        batch.setColor(Math.min(tint.r * br, 1f), Math.min(tint.g * br, 1f), Math.min(tint.b * br, 1f), 1f);
        batch.draw(texButton, r.x, r.y, r.width, r.height);
        gl.setText(font, label);
        float tx = r.x + (r.width - gl.width) / 2f;
        float ty = r.y + (r.height + gl.height) / 2f;
        font.setColor(0f, 0f, 0f, 0.85f);
        font.draw(batch, label, tx + 2, ty - 2);
        font.setColor(Color.WHITE);
        font.draw(batch, label, tx, ty);
        batch.end();
    }

    private void drawCentred(String text, float cx, float y, Color color) {
        gl.setText(font, text);
        float x = cx - gl.width / 2f;
        batch.begin();
        font.setColor(0f, 0f, 0f, 0.85f);
        font.draw(batch, text, x + 2, y - 2);
        font.setColor(color);
        font.draw(batch, text, x, y);
        batch.end();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        sr.dispose();
        font.dispose();
        texButton.dispose();
        texPanel.dispose();
        renderer.dispose();
    }
}
