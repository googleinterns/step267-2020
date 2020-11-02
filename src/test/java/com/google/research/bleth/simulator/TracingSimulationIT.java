package com.google.research.bleth.simulator;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TracingSimulationIT {

    private static final IMovementStrategy MOVE_UP = new UpMovementStrategy();
    private static final IMovementStrategy STATIONARY = new StationaryMovementStrategy();

    private static final double TRANSMISSION_THRESHOLD_RADIUS_EQUALS_ONE = 1.0;
    private static final int AWAKENESS_CYCLE_EQUALS_TWO = 2;
    private static final int AWAKENESS_DURATION_EQUALS_ONE = 1;
    private static final AwakenessStrategyFactory.Type FIXES_AWAKENESS_STRATEGY_TYPE = AwakenessStrategyFactory.Type.FIXED;

    @Test
    public void runSimulationSingleRoundVerifyAgentsLocations() {
        int roundsNum = 2;
        int rowsNum = 2;
        int colsNum = 2;
        int beaconsNum = 1;
        int observersNum = 1;

        // Create new simulation.
        AbstractSimulation simulation = new TracingSimulation.Builder()
                .setMaxNumberOfRounds(roundsNum)
                .setRowNum(rowsNum)
                .setColNum(colsNum)
                .setBeaconsNum(beaconsNum)
                .setObserversNum(observersNum)
                .setTransmissionThresholdRadius(TRANSMISSION_THRESHOLD_RADIUS_EQUALS_ONE)
                .setBeaconMovementStrategy(MOVE_UP)
                .setObserverMovementStrategy(STATIONARY)
                .setAwakenessCycle(AWAKENESS_CYCLE_EQUALS_TWO)
                .setAwakenessDuration(AWAKENESS_DURATION_EQUALS_ONE)
                .setAwakenessStrategyType(FIXES_AWAKENESS_STRATEGY_TYPE)
                .build();

        // Find agents and calculate their expected location after a single round.
        Location beaconInitialLocation = simulation.beacons.get(0).getLocation();
        Location observerInitialLocation = simulation.observers.get(0).getLocation();
        Location beaconExpectedLocation = predictLocationAfterMoveUp(beaconInitialLocation, 1);
        Location observerExpectedLocation = observerInitialLocation;

        // Run single-rounded simulation and find agents again.
        simulation.run();
        Location beaconActualLocation = simulation.beacons.get(0).getLocation();
        Location observerActualLocation = simulation.observers.get(0).getLocation();

        assertThat(beaconActualLocation).isEqualTo(beaconExpectedLocation);
        assertThat(observerActualLocation).isEqualTo(observerExpectedLocation);
    }

    private Location predictLocationAfterMoveUp(Location location, int numberOfRounds) {
        return Location.create(Math.max(0, location.row() - numberOfRounds), location.col());
    }
}
