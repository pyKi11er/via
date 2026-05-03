package com.via.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.via.VIA;
import com.via.controller.GameEngine;
import com.via.controller.MapInitializer;
import com.via.controller.SaveManager;
import com.via.model.Constants;
import com.via.model.GameState;
import com.via.model.vehicle.Vehicle;
import com.via.model.vehicle.VehiclePosition;

public class MainMenuScreen extends InputAdapter implements Screen {
    private final VIA game;

    // ── UI camera / rendering
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont buttonFont;
    private final BitmapFont smallFont;
    private final GlyphLayout glyphLayout;
    private final Texture texButton;
    private final Texture texPanel;

    // ── Background simulation
    private final GameState          bgState;
    private final GameEngine         bgEngine;
    private final WorldMapRenderer   bgRenderer;
    private final OrthographicCamera bgCamera;
    /** smoothed camera position in world pixels */
    private float bgCamX, bgCamY;
    /** which vehicle the camera is currently following (cycles when vehicle stops) */
    private Vehicle trackedVehicle = null;

    // MapInitializer only fills a 40×30 tile area regardless of Constants.MAP_WIDTH/HEIGHT
    private static final float MAP_PX_W   = 40 * (float) Constants.TILE_SIZE;
    private static final float MAP_PX_H   = 30 * (float) Constants.TILE_SIZE;

    // ── Dialog state
    private enum Dialog { NONE, NEW_GAME, LOAD_GAME, ABOUT, HELP, EXIT, SETTINGS }
    private Dialog dialog = Dialog.NONE;

    // ── Load game state
    private String[] saveFiles = new String[0];
    private final java.util.List<Rectangle> rSaveEntries = new java.util.ArrayList<>();

    private boolean showResolutionDropdown = false;
    private java.util.List<com.badlogic.gdx.Graphics.DisplayMode> cachedModes = new java.util.ArrayList<>();

    private boolean focusPlayer = true;
    private String  playerName  = "Player";
    private String  tycoonName  = "Tycoon";

    // ── Screen dimensions
    private float width;
    private float height;
    private float panelW;

    // ── Hit rectangles
    private Rectangle rLogo;
    private Rectangle rNew, rLoad, rAbout, rSettings, rHelp, rExit;
    private Rectangle rDialog, rField1, rField2, rDlgBtn1, rDlgBtn2, rDlgClose;

    // ── Button accent colors
    private static final Color COL_NEW      = new Color(0.18f, 0.75f, 0.40f, 1f);
    private static final Color COL_LOAD     = new Color(0.25f, 0.55f, 0.92f, 1f);
    private static final Color COL_SETTINGS = new Color(0.52f, 0.58f, 0.74f, 1f);
    private static final Color COL_HELP     = new Color(0.88f, 0.66f, 0.14f, 1f);
    private static final Color COL_EXIT     = new Color(0.82f, 0.22f, 0.22f, 1f);

    // ── Panel / dialog colors
    private static final Color PANEL_DARK   = new Color(0.04f, 0.07f, 0.13f, 0.94f);
    private static final Color PANEL_BORDER = new Color(0.22f, 0.35f, 0.62f, 0.70f);

    private static final Color DLG_HEADER_NEW      = new Color(0.12f, 0.50f, 0.28f, 1f);
    private static final Color DLG_HEADER_LOAD     = new Color(0.18f, 0.38f, 0.68f, 1f);
    private static final Color DLG_HEADER_ABOUT    = new Color(0.32f, 0.22f, 0.58f, 1f);
    private static final Color DLG_HEADER_HELP     = new Color(0.58f, 0.42f, 0.08f, 1f);
    private static final Color DLG_HEADER_SETTINGS = new Color(0.28f, 0.34f, 0.50f, 1f);
    private static final Color DLG_HEADER_EXIT     = new Color(0.55f, 0.14f, 0.14f, 1f);

    // ── Textures (logo only — button/panel kept for dialogs)
    private final Texture texLogo;

    //

    public MainMenuScreen(VIA game) {
        this.game = game;
        this.camera       = new OrthographicCamera();
        this.bgCamera     = new OrthographicCamera();
        this.shapeRenderer = new ShapeRenderer();
        this.glyphLayout  = new GlyphLayout();

        // Fonts
        boolean fontExists = Gdx.files.internal("fonts/arial.ttf").exists();
        if (fontExists) {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.minFilter = Texture.TextureFilter.Linear;
            p.magFilter = Texture.TextureFilter.Linear;

            p.size = 34;
            this.buttonFont = gen.generateFont(p);

            p.size = 22;
            this.smallFont = gen.generateFont(p);

            gen.dispose();
        } else {
            this.buttonFont = new BitmapFont();
            this.smallFont  = new BitmapFont();
        }

        // Textures used in UI
        this.texLogo   = new Texture("logo.png");
        this.texButton = new Texture("button.png");
        this.texPanel  = new Texture("panel.png");

        // Background simulation — initialize with a fresh game world
        this.bgState    = MapInitializer.createNewGame();
        this.bgEngine   = new GameEngine(bgState);
        this.bgEngine.getTimeController().setSpeed(com.via.model.enums.SpeedSetting.FAST);
        this.bgRenderer = new WorldMapRenderer(game.batch, bgCamera);

        // Start camera somewhere in the middle of the map
        this.bgCamX = MAP_PX_W * 0.45f;
        this.bgCamY = MAP_PX_H * 0.50f;
    }

    //

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int w, int h) {
        width  = w;
        height = h;
        panelW = width * 0.30f;

        // UI camera covers the full screen in screen-pixel space
        camera.setToOrtho(false, w, h);
        camera.update();

        // Background camera zooms in 2× so tiles appear larger and vehicles are visible
        bgCamera.setToOrtho(false, w * 0.55f, h * 0.55f);
        float halfVH = bgCamera.viewportHeight * 0.5f;
        bgCamY = Math.max(halfVH, Math.min(MAP_PX_H - halfVH, bgCamY));
        bgCamera.position.set(bgCamX, bgCamY, 0);
        bgCamera.update();

        float scale = h / 720f;
        buttonFont.getData().setScale(Math.max(scale * 1.05f, 0.80f));
        smallFont .getData().setScale(Math.max(scale * 0.85f, 0.70f));

        // Logo
        float logoW = panelW * 0.68f;
        float logoH = logoW * (texLogo.getHeight() / (float) texLogo.getWidth());
        float logoX = (panelW - logoW) * 0.5f;
        float logoY = height - height * 0.06f - logoH;
        rLogo = new Rectangle(logoX, logoY, logoW, logoH);

        // Buttons
        float btnW   = panelW * 0.86f;
        float btnH   = height * 0.078f;
        float btnX   = (panelW - btnW) * 0.5f;
        float btnGap = height * 0.013f;
        float firstBtnY = logoY - height * 0.11f - btnH;

        rNew      = new Rectangle(btnX, firstBtnY,                        btnW, btnH);
        rLoad     = new Rectangle(btnX, firstBtnY - (btnH + btnGap),      btnW, btnH);
        rSettings = new Rectangle(btnX, firstBtnY - (btnH + btnGap) * 2f, btnW, btnH);
        rHelp     = new Rectangle(btnX, firstBtnY - (btnH + btnGap) * 3f, btnW, btnH);
        rExit     = new Rectangle(btnX, firstBtnY - (btnH + btnGap) * 4f, btnW, btnH);

        rAbout = new Rectangle(0, height * 0.018f, panelW, height * 0.042f);

        // Dialog
        float dlgW = width  * 0.42f, dlgH = height * 0.52f;
        float dlgX = width  * 0.5f  - dlgW * 0.5f;
        float dlgY = height * 0.5f  - dlgH * 0.5f;
        rDialog = new Rectangle(dlgX, dlgY, dlgW, dlgH);

        float fW = dlgW * 0.86f, fH = dlgH * 0.13f;
        float fX = dlgX + (dlgW - fW) * 0.5f;
        rField1 = new Rectangle(fX, dlgY + dlgH * 0.57f, fW, fH);
        rField2 = new Rectangle(fX, dlgY + dlgH * 0.35f, fW, fH);

        float bW = dlgW * 0.32f, bH = dlgH * 0.15f, bY = dlgY + dlgH * 0.08f;
        rDlgBtn1  = new Rectangle(dlgX + dlgW * 0.11f,              bY, bW, bH);
        rDlgBtn2  = new Rectangle(dlgX + dlgW - dlgW * 0.11f - bW, bY, bW, bH);
        rDlgClose = new Rectangle(dlgX + dlgW * 0.5f - bW * 0.5f,  bY, bW, bH);
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        bgEngine.update();
        panBgCamera(delta);
        bgRenderer.render(bgState);

        // Restore UI camera on both renderers
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        game.batch.setProjectionMatrix(camera.combined);


        blend(() -> {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0f, 0.48f);
            shapeRenderer.rect(0, 0, width, height);
            shapeRenderer.end();
        });

        // Left sidebar panel
        blend(() -> {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(PANEL_DARK);
            shapeRenderer.rect(0, 0, panelW, height);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(PANEL_BORDER);
            shapeRenderer.line(panelW, 0, panelW, height);
            shapeRenderer.end();
        });

        // Logo
        game.batch.begin();
        game.batch.setColor(Color.WHITE);
        game.batch.draw(texLogo, rLogo.x, rLogo.y, rLogo.width, rLogo.height);
        game.batch.end();

        //  Subtitle + separator
        float subtitleY = rLogo.y - height * 0.022f;
        drawCentered(smallFont, "Mini Transport Tycoon", panelW * 0.5f, subtitleY,
                new Color(0.68f, 0.78f, 1.0f, 0.80f));

        float sepY = subtitleY - height * 0.030f;
        blend(() -> {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.22f, 0.35f, 0.62f, 0.45f);
            shapeRenderer.rect(panelW * 0.08f, sepY, panelW * 0.84f,
                    Math.max(1f, height * 0.0014f));
            shapeRenderer.end();
        });

        //  Menu buttons
        drawMenuButton(rNew,      "New Game",  COL_NEW);
        drawMenuButton(rLoad,     "Load Game", COL_LOAD);
        drawMenuButton(rSettings, "Settings",  COL_SETTINGS);
        drawMenuButton(rHelp,     "Help",      COL_HELP);
        drawMenuButton(rExit,     "Exit",      COL_EXIT);

        // About footer link
        boolean aboutHover = isHovered(rAbout);
        drawCentered(smallFont, "About", panelW * 0.5f,
                rAbout.y + rAbout.height * 0.78f,
                aboutHover ? new Color(0.80f, 0.86f, 1.0f, 0.95f)
                           : new Color(0.48f, 0.58f, 0.80f, 0.60f));

        // Dialog
        if (dialog != Dialog.NONE) renderDialog();
    }

    // Background camera follow

    private void panBgCamera(float delta) {
        // Switch to a different vehicle whenever the current one is stationary (origin == destination)
        VehiclePosition curPos = trackedVehicle != null ? trackedVehicle.getPosition() : null;
        boolean isMoving = curPos != null && !curPos.getOrigin().equals(curPos.getDestination());
        if (!isMoving) {
            Vehicle next = pickMovingVehicle();
            if (next != null) trackedVehicle = next;
        }

        if (trackedVehicle != null) {
            VehiclePosition pos = trackedVehicle.getPosition();
            float ts = Constants.TILE_SIZE;
            // Place the vehicle at the center of the visible area (right of a panel).
            // visibleCentreWorld is how far right in world-pixels the center of the
            // visible portion is, relative to the camera center.
            float vx = lerp(pos.getOrigin().x * ts,
                            pos.getDestination().x * ts, pos.getProgress()) + ts * 0.5f;
            float vy = lerp(pos.getOrigin().y * ts,
                            pos.getDestination().y * ts, pos.getProgress()) + ts * 0.5f;

            float panelFraction       = panelW / width;
            float visibleCentreFrac   = panelFraction + (1f - panelFraction) * 0.5f; // e.g. 0.65
            bgCamX = vx - bgCamera.viewportWidth * (visibleCentreFrac - 0.5f);
            bgCamY = vy;
        }

        float halfVH = bgCamera.viewportHeight * 0.5f;
        bgCamY = Math.max(halfVH, Math.min(MAP_PX_H - halfVH, bgCamY));

        bgCamera.position.set(bgCamX, bgCamY, 0);
        bgCamera.update();
    }

    /** Returns a vehicle actively moving between tiles (origin != destination), or null. */
    private Vehicle pickMovingVehicle() {
        for (Vehicle v : bgState.getVehicles()) {
            VehiclePosition p = v.getPosition();
            if (p != null && !p.getOrigin().equals(p.getDestination())) return v;
        }
        return null;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    // Drawing helpers

    /** Sidebar button with a colored left-accent bar. */
    private void drawMenuButton(Rectangle r, String label, Color accent) {
        boolean hover  = isHovered(r);
        float accentW  = r.width * 0.022f;

        blend(() -> {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(hover
                    ? new Color(0.14f, 0.20f, 0.34f, 0.92f)
                    : new Color(0.07f, 0.11f, 0.19f, 0.80f));
            shapeRenderer.rect(r.x, r.y, r.width, r.height);

            Color bar = hover
                    ? new Color(Math.min(accent.r + 0.18f, 1f),
                                Math.min(accent.g + 0.18f, 1f),
                                Math.min(accent.b + 0.18f, 1f), 1f)
                    : accent;
            shapeRenderer.setColor(bar);
            shapeRenderer.rect(r.x, r.y, accentW, r.height);
            shapeRenderer.end();
        });

        drawShadow(buttonFont, label,
                r.x + accentW + r.width * 0.06f,
                r.y + r.height * 0.64f,
                Color.WHITE);
    }

    /** Classic texture button used inside dialogs. */
    private void drawButton(Rectangle r, String label) {
        boolean hover = isHovered(r);
        game.batch.begin();
        game.batch.setColor(hover ? new Color(0.82f, 0.82f, 0.82f, 1f) : Color.WHITE);
        game.batch.draw(texButton, r.x, r.y, r.width, r.height);
        game.batch.end();

        glyphLayout.setText(buttonFont, label);
        float tx = r.x + (r.width  - glyphLayout.width)  * 0.5f;
        float ty = r.y + (r.height + glyphLayout.height) * 0.5f;
        drawShadow(buttonFont, label, tx, ty, Color.WHITE);
    }

    // Dialog rendering

    private void renderDialog() {
        blend(() -> {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0f, 0.68f);
            shapeRenderer.rect(0f, 0f, width, height);
            shapeRenderer.end();
        });

        blend(() -> {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.06f, 0.09f, 0.16f, 0.97f);
            shapeRenderer.rect(rDialog.x, rDialog.y, rDialog.width, rDialog.height);
            shapeRenderer.end();
        });

        game.batch.begin();
        game.batch.setColor(1f, 1f, 1f, 0.18f);
        game.batch.draw(texPanel, rDialog.x, rDialog.y, rDialog.width, rDialog.height);
        game.batch.end();

        // Coloured header bar
        float headerH = rDialog.height * 0.13f;
        blend(() -> {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(dialogHeaderColor());
            shapeRenderer.rect(rDialog.x, rDialog.y + rDialog.height - headerH,
                    rDialog.width, headerH);
            shapeRenderer.end();
        });

        blend(() -> {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.28f, 0.40f, 0.70f, 0.55f);
            shapeRenderer.rect(rDialog.x, rDialog.y, rDialog.width, rDialog.height);
            shapeRenderer.end();
        });

        float cx = rDialog.x + rDialog.width * 0.5f;
        float hTY = rDialog.y + rDialog.height - headerH * 0.28f;

        switch (dialog) {
            case NEW_GAME:
                drawCentered(buttonFont, "New Game", cx, hTY, Color.WHITE);
                drawLeft(smallFont, "Player name:", rField1.x,
                        rField1.y + rField1.height + rDialog.height * 0.06f,
                        new Color(0.78f, 0.88f, 1f, 1f));
                drawLeft(smallFont, "Tycoon name:", rField2.x,
                        rField2.y + rField2.height + rDialog.height * 0.06f,
                        new Color(0.78f, 0.88f, 1f, 1f));
                drawField(rField1, playerName, focusPlayer);
                drawField(rField2, tycoonName, !focusPlayer);
                drawButton(rDlgBtn1, "Start");
                drawButton(rDlgBtn2, "Cancel");
                break;

            case LOAD_GAME:
                drawCentered(buttonFont, "Load Game", cx, hTY, Color.WHITE);
                rSaveEntries.clear();
                if (saveFiles.length == 0) {
                    drawCentered(smallFont, "No save files found.", cx,
                            rDialog.y + rDialog.height * 0.56f, new Color(0.90f, 0.93f, 1f, 1f));
                } else {
                    float entryH = rDialog.height * 0.10f;
                    float entryW = rDialog.width * 0.80f;
                    float entryX = rDialog.x + (rDialog.width - entryW) * 0.5f;
                    float startY = rDialog.y + rDialog.height * 0.76f;
                    for (int i = 0; i < saveFiles.length; i++) {
                        Rectangle re = new Rectangle(entryX, startY - i * (entryH + rDialog.height * 0.02f), entryW, entryH);
                        rSaveEntries.add(re);
                        drawButton(re, saveFiles[i]);
                    }
                }
                drawButton(rDlgClose, "Close");
                break;

            case ABOUT:
                drawCentered(buttonFont, "About", cx, hTY, Color.WHITE);
                drawCentered(smallFont, "VIA - Mini Transport Tycoon", cx,
                        rDialog.y + rDialog.height * 0.72f, new Color(0.90f, 0.93f, 1f, 1f));
                drawCentered(smallFont, "Developed by: Vuk, Hayk, Valerii, Andrii", cx,
                        rDialog.y + rDialog.height * 0.58f, new Color(0.90f, 0.93f, 1f, 1f));
                drawCentered(smallFont, "ELTE 2025/2026", cx,
                        rDialog.y + rDialog.height * 0.44f, new Color(0.90f, 0.93f, 1f, 1f));
                drawButton(rDlgClose, "Close");
                break;

            case HELP:
                drawCentered(buttonFont, "Help", cx, hTY, Color.WHITE);
                String[] tips = {
                        "Build roads between cities.",
                        "Buy vehicles and assign routes.",
                        "Deliver goods to earn money.",
                        "Don't go bankrupt!"
                };
                for (int i = 0; i < tips.length; i++) {
                    drawCentered(smallFont, tips[i], cx,
                            rDialog.y + rDialog.height * 0.72f - i * rDialog.height * 0.12f,
                            new Color(0.90f, 0.93f, 1f, 1f));
                }
                drawButton(rDlgClose, "Close");
                break;

            case SETTINGS:
                drawCentered(buttonFont, "Settings", cx, hTY, Color.WHITE);
                drawCentered(smallFont, "Change Resolution", cx,
                        rDialog.y + rDialog.height * 0.76f, Color.WHITE);

                Rectangle rDrop = new Rectangle(rField1.x,
                        rField1.y + rField1.height * 0.5f, rField1.width, rField1.height);
                drawButton(rDrop, Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight());

                if (showResolutionDropdown) {
                    if (cachedModes.isEmpty()) {
                        com.badlogic.gdx.Graphics.DisplayMode cur = Gdx.graphics.getDisplayMode();
                        int[] basicWidths = {1280, 1366, 1600, 1920, 2560, 3840};
                        for (com.badlogic.gdx.Graphics.DisplayMode m : Gdx.graphics.getDisplayModes()) {
                            if (m.refreshRate != cur.refreshRate) continue;
                            boolean ok = false;
                            for (int bw : basicWidths) if (m.width == bw) { ok = true; break; }
                            if (!ok) continue;
                            boolean dup = false;
                            for (com.badlogic.gdx.Graphics.DisplayMode e : cachedModes)
                                if (e.width == m.width && e.height == m.height) { dup = true; break; }
                            if (!dup) cachedModes.add(m);
                        }
                        cachedModes.sort((a, b) -> a.width != b.width
                                ? Integer.compare(a.width, b.width)
                                : Integer.compare(a.height, b.height));
                    }
                    float itemH = rDrop.height * 0.8f, listY = rDrop.y - itemH;
                    for (int i = 0; i < cachedModes.size(); i++) {
                        com.badlogic.gdx.Graphics.DisplayMode m = cachedModes.get(i);
                        drawButton(new Rectangle(rDrop.x, listY - i * itemH, rDrop.width, itemH),
                                m.width + "x" + m.height);
                    }
                }
                drawButton(rDlgClose, "Back");
                break;

            case EXIT:
                drawCentered(buttonFont, "Exit", cx, hTY, Color.WHITE);
                drawCentered(smallFont, "Are you sure you want to quit?", cx,
                        rDialog.y + rDialog.height * 0.58f, new Color(0.92f, 0.92f, 1f, 1f));
                drawButton(rDlgBtn1, "Yes, Exit");
                drawButton(rDlgBtn2, "Cancel");
                break;

            default: break;
        }
    }

    private Color dialogHeaderColor() {
        switch (dialog) {
            case NEW_GAME:  return DLG_HEADER_NEW;
            case LOAD_GAME: return DLG_HEADER_LOAD;
            case ABOUT:     return DLG_HEADER_ABOUT;
            case HELP:      return DLG_HEADER_HELP;
            case SETTINGS:  return DLG_HEADER_SETTINGS;
            case EXIT:      return DLG_HEADER_EXIT;
            default:        return DLG_HEADER_SETTINGS;
        }
    }

    private void drawField(Rectangle r, String text, boolean focused) {
        blend(() -> {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.05f, 0.08f, 0.14f, 1f);
            shapeRenderer.rect(r.x, r.y, r.width, r.height);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(focused
                    ? new Color(0.30f, 0.66f, 1f, 1f)
                    : new Color(0.26f, 0.34f, 0.52f, 1f));
            shapeRenderer.rect(r.x, r.y, r.width, r.height);
            shapeRenderer.end();
        });
        drawShadow(smallFont, text + (focused ? "|" : ""),
                r.x + r.width * 0.03f, r.y + r.height * 0.70f, Color.WHITE);
    }

    private void drawCentered(BitmapFont font, String text, float cx, float y, Color color) {
        glyphLayout.setText(font, text);
        drawShadow(font, text, cx - glyphLayout.width * 0.5f, y, color);
    }

    private void drawLeft(BitmapFont font, String text, float x, float y, Color color) {
        drawShadow(font, text, x, y, color);
    }

    private void drawShadow(BitmapFont font, String text, float x, float y, Color color) {
        float sx = Math.max(width  * 0.0015f, 1.5f);
        float sy = Math.max(height * 0.0025f, 1.5f);
        game.batch.begin();
        font.setColor(0f, 0f, 0f, 0.85f);
        font.draw(game.batch, text, x + sx, y - sy);
        font.setColor(color);
        font.draw(game.batch, text, x, y);
        game.batch.end();
    }

    private void blend(Runnable draw) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        draw.run();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private boolean isHovered(Rectangle r) {
        return r != null && r.contains(Gdx.input.getX(), height - Gdx.input.getY());
    }

    // Input

    @Override
    public boolean keyDown(int keycode) {
        if (dialog != Dialog.NEW_GAME) return false;
        if (keycode == Input.Keys.TAB)       { focusPlayer = !focusPlayer; return true; }
        if (keycode == Input.Keys.ENTER)     { confirmStart(); return true; }
        if (keycode == Input.Keys.ESCAPE)    { dialog = Dialog.NONE; return true; }
        if (keycode == Input.Keys.BACKSPACE) {
            if (focusPlayer  && !playerName.isEmpty()) { playerName = playerName.substring(0, playerName.length() - 1); return true; }
            if (!focusPlayer && !tycoonName.isEmpty()) { tycoonName = tycoonName.substring(0, tycoonName.length() - 1); return true; }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        if (dialog != Dialog.NEW_GAME) return false;
        if (!Character.isLetterOrDigit(c) && c != ' ') return false;
        if (focusPlayer  && playerName.length() < 24) { playerName += c; return true; }
        if (!focusPlayer && tycoonName.length() < 24) { tycoonName += c; return true; }
        return false;
    }

    @Override
    public boolean touchDown(int sx, int sy, int pointer, int btn) {
        if (btn != Input.Buttons.LEFT) return false;
        float mx = sx, my = height - sy;

        if (dialog == Dialog.NONE) {
            if (rNew     .contains(mx, my)) { dialog = Dialog.NEW_GAME;  focusPlayer = true; return true; }
            if (rLoad    .contains(mx, my)) { dialog = Dialog.LOAD_GAME; saveFiles = SaveManager.listSaves(); return true; }
            if (rSettings.contains(mx, my)) { dialog = Dialog.SETTINGS;                      return true; }
            if (rHelp    .contains(mx, my)) { dialog = Dialog.HELP;                          return true; }
            if (rExit    .contains(mx, my)) { dialog = Dialog.EXIT;                          return true; }
            if (rAbout   .contains(mx, my)) { dialog = Dialog.ABOUT;                         return true; }
            return false;
        }

        if (!rDialog.contains(mx, my)) { dialog = Dialog.NONE; return true; }

        if (dialog == Dialog.NEW_GAME) {
            if (rField1 .contains(mx, my)) { focusPlayer = true;       return true; }
            if (rField2 .contains(mx, my)) { focusPlayer = false;      return true; }
            if (rDlgBtn1.contains(mx, my)) { confirmStart();           return true; }
            if (rDlgBtn2.contains(mx, my)) { dialog = Dialog.NONE;     return true; }
        } else if (dialog == Dialog.SETTINGS) {
            Rectangle rDrop = new Rectangle(rField1.x,
                    rField1.y + rField1.height * 0.5f, rField1.width, rField1.height);
            if (showResolutionDropdown) {
                float itemH = rDrop.height * 0.8f, listY = rDrop.y - itemH;
                for (int i = 0; i < cachedModes.size(); i++) {
                    if (new Rectangle(rDrop.x, listY - i * itemH, rDrop.width, itemH).contains(mx, my)) {
                        Gdx.graphics.setFullscreenMode(cachedModes.get(i));
                        showResolutionDropdown = false;
                        return true;
                    }
                }
                showResolutionDropdown = false;
                return true;
            }
            if (rDrop    .contains(mx, my)) { showResolutionDropdown = true;                        return true; }
            if (rDlgClose.contains(mx, my)) { dialog = Dialog.NONE; showResolutionDropdown = false; return true; }
        } else if (dialog == Dialog.EXIT) {
            if (rDlgBtn1.contains(mx, my)) { Gdx.app.exit();       return true; }
            if (rDlgBtn2.contains(mx, my)) { dialog = Dialog.NONE; return true; }
        } else if (dialog == Dialog.LOAD_GAME) {
            for (int i = 0; i < rSaveEntries.size(); i++) {
                if (rSaveEntries.get(i).contains(mx, my)) {
                    try {
                        com.via.model.GameState loaded = SaveManager.loadGame(saveFiles[i]);
                        dialog = Dialog.NONE;
                        game.setScreen(new GameScreen(game, loaded));
                    } catch (Exception e) {
                        Gdx.app.log("VIA", "Load failed: " + e.getMessage());
                    }
                    return true;
                }
            }
            if (rDlgClose.contains(mx, my)) { dialog = Dialog.NONE; return true; }
        } else {
            if (rDlgClose.contains(mx, my)) { dialog = Dialog.NONE; return true; }
        }
        return true;
    }

    private void confirmStart() {
        if (playerName.trim().isEmpty() || tycoonName.trim().isEmpty()) return;
        Gdx.app.log("VIA", "Start: " + playerName);
        dialog = Dialog.NONE;
        game.setScreen(new GameScreen(game, MapInitializer.createNewGame()));
    }

    // Screen lifecycle

    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == this)
            Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        buttonFont.dispose();
        smallFont.dispose();
        texLogo.dispose();
        texButton.dispose();
        texPanel.dispose();
        bgRenderer.dispose();
    }
}
