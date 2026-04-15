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
    private final TextureRegion   texSlopeN, texSlopeS, texSlopeE, texSlopeW;
    private final TextureRegion[] texForest = new TextureRegion[4];

    // Roads
    private final TextureRegion[] roadSprites = new TextureRegion[16];

    // Buildings
    private final TextureRegion texCityTile;
    private final TextureRegion texFacilityTile;
    private final TextureRegion texGarageTile;
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
        texSlopeN = load("terrain/slope_n.png", fallbackTiles[0][2]);
        texSlopeS = load("terrain/slope_s.png", fallbackTiles[0][2]);
        texSlopeE = load("terrain/slope_e.png", fallbackTiles[0][2]);
        texSlopeW = load("terrain/slope_w.png", fallbackTiles[0][2]);
        texForest[0] = load("terrain/forest_1.jpg", fallbackTiles[0][0]);
        texForest[1] = load("terrain/forest_2.jpg", fallbackTiles[0][0]);
        texForest[2] = load("terrain/forest_3.jpg", fallbackTiles[0][0]);
        texForest[3] = load("terrain/forest_4.jpg", fallbackTiles[0][0]);

        texCityTile     = load("buildings/city_tile.png",     fallbackTiles[2][0]);
        texFacilityTile = load("buildings/facility_tile.png", fallbackTiles[3][4]);
        texGarageTile = load("buildings/garage_tile.png", fallbackTiles[3][9]);
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
        for (int x = 0; x < map.size.width; x++) {
            for (int y = 0; y < map.size.height; y++) {
                TileCoord coord = new TileCoord(x, y);
                Tile tile = map.getTile(coord);
                if (tile == null) continue;
                float sx = x * TILE_SIZE, sy = y * TILE_SIZE;
                draw(getTerrainTexture(tile.getTerrain()), sx, sy);
                if (tile.getStructure() != null)
                    draw(getStructureTexture(tile.getStructure(), map), sx, sy);
                if (tile.getOverlay() != null)
                    draw(getOverlayTexture(tile.getOverlay()), sx, sy);
            }
        }
    }

    private TextureRegion getTerrainTexture(Terrain terrain) {
        if (terrain == null) return texGrass;
        int f = terrain.getForestLevel();
        if (f > 0) return texForest[Math.min(f - 1, 3)];
        return switch (terrain.getType()) {
            case SLOPE_NORTH -> texSlopeN;
            case SLOPE_SOUTH -> texSlopeS;
            case SLOPE_EAST  -> texSlopeE;
            case SLOPE_WEST  -> texSlopeW;
            default          -> texGrass;
        };
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

    private void renderVehicles(List<Vehicle> vehicles) {
        for (Vehicle v : vehicles) {
            VehiclePosition pos = v.getPosition();
            if (pos == null) continue;
            float sx = lerp(pos.getOrigin().x      * TILE_SIZE,
                pos.getDestination().x * TILE_SIZE, pos.getProgress());
            float sy = lerp(pos.getOrigin().y      * TILE_SIZE,
                pos.getDestination().y * TILE_SIZE, pos.getProgress());
            draw(getVehicleSprite(v), sx, sy);
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
        return fallback; // specific slice, NOT the whole sheet
    }

    public void dispose() {
        fallbackSheet.dispose();
        for (Texture t : ownedTextures) t.dispose();
    }
}
