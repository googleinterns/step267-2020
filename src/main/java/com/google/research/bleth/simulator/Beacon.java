package com.google.research.bleth.simulator;

import com.google.common.base.Preconditions;

public class Beacon implements IBeacon {
    static int beaconId = 0; // used for generating unique id for every beacon.

    public final int id;
    private Location realLocation;
    private final MovementStrategy movementStrategy;
    private final Simulation simulation;

    /**
     * Create new Beacon with consecutive serial number.
     * @param initialLocation is the location on board where the agent is placed.
     * @param movementStrategy determines how the agent moves.
     * @param simulation is the world the agent lives in.
     */
    Beacon(Location initialLocation, MovementStrategy movementStrategy, Simulation simulation) {
        Preconditions.checkNotNull(initialLocation);
        Preconditions.checkNotNull(movementStrategy);
        Preconditions.checkNotNull(simulation);
        if (!simulation.getBoard().isLocationValid(initialLocation)) {
            throw new IllegalArgumentException("Invalid Location");
        }

        this.id = beaconId++;
        realLocation = initialLocation;
        simulation.getBoard().placeAgent(realLocation,this);
        this.movementStrategy = movementStrategy;
        this.simulation = simulation;
    }

    /**
     * Calculate the location the agent is moving to, based on its current location and its strategy.
     * @return the location on board which the agent is moving to.
     */
    @Override
    public Location moveTo() {
        return movementStrategy.move(simulation.getBoard(), realLocation);
    }

    /**
     * @return a transmission based on the beacon's eid.
     */
    @Override
    public Transmission transmit() {
        return new Transmission(id);
    }

    /**
     * @return the current agent's location on the real board.
     */
    @Override
    public Location getLocation() {
        return realLocation;
    }

    /**
     * Move the agent to its next location and update the board accordingly.
     */
    @Override
    public void move() {
        Location nextMove = moveTo();
        simulation.getBoard().moveAgent(realLocation, nextMove, this);
        realLocation = nextMove;
    }
}
