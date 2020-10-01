package com.google.research.bleth.simulator;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TracingSimulationTest {

    public final MovementStrategy moveUp = new moveUp();
    public final MovementStrategy stationary = new StationaryMovementStrategy();

    private static final String simulationId = "test-sim-id-1";
    private static final int maxRoundsEqualsTwo = 2;
    private static final int numberOfBeaconsEqualsTwo = 2;
    private static final int numberOfObserversEqualsZero = 0;

    private static final int awakenessCycleEqualsTwo = 2;
    private static final int awakenessdURATIONEqualsOne = 1;
    private static final double radiusEqualsOne = 1.0;

    public class moveUp implements MovementStrategy {

        @Override
        public Location moveTo(Board board, Location currentLocation) {
            Location newLocation = currentLocation.moveInDirection(Direction.UP);
            if (board.isLocationValid(newLocation)) {
                return newLocation;
            }
            return currentLocation;
        }
    }

    public Board buildExpectedBoard(Board originalBoard, int rowNum, int colNum) {

        Board expectedBoard = new Board(rowNum, colNum);
        for (int row = 0; row < rowNum; row++) {
            for (int col = 0; col < colNum; col++) {
                for (Agent agent : originalBoard.getAgentsOnLocation(new Location(row, col))) {
                    expectedBoard.placeAgent(agent.moveTo(), agent);
                }
            }
        }
        return expectedBoard;
    }

    public boolean boardsEqual(Board actual, Board expected, int rowNum, int colNum) {
        for (int row = 0; row < rowNum; row++) {
            for (int col = 0; col < colNum; col++) {
                Location loc = new Location(row, col);
                if (!actual.getAgentsOnLocation(loc).equals(expected.getAgentsOnLocation(loc))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Test
    public void initializeTwoBeaconsBeaconsContainerShouldEqualTwo() throws Exception {
        Simulation simulation = new TracingSimulation.TracingSimulationBuilder()
                .setId(simulationId).setMaxNumberOfRounds(maxRoundsEqualsTwo).setRowNum(2).setColNum(2)
                .setBeaconsNum(numberOfBeaconsEqualsTwo).setObserversNum(numberOfObserversEqualsZero)
                .setAwakenessCycle(awakenessCycleEqualsTwo).setAwakenessDuration(awakenessdURATIONEqualsOne)
                .setRadius(radiusEqualsOne).setBeaconMovementStrategy(moveUp).setObserverMovementStrategy(stationary)
                .build();

        simulation.initializeBeacons();

        assertThat(simulation.getBeacons().size()).isEqualTo(2);
    }

    @Test
    public void runSimulationWithMaxRoundNumberTwoCurrentRoundNumberShouldBeTwo() throws Exception {
        Simulation simulation = new TracingSimulation.TracingSimulationBuilder()
                .setId(simulationId).setMaxNumberOfRounds(maxRoundsEqualsTwo).setRowNum(2).setColNum(2)
                .setBeaconsNum(numberOfBeaconsEqualsTwo).setObserversNum(numberOfObserversEqualsZero)
                .setAwakenessCycle(awakenessCycleEqualsTwo).setAwakenessDuration(awakenessdURATIONEqualsOne)
                .setRadius(radiusEqualsOne).setBeaconMovementStrategy(moveUp).setObserverMovementStrategy(stationary)
                .build();

        simulation.run();

        assertThat(simulation.currentRound).isEqualTo(maxRoundsEqualsTwo);
    }

    @Test
    public void moveAgentsDeterministicMovementStrategyBoardShouldEqualExpectedBoard() throws Exception {
        Simulation simulation = new TracingSimulation.TracingSimulationBuilder()
                .setId(simulationId).setMaxNumberOfRounds(maxRoundsEqualsTwo).setRowNum(2).setColNum(2)
                .setBeaconsNum(numberOfBeaconsEqualsTwo).setObserversNum(numberOfObserversEqualsZero)
                .setAwakenessCycle(awakenessCycleEqualsTwo).setAwakenessDuration(awakenessdURATIONEqualsOne)
                .setRadius(radiusEqualsOne).setBeaconMovementStrategy(moveUp).setObserverMovementStrategy(stationary)
                .build();

        Board boardWhenSimulationInitialized = simulation.getBoard();
        Board expectedBoardAfterSingleRound = buildExpectedBoard(boardWhenSimulationInitialized, 2, 2);

        simulation.moveAgents();
        Board boardAfterMovement = simulation.getBoard();

        assertThat(boardsEqual(boardAfterMovement, expectedBoardAfterSingleRound, 2, 2)).isTrue();
    }
}
