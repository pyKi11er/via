package com.via.model;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class GameState {
    private Map map;
    private List<Vehicle> vehicles;
    private List<Route> routes;
    private List<Building> buildings;
    private PlayerEconomy economy;
    private TimeState timeState;
    private boolean gameOver;

    public GameState(Map map) {
        this.map = map;
        vehicles = new ArrayList<>();
        routes = new ArrayList<>();
        buildings = new ArrayList<>();
        economy = new PlayerEconomy();
        timeState = new TimeState();
        gameOver = false;
    }
    public Map getMap() {
        return map;
    }
    public List<Vehicle> getVehicles() {
        return vehicles;
    }
    public List<Route> getRoutes() {
        return routes;
    }
    public List<Building> getBuildings() {
        return buildings;
    }
    public PlayerEconomy getEconomy() {
        return economy;
    }
    public TimeState getTimeState() {
        return timeState;
    }
    public boolean isGameOver() {
        return gameOver;
    }
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
    }

    public void addRoute(Route route) {
        routes.add(route);
    }

    public void addBuilding(Building building) {
        buildings.add(building);
    }

    public void removeBuilding(Building building) {
        buildings.remove(building);
    }
}
