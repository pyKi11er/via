package com.via.view;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.via.model.Constants;
import com.via.model.GameState;
import com.via.model.building.City;
import com.via.model.building.Facility;
import com.via.model.building.Road;
import com.via.model.enums.Direction;
import com.via.model.map.*;
import com.via.model.vehicle.Vehicle;
import com.via.model.vehicle.VehiclePosition;

import java.util.List;

public class WorldMapRenderer {
    private static final int TILE_SIZE = Constants.TILE_SIZE;
    private static final int SPRITE_SIZE = 8;

    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private Texture tilesheet;
    private TextureRegion[][] tiles;
    private TextureRegion[] roadSprites;

    public WorldMapRenderer(SpriteBatch batch, OrthographicCamera camera) {
        this.batch = batch;
        this.camera = camera;
        tilesheet = new Texture("tilemap_packed.png");
        tilesheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        tiles = TextureRegion.split(tilesheet, SPRITE_SIZE, SPRITE_SIZE);
        roadSprites = new TextureRegion[16];
        // key 0 = no connections (isolated road)
        roadSprites[0] = tiles[7][3];   // plain gray
        // key 1 = W only
        roadSprites[1] = tiles[7][3];   // horizontal
        // key 2 = S only
        roadSprites[2] = tiles[7][3];   // vertical
        // key 3 = S+W
        roadSprites[3] = tiles[7][3];   // corner
        // ... all 16 entries
        // For now just put tiles[7][3] for all of them
        for (int i = 0; i < 16; i++) {
            roadSprites[i] = tiles[7][3];
        }
    }

    private TextureRegion getTerrainSprite(Terrain terrain) {
        // tiles[row][col] — look at spritesheet
        // figure out which row/col is grass, road, etc.
        if (terrain == null) return tiles[0][0];
        switch (terrain.getType()) {
            case FLAT:
                if (terrain.getForestLevel() > 0) return tiles[0][0]; // green with trees later
                return tiles[0][0]; // plain grass
            case SLOPE_NORTH:
            case SLOPE_SOUTH:
            case SLOPE_EAST:
            case SLOPE_WEST:
                return tiles[0][2]; // gray pavement as placeholder for slopes
            default:
                return tiles[0][0];
        }
    }

    public void render(GameState state) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        renderTiles(state.getMap());
        renderVehicles(state.getVehicles());
        batch.end();
    }

    private void renderTiles(Map map) {
        for(int x = 0; x < map.size.width; x++){
            for(int y = 0; y < map.size.height; y++){
                TileCoord coord = new TileCoord(x, y);
                Tile tile = map.getTile(coord);
                if(tile == null) continue;

                float screenX = x * TILE_SIZE;
                float screenY = y * TILE_SIZE;

                batch.draw(getTerrainSprite(tile.getTerrain()),  screenX, screenY,  TILE_SIZE, TILE_SIZE);

                if(tile.getStructure() != null) {
                    batch.draw(getStructureSprite(tile.getStructure(), map), screenX, screenY, TILE_SIZE, TILE_SIZE);
                }

                if(tile.getOverlay() != null) {
                    batch.draw(getOverlaySprite(tile.getOverlay()), screenX,
                        screenY, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private TextureRegion getStructureSprite(Structure structure, Map map) {
        if (structure instanceof Road road) return getRoadSprite(road, map);
        if (structure instanceof City) return tiles[2][0];
        if (structure instanceof Facility) return tiles[3][4];
        return tiles[2][0];
    }

    private TextureRegion getOverlaySprite(Overlay overlay) {
        return tiles[0][0]; // placeholder
    }

    private void renderVehicles(List<Vehicle> vehicles) {
        for (Vehicle vehicle : vehicles) {
            VehiclePosition pos = vehicle.getPosition();
            if (pos == null) continue;

            // Interpolate between origin and destination
            float screenX = lerp(pos.getOrigin().x * TILE_SIZE,
                pos.getDestination().x * TILE_SIZE,
                pos.getProgress());
            float screenY = lerp(pos.getOrigin().y * TILE_SIZE,
                pos.getDestination().y * TILE_SIZE,
                pos.getProgress());

            batch.draw(getVehicleSprite(vehicle),
                screenX, screenY, TILE_SIZE, TILE_SIZE);
        }
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private TextureRegion getRoadSprite(Road road, Map map) {
        List<Direction> connections = road.getConnections(map);
        boolean n = connections.contains(Direction.N);
        boolean e = connections.contains(Direction.E);
        boolean s = connections.contains(Direction.S);
        boolean w = connections.contains(Direction.W);

        int key = (n ? 8 : 0) | (e ? 4 : 0) | (s ? 2 : 0) | (w ? 1 : 0);

        return roadSprites[key];
    }

    private TextureRegion getVehicleSprite(Vehicle vehicle) {
        switch (vehicle.getType()) {
            case TRUCK: return tiles[12][12]; // green truck
            case BUS: return tiles[13][12];   // yellow bus
            default: return tiles[10][12];
        }
    }
}
