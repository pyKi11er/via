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
import com.via.controller.MapInitializer;
import com.via.view.GameScreen;

public class MainMenuScreen extends InputAdapter implements Screen {
    private final VIA game;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont buttonFont;
    private final BitmapFont smallFont;
    private final GlyphLayout glyphLayout;

    private final Texture texBg;
    private final Texture texLogo;
    private final Texture texButton;
    private final Texture texPanel;

    private enum Dialog { NONE, NEW_GAME, LOAD_GAME, ABOUT, HELP, EXIT, SETTINGS }
    private Dialog dialog = Dialog.NONE;

    private boolean showResolutionDropdown = false;
    private java.util.List<com.badlogic.gdx.Graphics.DisplayMode> cachedModes = new java.util.ArrayList<>();

    private boolean focusPlayer = true;
    private String playerName = "";
    private String tycoonName = "";

    private float width;
    private float height;

    private Rectangle rLogo;
    private Rectangle rNew;
    private Rectangle rLoad;
    private Rectangle rAbout;
    private Rectangle rSettings;
    private Rectangle rHelp;
    private Rectangle rExit;

    private Rectangle rDialog;
    private Rectangle rField1;
    private Rectangle rField2;
    private Rectangle rDlgBtn1;
    private Rectangle rDlgBtn2;
    private Rectangle rDlgClose;

    private static final Color BLUE = new Color(0.23f, 0.47f, 0.88f, 1f);
    private static final Color GREEN = new Color(0.20f, 0.62f, 0.30f, 1f);
    private static final Color RED = new Color(0.76f, 0.20f, 0.20f, 1f);
    private static final Color GRAY = new Color(0.40f, 0.42f, 0.50f, 1f);
    private static final Color AMBER = new Color(0.72f, 0.55f, 0.20f, 1f);

    public MainMenuScreen(VIA game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.shapeRenderer = new ShapeRenderer();

        // Use default font if custom font is missing
        boolean fontExists = Gdx.files.internal("fonts/arial.ttf").exists();
        if (fontExists) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

            parameter.size = 36;
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;
            this.buttonFont = generator.generateFont(parameter);

            parameter.size = 24;
            this.smallFont = generator.generateFont(parameter);

            generator.dispose();
        } else {
            this.buttonFont = new BitmapFont();
            this.smallFont = new BitmapFont();
            System.out.println("[DEBUG_LOG] Font 'fonts/arial.ttf' not found, using default LibGDX font.");
        }

        this.glyphLayout = new GlyphLayout();

        this.texBg = new Texture("background.jpeg");
        this.texLogo = new Texture("logo.png");
        this.texButton = new Texture("button.png");
        this.texPanel = new Texture("panel.png");
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int w, int h) {
        width = w;
        height = h;
        camera.setToOrtho(false, w, h);
        camera.update();

        float scale = h / 720f;
        buttonFont.getData().setScale(Math.max(scale * 1.2f, 1.0f));
        smallFont.getData().setScale(Math.max(scale * 0.9f, 0.8f));

        // Wide Minecraft-style buttons
        float btnW = width * 0.40f;
        float btnH = height * 0.08f;
        float gap = 0;
        float smallW = (btnW - gap * 2f) / 3f;
        float smallH = btnH;

        float logoW = width * 0.40f;
        float logoH = logoW * (texLogo.getHeight() / (float) texLogo.getWidth());
        float logoGap = height * 0.04f;

        float stackH = logoH + logoGap + btnH + gap + btnH + gap + smallH + gap + btnH;
        float stackY = height * 0.45f - stackH * 0.5f;

        float logoX = width * 0.5f - logoW * 0.5f;
        float logoY = stackY + stackH - logoH;
        rLogo = new Rectangle(logoX, logoY, logoW, logoH);

        float mainX = width * 0.5f - btnW * 0.5f;
        float y1 = logoY - logoGap - btnH;
        rNew = new Rectangle(mainX, y1, btnW, btnH);

        float y2 = y1 - gap - btnH;
        rLoad = new Rectangle(mainX, y2, btnW, btnH);

        float y3 = y2 - gap - smallH;
        float smallX = width * 0.5f - btnW * 0.5f;
        rAbout = new Rectangle(smallX, y3, smallW, smallH);
        rSettings = new Rectangle(smallX + smallW + gap, y3, smallW, smallH);
        rHelp = new Rectangle(smallX + (smallW + gap) * 2f, y3, smallW, smallH);

        float y4 = y3 - gap - btnH;
        rExit = new Rectangle(mainX, y4, btnW, btnH);

        float dlgW = width * 0.40f;
        float dlgH = height * 0.50f;
        float dlgX = width * 0.5f - dlgW * 0.5f;
        float dlgY = height * 0.5f - dlgH * 0.5f;
        rDialog = new Rectangle(dlgX, dlgY, dlgW, dlgH);

        float fW = dlgW * 0.86f;
        float fH = dlgH * 0.13f;
        float fX = dlgX + (dlgW - fW) * 0.5f;
        rField1 = new Rectangle(fX, dlgY + dlgH * 0.57f, fW, fH);
        rField2 = new Rectangle(fX, dlgY + dlgH * 0.35f, fW, fH);

        float bW = dlgW * 0.32f;
        float bH = dlgH * 0.16f;
        float bY = dlgY + dlgH * 0.08f;
        rDlgBtn1 = new Rectangle(dlgX + dlgW * 0.11f, bY, bW, bH);
        rDlgBtn2 = new Rectangle(dlgX + dlgW - dlgW * 0.11f - bW, bY, bW, bH);
        rDlgClose = new Rectangle(dlgX + dlgW * 0.5f - bW * 0.5f, bY, bW, bH);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.setColor(Color.WHITE);
        game.batch.draw(texBg, 0f, 0f, width, height);
        game.batch.draw(texLogo, rLogo.x, rLogo.y, rLogo.width, rLogo.height);
        game.batch.end();

        drawButton(rNew, "New Game");
        drawButton(rLoad, "Load Game");
        drawButton(rAbout, "About");
        drawButton(rSettings, "Settings");
        drawButton(rHelp, "Help");
        drawButton(rExit, "Exit");

        if (dialog != Dialog.NONE) {
            renderDialog();
        }
    }

    private void renderDialog() {
        blend(() -> {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0f, 0.70f);
            shapeRenderer.rect(0f, 0f, width, height);
            shapeRenderer.setColor(0.06f, 0.09f, 0.16f, 0.97f);
            shapeRenderer.rect(rDialog.x, rDialog.y, rDialog.width, rDialog.height);
            shapeRenderer.end();
        });

        game.batch.begin();
        game.batch.setColor(1f, 1f, 1f, 0.25f);
        game.batch.draw(texPanel, rDialog.x, rDialog.y, rDialog.width, rDialog.height);
        game.batch.end();

        float cx = rDialog.x + rDialog.width * 0.5f;

        switch (dialog) {
            case NEW_GAME:
                drawCentered(smallFont, "New Game", cx, rDialog.y + rDialog.height * 0.92f, Color.WHITE);
                drawLeft(smallFont, "Player name:", rField1.x, rField1.y + rField1.height + rDialog.height * 0.07f, new Color(0.78f, 0.86f, 1f, 1f));
                drawLeft(smallFont, "Tycoon name:", rField2.x, rField2.y + rField2.height + rDialog.height * 0.07f, new Color(0.78f, 0.86f, 1f, 1f));
                drawField(rField1, playerName, focusPlayer);
                drawField(rField2, tycoonName, !focusPlayer);
                drawButton(rDlgBtn1, "Start");
                drawButton(rDlgBtn2, "Cancel");
                break;

            case LOAD_GAME:
                drawCentered(smallFont, "Load Game", cx, rDialog.y + rDialog.height * 0.92f, Color.WHITE);
                drawCentered(smallFont, "No save files found.", cx, rDialog.y + rDialog.height * 0.62f, new Color(0.90f, 0.93f, 1f, 1f));
                drawButton(rDlgClose, "Close");
                break;

            case ABOUT:
                drawCentered(smallFont, "About Us", cx, rDialog.y + rDialog.height * 0.92f, Color.WHITE);
                drawCentered(smallFont, "VIA - Mini Transport Tycoon", cx, rDialog.y + rDialog.height * 0.72f, new Color(0.90f, 0.93f, 1f, 1f));
                drawCentered(smallFont, "Developed by: Vuk, Hayk, Valerii, Andrii", cx, rDialog.y + rDialog.height * 0.58f, new Color(0.90f, 0.93f, 1f, 1f));
                drawCentered(smallFont, "ELTE 2025/2026", cx, rDialog.y + rDialog.height * 0.44f, new Color(0.90f, 0.93f, 1f, 1f));
                drawButton(rDlgClose, "Close");
                break;

            case HELP:
                drawCentered(smallFont, "Help", cx, rDialog.y + rDialog.height * 0.92f, Color.WHITE);
                String[] tips = {
                        "Build roads between cities.",
                        "Buy vehicles and assign routes.",
                        "Deliver goods to earn money.",
                        "Don't go bankrupt!"
                };
                for (int i = 0; i < tips.length; i++) {
                    drawCentered(smallFont, tips[i], cx, rDialog.y + rDialog.height * 0.74f - i * rDialog.height * 0.12f, new Color(0.90f, 0.93f, 1f, 1f));
                }
                drawButton(rDlgClose, "Close");
                break;

            case SETTINGS:
                drawCentered(smallFont, "Settings", cx, rDialog.y + rDialog.height * 0.92f, Color.WHITE);
                String res = Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight();
                drawCentered(smallFont, "Change Resolution", cx, rDialog.y + rDialog.height * 0.78f, Color.WHITE);

                Rectangle rDrop = new Rectangle(rField1.x, rField1.y + rField1.height * 0.5f, rField1.width, rField1.height);
                drawButton(rDrop, res);

                if (showResolutionDropdown) {
                    if (cachedModes.isEmpty()) {
                        com.badlogic.gdx.Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
                        int[] basicWidths = {1280, 1366, 1600, 1920, 2560, 3840};
                        for (com.badlogic.gdx.Graphics.DisplayMode m : Gdx.graphics.getDisplayModes()) {
                            if (m.refreshRate == currentMode.refreshRate) {
                                boolean isBasic = false;
                                for (int bw : basicWidths) {
                                    if (m.width == bw) {
                                        isBasic = true;
                                        break;
                                    }
                                }
                                if (!isBasic) continue;

                                boolean duplicate = false;
                                for (com.badlogic.gdx.Graphics.DisplayMode existing : cachedModes) {
                                    if (existing.width == m.width && existing.height == m.height) {
                                        duplicate = true;
                                        break;
                                    }
                                }
                                if (!duplicate) cachedModes.add(m);
                            }
                        }
                        cachedModes.sort((a, b) -> a.width != b.width ? Integer.compare(a.width, b.width) : Integer.compare(a.height, b.height));
                    }

                    float itemH = rDrop.height * 0.8f;
                    float listY = rDrop.y - itemH; // Start right below the dropdown button

                    for (int i = 0; i < cachedModes.size(); i++) {
                        com.badlogic.gdx.Graphics.DisplayMode m = cachedModes.get(i);
                        float y = listY - i * itemH;
                        Rectangle rItem = new Rectangle(rDrop.x, y, rDrop.width, itemH);
                        drawButton(rItem, m.width + "x" + m.height);
                    }
                }

                drawButton(rDlgClose, "Back");
                break;

            case EXIT:
                drawCentered(smallFont, "Exit", cx, rDialog.y + rDialog.height * 0.92f, Color.WHITE);
                drawCentered(smallFont, "Are you sure?", cx, rDialog.y + rDialog.height * 0.62f, new Color(0.92f, 0.92f, 1f, 1f));
                drawButton(rDlgBtn1, "Yes Exit");
                drawButton(rDlgBtn2, "No");
                break;

            default:
                break;
        }
    }

    private void drawButton(Rectangle r, String label) {
        boolean hover = isHovered(r);
        Color drawColor = hover ? new Color(0.85f, 0.85f, 0.85f, 1f) : Color.WHITE;

        game.batch.begin();
        game.batch.setColor(drawColor);
        game.batch.draw(texButton, r.x, r.y, r.width, r.height);
        game.batch.end();

        glyphLayout.setText(buttonFont, label);
        float tx = r.x + (r.width - glyphLayout.width) * 0.5f;
        float ty = r.y + (r.height + glyphLayout.height) * 0.5f;
        drawShadow(buttonFont, label, tx, ty, Color.WHITE);
    }

    private void drawField(Rectangle r, String text, boolean focused) {
        blend(() -> {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.05f, 0.08f, 0.14f, 1f);
            shapeRenderer.rect(r.x, r.y, r.width, r.height);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(focused ? new Color(0.30f, 0.66f, 1f, 1f) : new Color(0.26f, 0.34f, 0.52f, 1f));
            shapeRenderer.rect(r.x, r.y, r.width, r.height);
            shapeRenderer.end();
        });

        drawShadow(smallFont, text + (focused ? "|" : ""), r.x + r.width * 0.03f, r.y + r.height * 0.70f, Color.WHITE);
    }

    private void drawCentered(BitmapFont font, String text, float cx, float y, Color color) {
        glyphLayout.setText(font, text);
        drawShadow(font, text, cx - glyphLayout.width * 0.5f, y, color);
    }

    private void drawLeft(BitmapFont font, String text, float x, float y, Color color) {
        drawShadow(font, text, x, y, color);
    }

    private void drawShadow(BitmapFont font, String text, float x, float y, Color color) {
        float sx = Math.max(width * 0.0015f, 2f);
        float sy = Math.max(height * 0.0025f, 2f);

        game.batch.begin();
        font.setColor(0f, 0f, 0f, 0.88f);
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

    private Color brighten(Color c, float amount) {
        return new Color(Math.min(c.r + amount, 1f), Math.min(c.g + amount, 1f), Math.min(c.b + amount, 1f), 1f);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (dialog != Dialog.NEW_GAME) {
            return false;
        }

        if (keycode == Input.Keys.TAB) {
            focusPlayer = !focusPlayer;
            return true;
        }
        if (keycode == Input.Keys.ENTER) {
            confirmStart();
            return true;
        }
        if (keycode == Input.Keys.BACKSPACE) {
            if (focusPlayer && !playerName.isEmpty()) {
                playerName = playerName.substring(0, playerName.length() - 1);
                return true;
            }
            if (!focusPlayer && !tycoonName.isEmpty()) {
                tycoonName = tycoonName.substring(0, tycoonName.length() - 1);
                return true;
            }
        }
        if (keycode == Input.Keys.ESCAPE) {
            dialog = Dialog.NONE;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        if (dialog != Dialog.NEW_GAME) {
            return false;
        }
        if (!Character.isLetterOrDigit(c) && c != ' ') {
            return false;
        }
        if (focusPlayer && playerName.length() < 24) {
            playerName += c;
            return true;
        }
        if (!focusPlayer && tycoonName.length() < 24) {
            tycoonName += c;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(int sx, int sy, int pointer, int btn) {
        if (btn != Input.Buttons.LEFT) {
            return false;
        }
        float mx = sx;
        float my = height - sy;

        if (dialog == Dialog.NONE) {
            if (rNew.contains(mx, my)) {
                dialog = Dialog.NEW_GAME;
                focusPlayer = true;
                playerName = "";
                tycoonName = "";
                return true;
            }
            if (rLoad.contains(mx, my)) {
                dialog = Dialog.LOAD_GAME;
                return true;
            }
            if (rAbout.contains(mx, my)) {
                dialog = Dialog.ABOUT;
                return true;
            }
            if (rSettings.contains(mx, my)) {
                dialog = Dialog.SETTINGS;
                return true;
            }
            if (rHelp.contains(mx, my)) {
                dialog = Dialog.HELP;
                return true;
            }
            if (rExit.contains(mx, my)) {
                dialog = Dialog.EXIT;
                return true;
            }
            return false;
        }

        if (!rDialog.contains(mx, my)) {
            dialog = Dialog.NONE;
            return true;
        }

        if (dialog == Dialog.NEW_GAME) {
            if (rField1.contains(mx, my)) {
                focusPlayer = true;
                return true;
            }
            if (rField2.contains(mx, my)) {
                focusPlayer = false;
                return true;
            }
            if (rDlgBtn1.contains(mx, my)) {
                confirmStart();
                return true;
            }
            if (rDlgBtn2.contains(mx, my)) {
                dialog = Dialog.NONE;
                return true;
            }
        } else if (dialog == Dialog.SETTINGS) {
            Rectangle rDrop = new Rectangle(rField1.x, rField1.y + rField1.height * 0.5f, rField1.width, rField1.height);
            if (showResolutionDropdown) {
                float itemH = rDrop.height * 0.8f;
                float listY = rDrop.y - itemH;

                for (int i = 0; i < cachedModes.size(); i++) {
                    float y = listY - i * itemH;
                    Rectangle rItem = new Rectangle(rDrop.x, y, rDrop.width, itemH);
                    if (rItem.contains(mx, my)) {
                        Gdx.graphics.setFullscreenMode(cachedModes.get(i));
                        showResolutionDropdown = false;
                        return true;
                    }
                }
                showResolutionDropdown = false;
                return true;
            }

            if (rDrop.contains(mx, my)) {
                showResolutionDropdown = true;
                return true;
            }

            if (rDlgClose.contains(mx, my)) {
                dialog = Dialog.NONE;
                showResolutionDropdown = false;
                return true;
            }
        } else if (dialog == Dialog.EXIT) {
            if (rDlgBtn1.contains(mx, my)) {
                Gdx.app.exit();
                return true;
            }
            if (rDlgBtn2.contains(mx, my)) {
                dialog = Dialog.NONE;
                return true;
            }
        } else {
            if (rDlgClose.contains(mx, my)) {
                dialog = Dialog.NONE;
                return true;
            }
        }

        return true;
    }

    private void confirmStart() {
        if (playerName.trim().isEmpty() || tycoonName.trim().isEmpty()) {
            return;
        }
        Gdx.app.log("VIA", "Start: " + playerName);
        dialog = Dialog.NONE;
        game.setScreen(new GameScreen(MapInitializer.createNewGame()));
    }

    private void changeResolution(int direction) {
        com.badlogic.gdx.Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
        com.badlogic.gdx.Graphics.DisplayMode[] modes = Gdx.graphics.getDisplayModes();

        // Filter and sort modes to make cycling predictable (optional but good)
        java.util.List<com.badlogic.gdx.Graphics.DisplayMode> filtered = new java.util.ArrayList<>();
        for(com.badlogic.gdx.Graphics.DisplayMode m : modes) {
            if (m.refreshRate == currentMode.refreshRate) {
                filtered.add(m);
            }
        }

        if (filtered.isEmpty()) filtered.addAll(java.util.Arrays.asList(modes));

        filtered.sort((a, b) -> {
            if (a.width != b.width) return Integer.compare(a.width, b.width);
            return Integer.compare(a.height, b.height);
        });

        int index = -1;
        for (int i = 0; i < filtered.size(); i++) {
            if (filtered.get(i).width == Gdx.graphics.getWidth() && filtered.get(i).height == Gdx.graphics.getHeight()) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            int nextIndex = (index + direction + filtered.size()) % filtered.size();
            com.badlogic.gdx.Graphics.DisplayMode target = filtered.get(nextIndex);
            Gdx.graphics.setFullscreenMode(target);
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == this) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        buttonFont.dispose();
        smallFont.dispose();
        texBg.dispose();
        texLogo.dispose();
        texButton.dispose();
        texPanel.dispose();
    }
}
