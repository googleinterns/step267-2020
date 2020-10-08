package com.google.research.bleth.simulator;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** Tracing Simulation's Resolver, which receives information from its observers and estimate the beacons' locations according to it. */
public final class GlobalResolver implements IGlobalResolver {
    private Board estimatedBoard;
    private final Simulation simulation;
    private Multimap<Transmission, Location> currentRoundTransmissions = ArrayListMultimap.create();
    private HashBiMap<Beacon, Transmission> beaconsToTransmissions;
    private Map<Beacon, Location> beaconsToEstimatedLocations = new HashMap<>();

    /**
     * A wrapper method to create new global resolver for a specific simulation.
     * The new resolver has a board for storing the estimated beacons' locations.
     * @param simulation is the world the resolver exists in.
     */
    public static GlobalResolver createResolver(Simulation simulation) {
        checkNotNull(simulation);
        Board board = new Board(simulation.getBoard().getRowsNum(), simulation.getBoard().getColsNum());
        HashBiMap<Beacon, Transmission> beaconsToTransmissions = HashBiMap.create();
        for (Beacon beacon : simulation.getBeacons()) {
            beaconsToTransmissions.put(beacon, beacon.transmit());
        }
        return new GlobalResolver(simulation, board, beaconsToTransmissions);
    }

    @Override
    public void receiveInformation(Location observerLocation, List<Transmission> transmissions) {
        for (Transmission transmission : transmissions) {
            currentRoundTransmissions.put(transmission, observerLocation);
        }
    }

    @Override
    public void estimate() {
        // Update only the beacons that there's new information about their location
        for (Transmission transmission : currentRoundTransmissions.keySet()) {
            Beacon beacon = beaconsToTransmissions.inverse().get(transmission);
            // Take into consideration the current estimated location of the beacon if there's such
            if (beaconsToEstimatedLocations.containsKey(beacon)) {
                currentRoundTransmissions.put(transmission, beaconsToEstimatedLocations.get(beacon));
            }
            Location newLocation = estimateNewLocation(beacon);

            if (!beaconsToEstimatedLocations.keySet().contains(beacon)) {
                estimatedBoard.placeAgent(newLocation, beacon);
            } else {
                estimatedBoard.moveAgent(beaconsToEstimatedLocations.get(beacon), newLocation, beacon);
            }
            beaconsToEstimatedLocations.put(beacon, newLocation);
        }

         currentRoundTransmissions.clear();
    }

    @Override
    public Board getBoard() {
        return estimatedBoard;
    }

    private GlobalResolver(Simulation simulation, Board estimatedBoard, HashBiMap<Beacon, Transmission> beaconsToTransmissions) {
        this.simulation = simulation;
        this.estimatedBoard = estimatedBoard;
        this.beaconsToTransmissions = beaconsToTransmissions;
    }

    private Location estimateNewLocation(Beacon beacon) {
        List<Location> locations = (List<Location>) currentRoundTransmissions.get(beacon.transmit());
        int newRow = (int) Math.round((locations.stream().mapToDouble(location -> location.row).average().getAsDouble()));
        int newCol = (int) Math.round((locations.stream().mapToDouble(location -> location.col).average().getAsDouble()));
        return new Location(newRow, newCol);
    }
}