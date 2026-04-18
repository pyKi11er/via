package com.via.model;

import com.via.model.building.Building;
import com.via.model.economy.PlayerEconomy;
import com.via.model.economy.TimeState;
import com.via.model.map.Map;
import com.via.model.vehicle.Route;
import com.via.model.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete state of the game session.
 * Holds references to the map, vehicles, routes, buildings, economy, and time state.
 * Acts as the central data container for the simulation.
 */
public class GameState {
    private Map map;
    private List<Vehicle> vehicles;
    private List<Route> routes;
    private List<Building> buildings;
    private PlayerEconomy economy;
    private TimeState timeState;
    private boolean gameOver;

    /**
     * Constructs a new GameState with the specified map.
     * Initializes empty collections for vehicles, routes, and buildings,
     * creates a new player economy and time state, and sets game over to false.
     *
     * @param map the game map (must not be null)
     */
    public GameState(Map map) {
        this.map = map;
        vehicles = new ArrayList<>();
        routes = new ArrayList<>();
        buildings = new ArrayList<>();
        economy = new PlayerEconomy();
        timeState = new TimeState();
        gameOver = false;
    }

    /**
     * Returns the game map.
     *
     * @return the map instance
     */
    public Map getMap() {
        return map;
    }

    /**
     * Returns the list of all vehicles in the game.
     *
     * @return the vehicle list (modifiable)
     */
    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    /**
     * Returns the list of all routes in the game.
     *
     * @return the route list (modifiable)
     */
    public List<Route> getRoutes() {
        return routes;
    }

    /**
     * Returns the list of all buildings in the game.
     *
     * @return the building list (modifiable)
     */
    public List<Building> getBuildings() {
        return buildings;
    }

    /**
     * Returns the player economy manager.
     *
     * @return the economy instance
     */
    public PlayerEconomy getEconomy() {
        return economy;
    }

    /**
     * Returns the game time state.
     *
     * @return the time state instance
     */
    public TimeState getTimeState() {
        return timeState;
    }

    /**
     * Returns whether the game is over.
     *
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Sets the game over state.
     *
     * @param gameOver the new game over state
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    /**
     * Adds a vehicle to the game state.
     *
     * @param vehicle the vehicle to add
     */
    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    /**
     * Removes a vehicle from the game state.
     *
     * @param vehicle the vehicle to remove
     */
    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
    }

    /**
     * Adds a route to the game state.
     *
     * @param route the route to add
     */
    public void addRoute(Route route) {
        routes.add(route);
    }

    /**
     * Adds a building to the game state.
     *
     * @param building the building to add
     */
    public void addBuilding(Building building) {
        buildings.add(building);
    }

    /**
     * Removes a building from the game state.
     *
     * @param building the building to remove
     */
    public void removeBuilding(Building building) {
        buildings.remove(building);
    }
}
