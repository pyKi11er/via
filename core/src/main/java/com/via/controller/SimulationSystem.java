package com.via.controller;

import com.via.model.GameState;

import java.util.Objects;

/**
 * Coordinates all simulation subsystems and defines the per-tick update order.
 *
 * <p>The {@code SimulationSystem} acts as the central orchestrator of game logic,
 * ensuring that each subsystem is updated in a consistent and deterministic sequence
 * every simulation tick.</p>
 *
 * <p>Managed subsystems include:</p>
 * <ul>
 *     <li>{@link ForestSystem} – environmental growth and spreading</li>
 *     <li>{@link FacilitySystem} – production, consumption, and stop transfers</li>
 *     <li>{@link VehicleMaintenanceSystem} – maintenance cost and repair handling</li>
 *     <li>{@link VehicleMovementSystem} – vehicle movement and cargo handling</li>
 *     <li>{@link EconomySystem} – income processing and game-over conditions</li>
 * </ul>
 */
public class SimulationSystem {

    private final ForestSystem forestSystem;
    private final EconomySystem economySystem;
    private final FacilitySystem facilitySystem;
    private final VehicleMovementSystem vehicleMovementSystem;
    private final VehicleMaintenanceSystem vehicleMaintenanceSystem;

    /**
     * Creates a {@code SimulationSystem} with default subsystem instances.
     *
     * <p>The {@link EconomySystem} instance is shared with the
     * {@link VehicleMovementSystem} to ensure consistent income handling.</p>
     */
    public SimulationSystem() {
        EconomySystem economySystem = new EconomySystem();
        this.forestSystem = new ForestSystem();
        this.economySystem = economySystem;
        this.facilitySystem = new FacilitySystem();
        this.vehicleMovementSystem = new VehicleMovementSystem(economySystem);
        this.vehicleMaintenanceSystem = new VehicleMaintenanceSystem();
    }

    /**
     * Creates a {@code SimulationSystem} with explicitly provided subsystems.
     *
     * <p>This constructor is useful for testing or custom configurations.</p>
     *
     * @param forestSystem handles environmental simulation
     * @param economySystem handles income and economy state
     * @param facilitySystem handles production and resource flow
     * @param vehicleMovementSystem handles vehicle movement and cargo logic
     * @param vehicleMaintenanceSystem handles maintenance and repairs
     * @throws NullPointerException if any parameter is {@code null}
     */
    public SimulationSystem(
        ForestSystem forestSystem,
        EconomySystem economySystem,
        FacilitySystem facilitySystem,
        VehicleMovementSystem vehicleMovementSystem,
        VehicleMaintenanceSystem vehicleMaintenanceSystem
    ) {
        this.forestSystem = Objects.requireNonNull(forestSystem, "forestSystem must not be null");
        this.economySystem = Objects.requireNonNull(economySystem, "economySystem must not be null");
        this.facilitySystem = Objects.requireNonNull(facilitySystem, "facilitySystem must not be null");
        this.vehicleMovementSystem = Objects.requireNonNull(vehicleMovementSystem, "vehicleMovementSystem must not be null");
        this.vehicleMaintenanceSystem = Objects.requireNonNull(vehicleMaintenanceSystem, "vehicleMaintenanceSystem must not be null");
    }

    /**
     * Advances the simulation by one tick.
     *
     * <p>The update order is strictly defined to ensure correct data flow:</p>
     * <ol>
     *     <li><b>Time update:</b> Increment the global tick counter</li>
     *     <li><b>Forest:</b> Grow and spread environmental features</li>
     *     <li><b>Facilities:</b> Produce/consume goods and transfer to stops</li>
     *     <li><b>Maintenance (pre-move):</b> Apply maintenance costs and checks</li>
     *     <li><b>Vehicle movement:</b> Move vehicles and handle loading/unloading
     *         (income is generated during unloading)</li>
     *     <li><b>Maintenance (post-move):</b> Restore vehicles if serviced at garages</li>
     *     <li><b>Economy:</b> Perform final checks (e.g., game-over conditions)</li>
     * </ol>
     *
     * <p>The {@code timeMultiplier} affects vehicle movement speed but does not
     * alter the logical tick rate of other systems.</p>
     *
     * @param state the current game state
     * @param timeMultiplier scaling factor for time-dependent systems (e.g., vehicle speed)
     */
    public void updateTick(GameState state, float timeMultiplier) {
        if (state == null || state.isGameOver()) return;

        // 1. Advance time
        state.getTimeState().incrementTick();

        // 2. Environment
        forestSystem.tick(state);

        // 3. Production and logistics
        facilitySystem.tick(state);

        // 4. Pre-movement maintenance
        vehicleMaintenanceSystem.tickBeforeMovement(state);

        // 5. Movement and cargo handling
        vehicleMovementSystem.tick(state, timeMultiplier);

        // 6. Post-movement maintenance
        vehicleMaintenanceSystem.tickAfterMovement(state);

        // 7. Economy checks
        economySystem.tick(state);
    }
}
