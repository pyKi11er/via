package com.via.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.via.model.Constants;
import com.via.model.GameState;
import com.via.model.building.*;
import com.via.model.enums.Direction;
import com.via.model.enums.VehicleType;
import com.via.model.map.*;
import com.via.model.vehicle.Vehicle;
import com.via.model.vehicle.VehiclePosition;

import java.util.ArrayList;
import java.util.List;

public class WorldMapRenderer {

    private static final int TILE_SIZE   = Constants.TILE_SIZE;
    private static final int SPRITE_SIZE = 8;

    private final SpriteBatch        batch;
    private final OrthographicCamera camera;

    private final Texture            fallbackSheet;
    private final TextureRegion[][]  fallbackTiles;
    private final List<Texture>      ownedTextures = new ArrayList<>();

    // Terrain
    private final TextureRegion   texGrass;
    private final TextureRegion   texFlatGround;
    private final TextureRegion   texElevatedGround;
    private final TextureRegion   texSlopeN, texSlopeS, texSlopeE, texSlopeW;
    private final TextureRegion[] texForest = new TextureRegion[4];

    // Roads
    private final TextureRegion[] roadSprites = new TextureRegion[16];

    // Buildings
    private final TextureRegion texCityTile;
    private final TextureRegion texFacilityTile;
    private final TextureRegion texGarageTile;
    private final Texture       texParkingFull; // 64x64 drawn across 2x2 tiles
    // Overlays
    private final TextureRegion texStop;

    // Vehicles — 4 directions (N=0 E=1 S=2 W=3)
    private final TextureRegion[] truckSprites = new TextureRegion[4];
    private final TextureRegion[] busSprites   = new TextureRegion[4];

    public WorldMapRenderer(SpriteBatch batch, OrthographicCamera camera) {
        this.batch  = batch;
        this.camera = camera;

        fallbackSheet = new Texture("tilemap_packed.png");
        fallbackSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        fallbackTiles = TextureRegion.split(fallbackSheet, SPRITE_SIZE, SPRITE_SIZE);

        texGrass  = load("terrain/grass.png",   fallbackTiles[0][0]);
        texFlatGround     = generateSolidTexture(0.85f, 0.45f, 0.10f); // orange
        texElevatedGround = generateSolidTexture(0.10f, 0.40f, 0.05f); // dark green
        texSlopeN = loadOrGenerateSlope("terrain/slope_n.png", SlopeEdge.NORTH);
        texSlopeS = loadOrGenerateSlope("terrain/slope_s.png", SlopeEdge.SOUTH);
        texSlopeE = loadOrGenerateSlope("terrain/slope_e.png", SlopeEdge.EAST);
        texSlopeW = loadOrGenerateSlope("terrain/slope_w.png", SlopeEdge.WEST);
        texForest[0] = load("terrain/forest_1.jpg", fallbackTiles[0][0]);
        texForest[1] = load("terrain/forest_2.jpg", fallbackTiles[0][0]);
        texForest[2] = load("terrain/forest_3.jpg", fallbackTiles[0][0]);
        texForest[3] = load("terrain/forest_4.jpg", fallbackTiles[0][0]);

        texCityTile     = load("buildings/city_tile.png",     fallbackTiles[2][0]);
        texFacilityTile = load("buildings/facility_tile.png", fallbackTiles[3][4]);
        texGarageTile   = load("buildings/garage_tile.png",   fallbackTiles[3][9]);
        texParkingFull  = loadFullTexture("buildings/parking_tile.png");
        texStop         = load("overlays/stop.png",           fallbackTiles[7][4]);

        truckSprites[0] = load("vehicles/truck_n.png", fallbackTiles[9][8]);
        truckSprites[1] = load("vehicles/truck_e.png", fallbackTiles[9][8]);
        truckSprites[2] = load("vehicles/truck_s.png", fallbackTiles[9][8]);
        truckSprites[3] = load("vehicles/truck_w.png", fallbackTiles[9][8]);
        busSprites[0]   = load("vehicles/bus_n.png",   fallbackTiles[9][9]);
        busSprites[1]   = load("vehicles/bus_e.png",   fallbackTiles[9][9]);
        busSprites[2]   = load("vehicles/bus_s.png",   fallbackTiles[9][9]);
        busSprites[3]   = load("vehicles/bus_w.png",   fallbackTiles[9][9]);

        buildRoadSpriteTable();
    }

    private void buildRoadSpriteTable() {
        TextureRegion cross = load("roads/road_cross.png",      fallbackTiles[7][4]);
        TextureRegion tN    = load("roads/road_t_n.png",        fallbackTiles[7][3]);
        TextureRegion tS    = load("roads/road_t_s.png",        fallbackTiles[7][3]);
        TextureRegion tE    = load("roads/road_t_e.png",        fallbackTiles[7][3]);
        TextureRegion tW    = load("roads/road_t_w.png",        fallbackTiles[7][3]);
        TextureRegion strH  = load("roads/road_straight_h.png", fallbackTiles[7][3]);
        TextureRegion strV  = load("roads/road_straight_v.png", fallbackTiles[7][3]);
        TextureRegion cNE   = load("roads/road_corner_ne.png",  fallbackTiles[7][3]);
        TextureRegion cNW   = load("roads/road_corner_nw.png",  fallbackTiles[7][3]);
        TextureRegion cSE   = load("roads/road_corner_se.png",  fallbackTiles[7][3]);
        TextureRegion cSW   = load("roads/road_corner_sw.png",  fallbackTiles[7][3]);
        TextureRegion endN  = load("roads/road_end_n.png",      fallbackTiles[7][3]);
        TextureRegion endS  = load("roads/road_end_s.png",      fallbackTiles[7][3]);
        TextureRegion endE  = load("roads/road_end_e.png",      fallbackTiles[7][3]);
        TextureRegion endW  = load("roads/road_end_w.png",      fallbackTiles[7][3]);
        TextureRegion alone = load("roads/road_alone.png",      fallbackTiles[7][3]);

        roadSprites[0]  = alone;
        roadSprites[2]  = endN;
        roadSprites[8]  = endS;
        roadSprites[1]  = endE;
        roadSprites[4]  = endW;
        roadSprites[3]  = cSW;
        roadSprites[5]  = strH;
        roadSprites[6]  = cSE;
        roadSprites[7]  = tS;
        roadSprites[9]  = cNW;
        roadSprites[10] = strV;
        roadSprites[11] = tW;
        roadSprites[12] = cNE;
        roadSprites[13] = tN;
        roadSprites[14] = tE;
        roadSprites[15] = cross;
    }

    public void render(GameState state) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float camX = camera.position.x - camera.viewportWidth  / 2f;
        float camY = camera.position.y - camera.viewportHeight / 2f;
        float camW = camera.viewportWidth;
        float camH = camera.viewportHeight;

        for (float x = camX - TILE_SIZE; x < camX + camW + TILE_SIZE; x += TILE_SIZE) {
            for (float y = camY - TILE_SIZE; y < camY + camH + TILE_SIZE; y += TILE_SIZE) {
                float tx = (float)(Math.floor(x / TILE_SIZE) * TILE_SIZE);
                float ty = (float)(Math.floor(y / TILE_SIZE) * TILE_SIZE);

                // Check if this screen position corresponds to a map tile
                int tileX = (int)(tx / TILE_SIZE);
                int tileY = (int)(ty / TILE_SIZE);
                TileCoord coord = new TileCoord(tileX, tileY);

                if (state.getMap().isWithinBounds(coord)) {
                    draw(texGrass, tx, ty);
                } else {
                    draw(texForest[3], tx, ty);
                }
            }
        }

        renderTiles(state.getMap());
        renderVehicles(state.getVehicles());
        batch.end();
    }
    private void renderTiles(Map map) {
        // Pass 1: terrain + non-parking structures + overlays
        for (int x = 0; x < map.size.width; x++) {
            for (int y = 0; y < map.size.height; y++) {
                TileCoord coord = new TileCoord(x, y);
                Tile tile = map.getTile(coord);
                if (tile == null) continue;
                float sx = x * TILE_SIZE, sy = y * TILE_SIZE;
                // Skip terrain for parking tiles — parking texture covers it fully
                if (!(tile.getStructure() instanceof com.via.model.building.Parking)) {
                    draw(getTerrainTexture(tile.getTerrain()), sx, sy);
                }
                if (tile.getStructure() != null
                        && !(tile.getStructure() instanceof com.via.model.building.Parking)) {
                    draw(getStructureTexture(tile.getStructure(), map), sx, sy);
                }
                if (tile.getOverlay() != null)
                    draw(getOverlayTexture(tile.getOverlay()), sx, sy);
            }
        }

        // Pass 2: draw each parking lot's 64×64 texture on top of terrain
        java.util.Set<com.via.model.building.Parking> drawn = new java.util.HashSet<>();
        for (int x = 0; x < map.size.width; x++) {
            for (int y = 0; y < map.size.height; y++) {
                Tile tile = map.getTile(new TileCoord(x, y));
                if (tile == null || !(tile.getStructure() instanceof com.via.model.building.Parking p)) continue;
                if (drawn.add(p) && texParkingFull != null) {
                    TileCoord o = p.getFootprint().getOrigin();
                    batch.draw(texParkingFull,
                        o.x * TILE_SIZE, o.y * TILE_SIZE,
                        TILE_SIZE * 2, TILE_SIZE * 2);
                }
            }
        }
    }

    private TextureRegion getTerrainTexture(Terrain terrain) {
        if (terrain == null) return texGrass;
        int f = terrain.getForestLevel();
        if (f > 0) return texForest[Math.min(f - 1, 3)];
        int level = terrain.getTerrainLevel();
        if (level < 0) return texFlatGround;
        if (level == 0) return texGrass;
        return texElevatedGround;
    }

    private TextureRegion getStructureTexture(Structure structure, Map map) {
        if (structure instanceof Road road) return getRoadSprite(road, map);
        if (structure instanceof City)     return texCityTile;
        if (structure instanceof Facility) return texFacilityTile;
        if (structure instanceof Garage) return texGarageTile;
        return fallbackTiles[7][3];
    }

    private TextureRegion getOverlayTexture(Overlay overlay) {
        if (overlay instanceof StopOverlay) return texStop;
        return fallbackTiles[0][0];
    }

    private TextureRegion getRoadSprite(Road road, Map map) {
        List<Direction> c = road.getConnections(map);
        int key = 0;
        if (c.contains(Direction.N)) key |= 2;
        if (c.contains(Direction.E)) key |= 4;
        if (c.contains(Direction.S)) key |= 8;
        if (c.contains(Direction.W)) key |= 1;
        return roadSprites[key];
    }
    /*
    private void renderVehicles(List<Vehicle> vehicles) {
        for (Vehicle v : vehicles) {
            VehiclePosition pos = v.getPosition();
            if (pos == null) continue;

            if (v.getParking() != null && v.getParkedSlot() >= 0) {
                float[] slotPos = v.getParking().getSlotWorldPos(v.getParkedSlot());
                batch.draw(getVehicleSprite(v), slotPos[0], slotPos[1], 16, 16);
            } else {
                float sx = lerp(pos.getOrigin().x * TILE_SIZE,
                    pos.getDestination().x * TILE_SIZE, pos.getProgress());
                float sy = lerp(pos.getOrigin().y * TILE_SIZE,
                    pos.getDestination().y * TILE_SIZE, pos.getProgress());
                draw(getVehicleSprite(v), sx, sy);
            }
        }
    }
    */
    private void renderVehicles(List<Vehicle> vehicles) {
        for (Vehicle v : vehicles) {
            VehiclePosition pos = v.getPosition();
            if (pos == null) continue;

            if (v.getParking() != null && v.getParkedSlot() >= 0) {
                float[] slotPos = v.getParking().getSlotWorldPos(v.getParkedSlot());
                batch.draw(getVehicleSprite(v), slotPos[0], slotPos[1], 16, 16);
                continue;
            }

            if (v.isMaintaining()) {
                // Draw translucent to indicate it is "inside" the garage being serviced
                batch.setColor(1f, 1f, 1f, 0.4f);
                draw(getVehicleSprite(v), pos.getOrigin().x * TILE_SIZE, pos.getOrigin().y * TILE_SIZE);
                batch.setColor(1f, 1f, 1f, 1f); // reset color for other sprites
                continue; // Skip the lane positioning logic
            }

            float sx = lerp(pos.getOrigin().x * TILE_SIZE,
                pos.getDestination().x * TILE_SIZE, pos.getProgress());
            float sy = lerp(pos.getOrigin().y * TILE_SIZE,
                pos.getDestination().y * TILE_SIZE, pos.getProgress());

            int dx = pos.getDestination().x - pos.getOrigin().x;
            int dy = pos.getDestination().y - pos.getOrigin().y;

            // Always place vehicle in right-hand lane based on direction
            float normalOffset = TILE_SIZE * 0.20f;
            float laneX = 0f, laneY = 0f;

            if      (dx > 0) laneY = -normalOffset; // east  → south lane
            else if (dx < 0) laneY =  normalOffset; // west  → north lane
            else if (dy > 0) laneX =  normalOffset; // north → east lane
            else if (dy < 0) laneX = -normalOffset; // south → west lane

            // Overtaking: snap to opposite lane immediately, no animation
            if (pos.isOvertaking()) {
                laneX = -laneX;
                laneY = -laneY;
            }

            draw(getVehicleSprite(v), sx + laneX, sy + laneY);
        }
    }

    private TextureRegion getVehicleSprite(Vehicle v) {
        int idx = getMovementDirection(v.getPosition()).ordinal();
        return switch (v.getType()) {
            case TRUCK, MINI_TRUCK, HEAVY_TRUCK -> truckSprites[idx];
            case BUS, EXPRESS_BUS, CITY_BUS     -> busSprites[idx];
        };
    }

    private Direction getMovementDirection(VehiclePosition pos) {
        if (pos == null) return Direction.N;
        int dx = pos.getDestination().x - pos.getOrigin().x;
        int dy = pos.getDestination().y - pos.getOrigin().y;
        if (dx > 0) return Direction.E;
        if (dx < 0) return Direction.W;
        if (dy > 0) return Direction.N;  // Higher Y = North (up)
        return Direction.S;  // Lower Y = South (down)
    }

    private void draw(TextureRegion r, float x, float y) {
        batch.draw(r, x, y, TILE_SIZE, TILE_SIZE);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /**
     * Loads a PNG from assets as a TextureRegion.
     * If the file doesn't exist returns the fallback slice — never crashes.
     */
    private TextureRegion load(String path, TextureRegion fallback) {
        if (Gdx.files.internal(path).exists()) {
            Texture t = new Texture(Gdx.files.internal(path));
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            ownedTextures.add(t);
            return new TextureRegion(t);
        }
        return fallback;
    }

    private Texture loadFullTexture(String path) {
        if (Gdx.files.internal(path).exists()) {
            Texture t = new Texture(Gdx.files.internal(path));
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            ownedTextures.add(t);
            return t;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Slope texture generation
    // -------------------------------------------------------------------------

    /** Which edge of the tile faces downhill (carries the shadow). */
    private enum SlopeEdge { NORTH, SOUTH, EAST, WEST }

    /**
     * Returns the asset PNG if it exists; otherwise generates a gradient
     * texture procedurally so slopes are always visually distinct.
     * When you drop real artwork into {@code assets/terrain/slope_*.png} it
     * will be picked up automatically on the next launch.
     */
    private TextureRegion loadOrGenerateSlope(String path, SlopeEdge shadow) {
        if (Gdx.files.internal(path).exists()) {
            return load(path, null);
        }
        return generateSlopeTexture(shadow);
    }

    /**
     * Generates a 32×32 gradient tile where the downhill edge is dark
     * (cliff/shadow) and the uphill edge is bright (sun-lit grass).
     *
     * <p>Colour key:
     * <ul>
     *   <li>Uphill highlight  – light green  #7ab24a
     *   <li>Normal grass      – medium green #5d8a3c
     *   <li>Downhill shadow   – dark cliff   #2d5014
     * </ul>
     *
     * <p>In LibGDX, Pixmap row 0 maps to the <em>top</em> of the rendered
     * sprite (= north side of tile), so "shadow at top" = SLOPE_NORTH, etc.
     */
    private TextureRegion generateSlopeTexture(SlopeEdge shadow) {
        // Colour stops (R, G, B)
        final int[] HIGHLIGHT = {122, 178,  74};   // bright uphill
        final int[] MIDTONE   = { 93, 138,  60};   // normal grass
        final int[] DARK      = { 35,  65,  15};   // deep shadow / cliff

        com.badlogic.gdx.graphics.Pixmap pm =
            new com.badlogic.gdx.graphics.Pixmap(TILE_SIZE, TILE_SIZE,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);

        for (int py = 0; py < TILE_SIZE; py++) {
            for (int px = 0; px < TILE_SIZE; px++) {
                // t = 0.0 at the shadow edge, 1.0 at the highlight edge
                float t = switch (shadow) {
                    case NORTH -> (float) py / (TILE_SIZE - 1);          // row 0=top=north
                    case SOUTH -> 1.0f - (float) py / (TILE_SIZE - 1);   // row 31=bottom=south
                    case EAST  -> 1.0f - (float) px / (TILE_SIZE - 1);   // col 31=right=east
                    case WEST  -> (float) px / (TILE_SIZE - 1);           // col 0=left=west
                };

                // Map t: 0–0.3 → dark→mid, 0.3–1.0 → mid→highlight
                int r, g, b;
                if (t < 0.3f) {
                    float s = t / 0.3f;
                    r = lerpi(DARK[0], MIDTONE[0], s);
                    g = lerpi(DARK[1], MIDTONE[1], s);
                    b = lerpi(DARK[2], MIDTONE[2], s);
                } else {
                    float s = (t - 0.3f) / 0.7f;
                    r = lerpi(MIDTONE[0], HIGHLIGHT[0], s);
                    g = lerpi(MIDTONE[1], HIGHLIGHT[1], s);
                    b = lerpi(MIDTONE[2], HIGHLIGHT[2], s);
                }

                pm.setColor(r / 255f, g / 255f, b / 255f, 1f);
                pm.drawPixel(px, py);
            }
        }

        com.badlogic.gdx.graphics.Texture tex =
            new com.badlogic.gdx.graphics.Texture(pm);
        tex.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest,
                      com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest);
        pm.dispose();
        ownedTextures.add(tex);
        return new TextureRegion(tex);
    }

    private static int lerpi(int a, int b, float t) {
        return Math.round(a + (b - a) * t);
    }

    private TextureRegion generateSolidTexture(float r, float g, float b) {
        com.badlogic.gdx.graphics.Pixmap pm =
            new com.badlogic.gdx.graphics.Pixmap(TILE_SIZE, TILE_SIZE,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pm.setColor(r, g, b, 1f);
        pm.fill();
        com.badlogic.gdx.graphics.Texture tex =
            new com.badlogic.gdx.graphics.Texture(pm);
        tex.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest,
                      com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest);
        pm.dispose();
        ownedTextures.add(tex);
        return new TextureRegion(tex);
    }

    public void dispose() {
        fallbackSheet.dispose();
        for (Texture t : ownedTextures) t.dispose();
    }
}
