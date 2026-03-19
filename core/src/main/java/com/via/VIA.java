package com.via;

import com.badlogic.gdx.Game;
import com.via.model.GameState;
import com.via.model.building.Road;
import com.via.model.map.*;
import com.via.model.vehicle.SmallTruck;
import com.via.model.vehicle.Vehicle;
import com.via.model.vehicle.VehiclePosition;
import com.via.view.GameScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class VIA extends Game {
    @Override
    public void create() {
        Map map = new Map(new GridSize(40, 30));
        for(int x = 0; x < 40; x++) {
            for(int y = 0; y < 30; y++) {
                Terrain terrain = new Terrain(TerrainType.FLAT, 0, 0);
                Tile tile = new Tile(new TileCoord(x, y), terrain, null, null);
                map.setTile(tile, new TileCoord(x, y));
            }
        }
        Tile roadTile = new Tile(new TileCoord(5, 5),
            new Terrain(TerrainType.FLAT, 0, 0),
            new Road(new TileCoord(5, 5), 100),
            null);
        map.setTile(roadTile, new TileCoord(5, 5));
        // Add a few road tiles in a row to test bitmask
        for (int i = 3; i < 8; i++) {
            Tile road = new Tile(new TileCoord(i, 5),
                new Terrain(TerrainType.FLAT, 0, 0),
                new Road(new TileCoord(i, 5), 100), null);
            map.setTile(road, new TileCoord(i, 5));
        }

// Add a test city block
        for (int x = 10; x < 13; x++) {
            for (int y = 10; y < 13; y++) {
                Tile city = new Tile(new TileCoord(x, y),
                    new Terrain(TerrainType.FLAT, 0, 0),
                    new Road(new TileCoord(x, y), 0), null);
                map.setTile(city, new TileCoord(x, y));
            }
        }

        GameState gameState = new GameState(map);
        VehiclePosition pos = new VehiclePosition(
            new TileCoord(5, 5), new TileCoord(6, 5));
        Vehicle truck = new SmallTruck(pos);
        gameState.addVehicle(truck);

        this.setScreen(new GameScreen(gameState));
    }
}
