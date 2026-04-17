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
import com.via.controller.GameController;
import com.via.controller.GameEngine;
import com.via.controller.RoadBuilder;
import com.via.controller.StopBuilder;
import com.via.controller.GarageBuilder;
import com.via.model.Constants;
import com.via.model.GameState;
import com.via.model.building.*;
import com.via.model.economy.Storage;
import com.via.model.map.Structure;
import com.via.model.map.Terrain;
import com.via.model.enums.AccessType;
import com.via.model.enums.Direction;
import com.via.model.enums.SpeedSetting;
import com.via.model.map.Tile;
import com.via.model.map.TileCoord;
import com.via.model.vehicle.Route;
import com.via.model.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {

    private final VIA game;
    private final GameState gameState;
    private final GameEngine gameEngine;
    private final RoadBuilder roadBuilder;
    private final StopBuilder stopBuilder;
    private final GarageBuilder garageBuilder;

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
    private final String[] tabLabels = {"Build", "Routes", "Buy", "Vehicles"};
    private int activeTab = -1;

    private boolean showPopup = false;
    private Rectangle rPopup;
    private Rectangle rPopupClose;

    // build mode options inside the Build popup
    private final Rectangle[] buildOptions = new Rectangle[5]; // Changed from 4 to 5
    private final String[] buildLabels = {"Road", "Stop", "Garage", "Terraform", "Demolish"}; // Added Demolish
    private int activeBuildOption = -1;

    // minimap content bounds (lazy-computed once, 0 = not yet computed)
    private int minimapContentW = 0, minimapContentH = 0;

    // buy popup — one button per catalog entry
    private final Rectangle[] buyBtns = new Rectangle[GameController.VEHICLE_CATALOG.size()];

    // route creation UI
    private final List<Rectangle> stopCheckboxes = new ArrayList<>();
    private final List<Boolean> selectedStops = new ArrayList<>();
    private Rectangle rBtnCreateRoute;

    // vehicle management UI
    private final List<Rectangle> vehicleAssignBtns = new ArrayList<>();
    private final List<Rectangle> vehicleSellBtns = new ArrayList<>();

    // post-purchase route assignment dialog
    private boolean showRouteAssignDialog = false;
    private Vehicle pendingVehicle = null;
    private String pendingVehicleName = "";
    private final List<Rectangle> routeAssignBtns = new ArrayList<>();
    private Rectangle rBtnSkipRoute;
    private Rectangle rRouteAssignDialog;

    // short message shown at the top after player actions
    private String feedbackMsg = "";
    private float feedbackTimer = 0f;
    private Color feedbackColor = Color.WHITE;

    // tile under the cursor in build mode
    private TileCoord hoveredTile = null;
    private TileCoord lastDragTile = null;

    // Garage Detail UI
    private boolean showGarageDetailDialog = false;
    private Garage selectedGarageForDetails = null;
    private Rectangle rGarageDetailClose;
    private final List<Rectangle> garageDetailSellBtns = new ArrayList<>();

    public GameScreen(VIA game, GameState gameState) {
        this.game = game;
        this.gameState = gameState;
        this.gameEngine = new GameEngine(gameState);
        this.roadBuilder = new RoadBuilder(gameState.getMap(), gameState.getEconomy(), 100, 50);
        this.stopBuilder = new StopBuilder(gameState.getMap(), gameState.getEconomy(), Constants.STOP_PLACEMENT_COST);
        this.garageBuilder = new GarageBuilder(gameState, 5000); // 5000 base cost for garages

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

        if (!showExitDialog && !showRouteAssignDialog) handleMapInput(delta);

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
        if (showRouteAssignDialog) drawRouteAssignDialog();
        if (showGarageDetailDialog) drawGarageDetailDialog();
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
            case 2 -> tile != null && tile.isEmpty(); // Garage placeholder validation
            case 4 -> tile != null && tile.getStructure() instanceof Garage;
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

        // Road drag — hold LMB to paint roads continuously
        if (activeBuildOption == 0 && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            Vector3 dw = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            mapCamera.unproject(dw);
            TileCoord dc = new TileCoord((int)(dw.x / Constants.TILE_SIZE), (int)(dw.y / Constants.TILE_SIZE));
            if (!dc.equals(lastDragTile) && gameState.getMap().isWithinBounds(dc)) {
                lastDragTile = dc;
                try { roadBuilder.buildRoad(dc); } catch (IllegalArgumentException ignored) {}
            }
        }
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) lastDragTile = null;

        // Only process left clicks for interaction
        if (!Gdx.input.justTouched() || !Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;

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

        // minimap click — scroll main camera to clicked world position
        float mmX = mmX(), mmY = mmY();
        if (mx >= mmX && mx <= mmX + MM_SIZE && myHud >= mmY && myHud <= mmY + MM_SIZE) {
            ensureMinimapBounds();
            float scaleX = MM_SIZE / (minimapContentW * Constants.TILE_SIZE);
            float scaleY = MM_SIZE / (minimapContentH * Constants.TILE_SIZE);
            float worldX = (mx   - mmX) / scaleX;
            float worldY = (myHud - mmY) / scaleY;
            mapCamera.position.set(worldX, worldY, 0);
            return;
        }

        // map click — convert screen coords to tile coords
        Vector3 world = new Vector3(mx, myRaw, 0);
        mapCamera.unproject(world);
        int tileX = (int)(world.x / Constants.TILE_SIZE);
        int tileY = (int)(world.y / Constants.TILE_SIZE);
        TileCoord coord = new TileCoord(tileX, tileY);

        if (!gameState.getMap().isWithinBounds(coord)) return;

        if (activeBuildOption == 0) {
            // handled by drag logic above
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

        if (activeBuildOption == 2) {
            if (gameState.getEconomy().getCapital() < 5000) {
                showFeedback("Not enough money (need $5000).", Color.RED);
                return;
            }
            try {
                if (garageBuilder.placeGarage(coord)) {
                    showFeedback("Garage built!", Color.GREEN);
                } else {
                    showFeedback("Tile is occupied.", Color.RED);
                }
            } catch (IllegalArgumentException e) {
                showFeedback(e.getMessage(), Color.RED);
            }
            return;
        }

        if (activeBuildOption == 3) { showFeedback("Terraform coming soon.", Color.YELLOW); return; }

        if (activeBuildOption == 4) {
            Tile tile = gameState.getMap().getTile(coord);
            if (tile != null && tile.getStructure() instanceof Garage garage) {
                // 1. Remove the structure from the map tile
                tile.setStructure(null);

                // 2. Remove the building from the GameState registry
                gameState.removeBuilding(garage);

                showFeedback("Garage demolished!", Color.GREEN);
            } else {
                showFeedback("Click on a Garage to demolish it.", Color.RED);
            }
            return;
        }
        // If no build mode is active, check if they clicked a Garage
        if (activeBuildOption == -1) {
            Tile tile = gameState.getMap().getTile(coord);
            if (tile != null && tile.getStructure() instanceof Garage clickedGarage) {
                selectedGarageForDetails = clickedGarage;
                showGarageDetailDialog = true;
                return;
            }
        }
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

        if (activeBuildOption >= 0) {
            int modeCost = switch (activeBuildOption) {
                case 0 -> 100;
                case 1 -> Constants.STOP_PLACEMENT_COST;
                case 2 -> 5000;
                default -> 0;
            };
            if (activeBuildOption == 0 && hoveredTile != null) {
                modeCost = gameState.getMap().getRoadBuildCost(hoveredTile, 100, 50);
            }

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

    private static final float MM_SIZE = 240f;

    /** Returns the minimap x origin for the current screen width. */
    private float mmX() { return Gdx.graphics.getWidth() - MM_SIZE - 10f; }
    private float mmY() { return BOT_BAR_H + 10f; }

    /** Scans for the max x/y of non-null tiles; result cached after first call. */
    private void ensureMinimapBounds() {
        if (minimapContentW > 0) return;
        com.via.model.map.Map map = gameState.getMap();
        for (int x = 0; x < map.size.width; x++) {
            for (int y = 0; y < map.size.height; y++) {
                if (map.getTile(new TileCoord(x, y)) != null) {
                    if (x + 1 > minimapContentW) minimapContentW = x + 1;
                    if (y + 1 > minimapContentH) minimapContentH = y + 1;
                }
            }
        }
        if (minimapContentW == 0) minimapContentW = map.size.width;
        if (minimapContentH == 0) minimapContentH = map.size.height;
    }

    private void drawMinimap() {
        ensureMinimapBounds();
        float mmX = mmX(), mmY = mmY();
        com.via.model.map.Map map = gameState.getMap();
        int mapW = map.size.width;
        int mapH = map.size.height;

        // scale: world pixel -> minimap pixel, based on actual initialized area
        float scaleX = MM_SIZE / (minimapContentW * Constants.TILE_SIZE);
        float scaleY = MM_SIZE / (minimapContentH * Constants.TILE_SIZE);
        float tileW  = Constants.TILE_SIZE * scaleX;
        float tileH  = Constants.TILE_SIZE * scaleY;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // --- tiles + vehicles in one Filled pass ---
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // background
        sr.setColor(0.07f, 0.10f, 0.17f, 1f);
        sr.rect(mmX, mmY, MM_SIZE, MM_SIZE);

        // tiles
        for (int x = 0; x < mapW; x++) {
            for (int y = 0; y < mapH; y++) {
                Tile tile = map.getTile(new TileCoord(x, y));
                if (tile == null) continue;
                sr.setColor(minimapTileColor(tile));
                sr.rect(mmX + x * tileW, mmY + y * tileH, tileW, tileH);
            }
        }

        // vehicles as 3×3 dots
        for (Vehicle v : gameState.getVehicles()) {
            if (v.getPosition() == null) continue;
            float wx = mmLerp(v.getPosition().getOrigin().x,
                v.getPosition().getDestination().x,
                v.getPosition().getProgress()) * Constants.TILE_SIZE;
            float wy = mmLerp(v.getPosition().getOrigin().y,
                v.getPosition().getDestination().y,
                v.getPosition().getProgress()) * Constants.TILE_SIZE;
            float px = mmX + wx * scaleX;
            float py = mmY + wy * scaleY;
            boolean isBus = switch (v.getType()) {
                case BUS, CITY_BUS, EXPRESS_BUS -> true;
                default -> false;
            };
            sr.setColor(isBus ? new Color(0.20f, 0.65f, 0.95f, 1f)
                : new Color(0.95f, 0.65f, 0.10f, 1f));
            sr.rect(px - 1.5f, py - 1.5f, 3f, 3f);
        }

        // viewport rect — clamped to minimap bounds
        float vpX  = mmX + (mapCamera.position.x - mapCamera.viewportWidth  / 2f) * scaleX;
        float vpY  = mmY + (mapCamera.position.y - mapCamera.viewportHeight / 2f) * scaleY;
        float vpX2 = vpX + mapCamera.viewportWidth  * scaleX;
        float vpY2 = vpY + mapCamera.viewportHeight * scaleY;
        float cvX  = Math.max(mmX, vpX);
        float cvY  = Math.max(mmY, vpY);
        float cvW  = Math.max(0, Math.min(mmX + MM_SIZE, vpX2) - cvX);
        float cvH  = Math.max(0, Math.min(mmY + MM_SIZE, vpY2) - cvY);
        sr.setColor(1f, 1f, 1f, 0.10f);
        sr.rect(cvX, cvY, cvW, cvH);

        sr.end();

        // --- border + viewport outline in Line pass ---
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.20f, 0.47f, 0.80f, 1f);
        sr.rect(mmX, mmY, MM_SIZE, MM_SIZE);
        sr.setColor(1f, 1f, 1f, 0.85f);
        sr.rect(cvX, cvY, cvW, cvH);
        sr.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private Color minimapTileColor(Tile tile) {
        Structure s = tile.getStructure();
        if (s instanceof Road)     return new Color(0.52f, 0.52f, 0.52f, 1f);
        if (s instanceof City)     return new Color(0.90f, 0.85f, 0.50f, 1f);
        if (s instanceof Facility) return new Color(0.75f, 0.40f, 0.18f, 1f);
        if (s instanceof Garage)   return new Color(0.48f, 0.48f, 0.78f, 1f);
        if (tile.getOverlay() != null) return new Color(1.0f, 0.90f, 0.15f, 1f);
        Terrain t = tile.getTerrain();
        if (t.hasForest()) return new Color(0.08f, 0.36f, 0.08f, 1f);
        return new Color(0.22f, 0.58f, 0.22f, 1f);
    }

    private static float mmLerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private boolean playerOwnsGarage() {
        for (Building b : gameState.getBuildings()) {
            if (b instanceof Garage) return true;
        }
        return false;
    }

    private void drawPopup() {
        // popup size varies by tab; Buy tab is taller and wider
        boolean isBuildTab = (activeTab == 0);
        boolean isBuyTab = (activeTab == 2);
        boolean isRoutesTab = (activeTab == 1);
        boolean isVehiclesTab = (activeTab == 3);

        float popupX = (isBuildTab || isBuyTab || isRoutesTab || isVehiclesTab) ? 8f : tabBtns[activeTab].x;
        float popupW = (isBuildTab || isBuyTab || isRoutesTab || isVehiclesTab) ? 720f : 600f;
        int vehicleCount = isVehiclesTab ? gameState.getVehicles().size() : 0;
        int stopCount = isRoutesTab ? countMapStops() : 0;
        float popupH = isBuyTab ? 290f
            : isRoutesTab ? Math.min(420f, 62f + Math.max(1, stopCount) * 36f + 54f)
            : isVehiclesTab ? Math.min(400f, 62f + Math.max(1, vehicleCount) * 38f + 16f)
            : 60f;

        rPopup = new Rectangle(popupX, BOT_BAR_H + 5f, popupW, popupH);
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

        // Build tab — Road / Stop / Garage / Terraform options
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

        // Buy tab — vehicle catalog rows
        if (activeTab == 2) {
            List<GameController.VehicleCatalogEntry> catalog = GameController.VEHICLE_CATALOG;
            float padX = 10f;
            float baseX = rPopup.x + padX + 4f;
            float rowH = 35f;
            float headerH = 24f;
            float topY = rPopup.y + rPopup.height - 36f; // leaves room for stripe + close btn

            // header row background
            float headerY = topY - headerH;
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0.12f, 0.16f, 0.26f, 1f);
            sr.rect(rPopup.x + padX, headerY, rPopup.width - padX * 2f - 26f, headerH);
            sr.end();

            font.getData().setScale(1.0f);
            batch.begin();
            font.setColor(new Color(0.70f, 0.80f, 1.0f, 1f));
            float hY = headerY + headerH / 2f + 7f;
            font.draw(batch, "Vehicle",  baseX,          hY);
            font.draw(batch, "Cap",      baseX + 128f,   hY);
            font.draw(batch, "Speed",    baseX + 192f,   hY);
            font.draw(batch, "Maint/c",  baseX + 268f,   hY);
            font.draw(batch, "Cost",     baseX + 368f,   hY);
            batch.end();
            font.getData().setScale(1.4f);

            // vehicle rows
            float curY = headerY - 4f;
            for (int i = 0; i < catalog.size(); i++) {
                curY -= rowH;
                float rowY = curY;
                GameController.VehicleCatalogEntry entry = catalog.get(i);
                boolean canAfford = gameState.getEconomy().getCapital() >= entry.purchaseCost();

                // alternating row background
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(i % 2 == 0
                    ? new Color(0.10f, 0.14f, 0.22f, 1f)
                    : new Color(0.12f, 0.17f, 0.27f, 1f));
                sr.rect(rPopup.x + padX, rowY, rPopup.width - padX * 2f - 26f, rowH);
                sr.end();

                // Buy button
                buyBtns[i] = new Rectangle(rPopup.x + rPopup.width - 26f - 84f, rowY + 4f, 82f, rowH - 8f);
                Color btnColor = canAfford
                    ? new Color(0.15f, 0.55f, 0.25f, 1f)
                    : new Color(0.45f, 0.20f, 0.20f, 1f);
                drawBtn(buyBtns[i], "Buy", btnColor);

                // row text
                font.getData().setScale(1.0f);
                batch.begin();
                font.setColor(canAfford ? Color.WHITE : new Color(0.50f, 0.50f, 0.50f, 1f));
                float tY = rowY + rowH / 2f + 7f;
                font.draw(batch, entry.displayName(),                   baseX,        tY);
                font.draw(batch, String.valueOf(entry.capacity()),       baseX + 128f, tY);
                font.draw(batch, String.format("%.2f", entry.speed()),   baseX + 192f, tY);
                font.draw(batch, "$" + entry.maintenanceCost(),          baseX + 268f, tY);
                font.draw(batch, String.format("$%,d", entry.purchaseCost()), baseX + 368f, tY);
                batch.end();
                font.getData().setScale(1.4f);
            }
        }

        // Routes tab — create routes from placed stops
        if (activeTab == 1) {
            List<StopOverlay> allStops = collectMapStops();

            long selectedCount = selectedStops.stream().filter(b -> b).count();

            // header
            font.getData().setScale(1.0f);
            batch.begin();
            font.setColor(new Color(0.70f, 0.80f, 1.0f, 1f));
            font.draw(batch, "Create Route", rPopup.x + 12f, rPopup.y + rPopup.height - 36f);
            font.setColor(new Color(0.50f, 0.55f, 0.65f, 1f));
            String selHint = selectedCount == 0 ? "select stops below"
                : selectedCount == 1 ? "1 stop selected — pick at least 2"
                : selectedCount + " stops selected";
            font.draw(batch, "—  " + selHint, rPopup.x + 120f, rPopup.y + rPopup.height - 36f);
            batch.end();

            // sync selectedStops size
            while (selectedStops.size() < allStops.size()) selectedStops.add(false);
            while (selectedStops.size() > allStops.size()) selectedStops.remove(selectedStops.size() - 1);

            stopCheckboxes.clear();
            float rowH = 32f;
            float curY = rPopup.y + rPopup.height - 58f;

            for (int i = 0; i < allStops.size(); i++) {
                curY -= rowH;
                boolean selected = selectedStops.get(i);

                // row background
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(selected
                    ? new Color(0.12f, 0.22f, 0.38f, 1f)
                    : i % 2 == 0 ? new Color(0.10f, 0.14f, 0.22f, 1f) : new Color(0.13f, 0.17f, 0.27f, 1f));
                sr.rect(rPopup.x + 8f, curY, rPopup.width - 32f, rowH);
                // left accent bar
                sr.setColor(selected ? new Color(0.20f, 0.60f, 0.95f, 1f) : new Color(0.25f, 0.30f, 0.42f, 1f));
                sr.rect(rPopup.x + 8f, curY, 4f, rowH);
                sr.end();

                // checkbox indicator
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(selected ? new Color(0.20f, 0.70f, 0.35f, 1f) : new Color(0.20f, 0.24f, 0.34f, 1f));
                sr.rect(rPopup.x + 18f, curY + rowH / 2f - 7f, 14f, 14f);
                sr.end();

                batch.begin();
                font.setColor(selected ? Color.WHITE : new Color(0.75f, 0.78f, 0.85f, 1f));
                font.draw(batch, allStops.get(i).getName(), rPopup.x + 40f, curY + rowH / 2f + 7f);
                if (selected) {
                    font.setColor(new Color(0.40f, 0.80f, 0.95f, 1f));
                    font.draw(batch, "✓", rPopup.x + rPopup.width - 50f, curY + rowH / 2f + 7f);
                }
                batch.end();

                stopCheckboxes.add(new Rectangle(rPopup.x + 8f, curY, rPopup.width - 32f, rowH));
            }

            if (allStops.isEmpty()) {
                batch.begin();
                font.setColor(new Color(0.50f, 0.55f, 0.65f, 1f));
                font.draw(batch, "No stops placed yet. Use the Build tab.", rPopup.x + 18f, curY - 8f);
                batch.end();
            }

            // Create Route button
            rBtnCreateRoute = new Rectangle(rPopup.x + rPopup.width - 144f, rPopup.y + 8f, 130f, 38f);
            drawBtn(rBtnCreateRoute, "Create Route  (" + selectedCount + ")",
                selectedCount >= 2 ? new Color(0.20f, 0.47f, 0.80f, 1f) : new Color(0.28f, 0.30f, 0.40f, 1f));

            font.getData().setScale(1.4f);
        }

        if (activeTab == 3) {
            List<Vehicle> vehicles = gameState.getVehicles();

            font.getData().setScale(1.0f);
            batch.begin();
            font.setColor(new Color(0.70f, 0.80f, 1.0f, 1f));
            font.draw(batch, "Vehicles", rPopup.x + 12f, rPopup.y + rPopup.height - 36f);
            font.setColor(new Color(0.50f, 0.55f, 0.65f, 1f));
            String subText = playerOwnsGarage()
                ? vehicles.size() + " total  —  click a row to assign route or sell manually"
                : vehicles.size() + " total  —  (Build a garage to sell vehicles)";
            font.draw(batch, subText, rPopup.x + 90f, rPopup.y + rPopup.height - 36f);
            batch.end();

            vehicleAssignBtns.clear();
            vehicleSellBtns.clear();
            float rowH = 34f;
            float curY = rPopup.y + rPopup.height - 58f;

            for (int i = 0; i < vehicles.size(); i++) {
                Vehicle v = vehicles.get(i);
                curY -= rowH;
                float rowY = curY;

                // row background
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(i % 2 == 0
                    ? new Color(0.10f, 0.14f, 0.22f, 1f)
                    : new Color(0.13f, 0.17f, 0.27f, 1f));
                sr.rect(rPopup.x + 8f, rowY, rPopup.width - 32f, rowH);
                sr.end();

                // left color bar (green = has route, orange = no route)
                boolean hasRoute = v.getAssignedRoute() != null;
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(hasRoute ? new Color(0.20f, 0.65f, 0.30f, 1f) : new Color(0.75f, 0.40f, 0.15f, 1f));
                sr.rect(rPopup.x + 8f, rowY, 4f, rowH);
                sr.end();

                // vehicle name
                String displayName = GameController.VEHICLE_CATALOG.stream()
                    .filter(e -> e.type() == v.getType())
                    .map(GameController.VehicleCatalogEntry::displayName)
                    .findFirst().orElse(v.getType().toString());

                String routeInfo;
                if (v.isReturningToGarage()) {
                    routeInfo = "En route to Garage";
                } else if (v.isMaintaining()) {
                    routeInfo = "Maintaining...";
                } else if (hasRoute) {
                    routeInfo = v.getAssignedRoute().size() + " stops";
                } else {
                    routeInfo = "No route";
                }

                batch.begin();
                font.setColor(Color.WHITE);
                font.draw(batch, "#" + (i + 1) + "  " + displayName, rPopup.x + 18f, rowY + rowH / 2f + 7f);
                font.setColor(hasRoute ? new Color(0.50f, 0.90f, 0.55f, 1f) : new Color(0.90f, 0.55f, 0.30f, 1f));
                font.draw(batch, routeInfo, rPopup.x + 340f, rowY + rowH / 2f + 7f);
                batch.end();

                // Button for assigning route
                Rectangle assignBtn = new Rectangle(rPopup.x + 8f, rowY, rPopup.width - 100f, rowH);
                vehicleAssignBtns.add(assignBtn);

                // Button for manual selling
                if (playerOwnsGarage()) {
                    Rectangle sellBtn = new Rectangle(rPopup.x + rPopup.width - 86f, rowY + 4f, 70f, rowH - 8f);
                    vehicleSellBtns.add(sellBtn);
                    drawBtn(sellBtn, "Sell", new Color(0.65f, 0.25f, 0.25f, 1f));
                }
            }

            if (vehicles.isEmpty()) {
                batch.begin();
                font.setColor(new Color(0.50f, 0.55f, 0.65f, 1f));
                font.draw(batch, "No vehicles yet. Buy one from the Buy tab.", rPopup.x + 18f, curY - 8f);
                batch.end();
            }

            font.getData().setScale(1.4f);
        }

        // input handling — skip if another dialog is open
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !showRouteAssignDialog) {
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
                        activeBuildOption = (activeBuildOption == i) ? -1 : i;
                        if (activeBuildOption >= 0)
                            showFeedback("Click tiles to place: " + buildLabels[activeBuildOption], Color.YELLOW);
                        return;
                    }
                }
            }

            if (activeTab == 2) {
                for (int i = 0; i < buyBtns.length; i++) {
                    if (buyBtns[i] != null && buyBtns[i].contains(mx, my)) {
                        GameController.VehicleCatalogEntry entry = GameController.VEHICLE_CATALOG.get(i);
                        String error = GameController.buyVehicle(gameState, entry.type());
                        if (error != null) {
                            showFeedback(error, Color.RED);
                        } else {
                            showFeedback("Purchased: " + entry.displayName() + "!", Color.GREEN);
                            List<Vehicle> vehicles = gameState.getVehicles();
                            if (!vehicles.isEmpty() && !gameState.getRoutes().isEmpty()) {
                                pendingVehicle = vehicles.get(vehicles.size() - 1);
                                pendingVehicleName = entry.displayName();
                                showRouteAssignDialog = true;
                            }
                        }
                        return;
                    }
                }
            }

            if (activeTab == 1) {
                // Handle stop checkbox clicks
                for (int i = 0; i < stopCheckboxes.size(); i++) {
                    if (stopCheckboxes.get(i).contains(mx, my)) {
                        selectedStops.set(i, !selectedStops.get(i));
                        return;
                    }
                }

                // Handle create route button
                if (rBtnCreateRoute != null && rBtnCreateRoute.contains(mx, my)) {
                    long selectedCount = selectedStops.stream().filter(b -> b).count();
                    if (selectedCount >= 2) {
                        // Collect selected stops
                        List<StopOverlay> routeStops = new ArrayList<>();
                        List<StopOverlay> allStops = new ArrayList<>();
                        for (int x = 0; x < gameState.getMap().size.width; x++) {
                            for (int y = 0; y < gameState.getMap().size.height; y++) {
                                Tile tile = gameState.getMap().getTile(new TileCoord(x, y));
                                if (tile != null && tile.getOverlay() instanceof StopOverlay stop) {
                                    allStops.add(stop);
                                }
                            }
                        }
                        for (int i = 0; i < selectedStops.size() && i < allStops.size(); i++) {
                            if (selectedStops.get(i)) {
                                routeStops.add(allStops.get(i));
                            }
                        }

                        // Create route
                        Route newRoute = new Route(routeStops);
                        gameState.addRoute(newRoute);
                        showFeedback("Route created with " + routeStops.size() + " stops!", Color.GREEN);

                        // Clear selection
                        for (int i = 0; i < selectedStops.size(); i++) {
                            selectedStops.set(i, false);
                        }
                    } else {
                        showFeedback("Select at least 2 stops to create a route.", Color.RED);
                    }
                    return;
                }
            }

            if (activeTab == 3) {
                // Handle manual sell logic first to avoid triggering route assignment
                for (int i = 0; i < vehicleSellBtns.size(); i++) {
                    if (vehicleSellBtns.get(i).contains(mx, my) && i < gameState.getVehicles().size()) {
                        Vehicle v = gameState.getVehicles().get(i);
                        long resale = v.getResaleValue();
                        gameState.getEconomy().earn(resale);
                        gameState.removeVehicle(v);
                        showFeedback("Vehicle sold for $" + resale, Color.GREEN);
                        return;
                    }
                }

                // Handle vehicle route assignment clicks
                for (int i = 0; i < vehicleAssignBtns.size(); i++) {
                    if (vehicleAssignBtns.get(i).contains(mx, my) && i < gameState.getVehicles().size()) {
                        if (gameState.getRoutes().isEmpty()) {
                            showFeedback("No routes available. Create a route first.", Color.RED);
                            return;
                        }
                        pendingVehicle = gameState.getVehicles().get(i);
                        pendingVehicleName = pendingVehicle.getType().toString();
                        showRouteAssignDialog = true;
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

    private void drawRouteAssignDialog() {
        List<Route> routes = gameState.getRoutes();
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();

        float dW = 480f;
        float dH = 120f + routes.size() * 50f + 60f;
        float dX = w / 2f - dW / 2f;
        float dY = h / 2f - dH / 2f;
        rRouteAssignDialog = new Rectangle(dX, dY, dW, dH);

        // dim overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.68f);
        sr.rect(0, 0, w, h);
        sr.setColor(0.07f, 0.10f, 0.17f, 0.97f);
        sr.rect(dX, dY, dW, dH);
        sr.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        batch.setColor(1f, 1f, 1f, 0.20f);
        batch.draw(texPanel, dX, dY, dW, dH);
        batch.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.20f, 0.47f, 0.80f, 1f);
        sr.rect(dX, dY + dH - 4f, dW, 4f);
        sr.end();

        float cx = dX + dW / 2f;
        drawCentred("Assign Route", cx, dY + dH - 18f, Color.WHITE);
        drawCentred(pendingVehicleName + " is ready — assign to a route?",
            cx, dY + dH - 44f, new Color(0.80f, 0.85f, 0.95f, 1f));

        // route buttons
        routeAssignBtns.clear();
        float btnY = dY + dH - 90f;
        for (int i = 0; i < routes.size(); i++) {
            btnY -= 50f;
            Route r = routes.get(i);
            Rectangle btn = new Rectangle(dX + 20f, btnY, dW - 40f, 42f);
            routeAssignBtns.add(btn);
            String label = "Route " + (i + 1) + "  (" + r.size() + " stops)";
            drawBtn(btn, label, new Color(0.20f, 0.47f, 0.80f, 1f));
        }

        rBtnSkipRoute = new Rectangle(cx - 55f, dY + 14f, 110f, 38f);
        drawBtn(rBtnSkipRoute, "Skip", new Color(0.35f, 0.38f, 0.48f, 1f));

        // input handling
        if (Gdx.input.justTouched()) {
            float mx = Gdx.input.getX();
            float my = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (rBtnSkipRoute.contains(mx, my)) {
                showRouteAssignDialog = false;
                pendingVehicle = null;
                return;
            }

            for (int i = 0; i < routeAssignBtns.size(); i++) {
                if (routeAssignBtns.get(i).contains(mx, my) && i < routes.size()) {
                    GameController.assignVehicleToRoute(pendingVehicle, routes.get(i));
                    showFeedback("Route assigned to " + pendingVehicleName + "!", Color.GREEN);
                    showRouteAssignDialog = false;
                    pendingVehicle = null;
                    return;
                }
            }
        }
    }

    private void drawGarageDetailDialog() {
        if (selectedGarageForDetails == null) return;

        // 1. Filter vehicles belonging to this garage
        List<Vehicle> parkedVehicles = gameState.getVehicles().stream()
            .filter(v -> v.getHomeGarageLocation() != null &&
                v.getHomeGarageLocation().equals(selectedGarageForDetails.getLocation()))
            .toList();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        float dW = 500f;
        float dH = Math.max(200f, 100f + parkedVehicles.size() * 40f);
        float dX = w / 2f - dW / 2f;
        float dY = h / 2f - dH / 2f;
        Rectangle rDialog = new Rectangle(dX, dY, dW, dH);

        rGarageDetailClose = new Rectangle(dX + dW - 30f, dY + dH - 30f, 24f, 24f);

        // Dim background overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.68f);
        sr.rect(0, 0, w, h);

        // Dialog Box
        sr.setColor(0.07f, 0.10f, 0.17f, 0.97f);
        sr.rect(rDialog.x, rDialog.y, rDialog.width, rDialog.height);

        // Top accent stripe
        sr.setColor(0.20f, 0.47f, 0.80f, 1f);
        sr.rect(rDialog.x, rDialog.y + rDialog.height - 4f, rDialog.width, 4f);

        // Close button
        sr.setColor(0.60f, 0.15f, 0.15f, 1f);
        sr.rect(rGarageDetailClose.x, rGarageDetailClose.y, rGarageDetailClose.width, rGarageDetailClose.height);
        sr.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Text rendering
        batch.begin();
        font.setColor(Color.WHITE);
        gl.setText(font, "X");
        font.draw(batch, "X", rGarageDetailClose.x + (rGarageDetailClose.width - gl.width) / 2f,
            rGarageDetailClose.y + (rGarageDetailClose.height + gl.height) / 2f);

        // Header
        font.getData().setScale(1.2f);
        font.setColor(new Color(0.70f, 0.80f, 1.0f, 1f));
        font.draw(batch, "Garage Details", dX + 16f, dY + dH - 16f);

        // Capacity indicator
        font.getData().setScale(1.0f);
        Color capColor = parkedVehicles.size() >= Constants.GARAGE_CAPACITY ? new Color(0.9f, 0.4f, 0.4f, 1f) : new Color(0.50f, 0.90f, 0.55f, 1f);
        font.setColor(capColor);
        font.draw(batch, "Capacity: " + parkedVehicles.size() + " / " + Constants.GARAGE_CAPACITY, dX + 16f, dY + dH - 40f);
        batch.end();

        garageDetailSellBtns.clear();
        float rowY = dY + dH - 85f;

        // Draw Vehicles
        for (int i = 0; i < parkedVehicles.size(); i++) {
            Vehicle v = parkedVehicles.get(i);

            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(i % 2 == 0 ? new Color(0.12f, 0.16f, 0.24f, 1f) : new Color(0.15f, 0.19f, 0.29f, 1f));
            sr.rect(dX + 16f, rowY, dW - 32f, 34f);
            sr.end();

            String displayName = GameController.VEHICLE_CATALOG.stream()
                .filter(e -> e.type() == v.getType())
                .map(GameController.VehicleCatalogEntry::displayName)
                .findFirst().orElse(v.getType().toString());

            batch.begin();
            font.setColor(Color.WHITE);
            font.draw(batch, displayName, dX + 24f, rowY + 22f);

            String status = v.isMaintaining() ? "Maintaining..." : (v.isReturningToGarage() ? "Returning..." : "On Route");
            font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
            font.draw(batch, status, dX + 160f, rowY + 22f);
            batch.end();

            Rectangle sellBtn = new Rectangle(dX + dW - 100f, rowY + 4f, 70f, 26f);
            garageDetailSellBtns.add(sellBtn);
            drawBtn(sellBtn, "Sell", new Color(0.65f, 0.25f, 0.25f, 1f));

            rowY -= 38f;
        }

        if (parkedVehicles.isEmpty()) {
            batch.begin();
            font.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
            font.draw(batch, "This garage is empty.", dX + 16f, rowY + 22f);
            batch.end();
        }

        font.getData().setScale(1.4f); // Reset scale

        // Input Handling
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mx = Gdx.input.getX();
            float my = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (rGarageDetailClose.contains(mx, my)) {
                showGarageDetailDialog = false;
                selectedGarageForDetails = null;
                return;
            }

            for (int i = 0; i < garageDetailSellBtns.size(); i++) {
                if (garageDetailSellBtns.get(i).contains(mx, my)) {
                    Vehicle vToSell = parkedVehicles.get(i);
                    long resale = vToSell.getResaleValue();
                    gameState.getEconomy().earn(resale);
                    gameState.removeVehicle(vToSell);
                    showFeedback("Vehicle sold for $" + resale, Color.GREEN);
                    return; // Will re-render next frame
                }
            }
        }
    }

    private int countMapStops() { return collectMapStops().size(); }

    private List<StopOverlay> collectMapStops() {
        List<StopOverlay> stops = new ArrayList<>();
        for (int x = 0; x < gameState.getMap().size.width; x++)
            for (int y = 0; y < gameState.getMap().size.height; y++) {
                Tile t = gameState.getMap().getTile(new TileCoord(x, y));
                if (t != null && t.getOverlay() instanceof StopOverlay s) stops.add(s);
            }
        return stops;
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
