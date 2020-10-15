package com.google.research.bleth.simulator;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An abstract class representing a BLETH simulation.
 * Can be either a Tracing simulation or a Stalking simulation.
 */
public abstract class AbstractSimulation {

    private final String id;
    private int currentRound;
    private final int maxNumberOfRounds;
    private RealBoard board;
    protected final ImmutableList<Beacon> beacons;
    protected final ImmutableList<Observer> observers;
    private IResolver resolver;
    private final double radius;
    private HashMap<String, Double> stats = new HashMap<>();

    /** Returns the simulation's real board. */
    RealBoard getBoard() {
        return board;
    }

    /** Run entire simulation logic, including writing data to db. */
    public void run() {
        if (currentRound == 0) {
            writeRoundState(); // round 0 is the initial simulation state
            currentRound++;
        }
        while (currentRound <= maxNumberOfRounds) {
            moveAgents();
            updateObserversAwaknessState();
            beaconsToObservers();
            observersToResolver();
            resolverEstimate();
            writeRoundState();
            updateSimulationStats();
            currentRound++;
        }
        writeSimulationStats();
    }

    /** Move all agents according to their movement strategies and update the real board. */
    void moveAgents() {
        for (Beacon beacon : beacons) {
            beacon.move();
        }
        for (Observer observer : observers) {
            observer.move();
        }
    }

    /** Update all observers awakeness states according to their awakeness strategies. */
    void updateObserversAwaknessState() {
        for (Observer observer : observers) {
            observer.updateAwakenessState(currentRound);
        }
    }

    /**
     * Pass transmissions from beacons to observers while taking into consideration world-physics parameters,
     * such as probability of transmission and distance between beacons and observers.
     */
    void beaconsToObservers() {
        for (Beacon beacon : beacons) {
            for (Observer observer : observers) {
                if (observer.isAwake()) {
                    double distance = distance(beacon.getLocation(), observer.getLocation());
                    if (distance <= radius) {
                        observer.observe(beacon.transmit());
                    }
                }
            }
        }
    }

    /** Pass current-round information of transmission data from all observers to the simulation's resolver. */
    void observersToResolver() {
        for (Observer observer : observers) {
            observer.passInformationToResolver();
        }
    }

    /** Update resolver's estimated board. */
    void resolverEstimate() { }

    /** Write current-round state of the simulation to db. */
    void writeRoundState() { }

    /** Gather statistical data of the current round and update the aggregated simulation statistics based on all rounds. */
    abstract void updateSimulationStats();

    /** Write final simulation statistical data to db. */
    void writeSimulationStats() { }

    /**
     * An abstract builder class designed to separate the construction of a simulation from its representation.
     * Designated for:
     * - Construction of a new simulation, based on parameters given by an end-user.
     * - Construction of a restored simulation, based on data read from the db.
     */
    public static abstract class Builder {

        protected String id;
        protected int currentRound = 0;
        protected int maxNumberOfRounds;
        protected int rowNum;
        protected int colNum;
        protected RealBoard realBoard;
        protected IResolver resolver;
        protected int beaconsNum;
        protected int observersNum;
        protected List<Beacon> beacons = new ArrayList<>();
        protected List<Observer> observers = new ArrayList<>();
        protected IMovementStrategy beaconMovementStrategy;
        protected IMovementStrategy observerMovementStrategy;
        protected AwakenessStrategyFactory.Type awakenessStrategyType;
        protected double radius;
        protected int awakenessCycle;
        protected int awakenessDuration;

        /**
         * Set simulation id.
         * @param id is a unique simulation id.
         * @return this, to provide chaining.
         */
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Set number of rows in simulation.
         * @param rowNum is the number of rows in simulation.
         * @return this, to provide chaining.
         */
        public Builder setRowNum(int rowNum) {
            this.rowNum = rowNum;
            return this;
        }

        /**
         * Set number of columns in simulation.
         * @param colNum is the number of columns in simulation.
         * @return this, to provide chaining.
         */
        public Builder setColNum(int colNum) {
            this.colNum = colNum;
            return this;
        }

        /**
         * Set number of beacons in simulation.
         * @param beaconsNum is the number of beacons in simulation.
         * @return this, to provide chaining.
         */
        public Builder setBeaconsNum(int beaconsNum) {
            this.beaconsNum = beaconsNum;
            return this;
        }

        /**
         * Set number of observers in simulation.
         * @param observersNum is the number of observers in simulation.
         * @return this, to provide chaining.
         */
        public Builder setObserversNum(int observersNum) {
            this.observersNum = observersNum;
            return this;
        }

        /**
         * Set the threshold transmission radius.
         * @param radius is the threshold transmission radius
         * @return this, to provide chaining.
         */
        public Builder setRadius(double radius) {
            this.radius = radius;
            return this;
        }

        /**
         * Set the awakeness cycle, which is the number of rounds in which every observer must have
         * an awakeness period.
         * @param awakenessCycle is the number of rounds to be considered as the cycle.
         * @return this, to provide chaining.
         */
        public Builder setAwakenessCycle(int awakenessCycle) {
            this.awakenessCycle = awakenessCycle;
            return this;
        }

        /**
         * Set the awakeness duration, which is the number of rounds in which an observer is awake in a
         * single awakeness cycle.
         * @param awakenessDuration is the number of rounds to be considered as the duration.
         * @return this, to provide chaining.
         */
        public Builder setAwakenessDuration(int awakenessDuration) {
            this.awakenessDuration = awakenessDuration;
            return this;
        }

        /**
         * Set the observers' awakeness strategy type, used for generating awakeness strategies for all observers.
         * @param awakenessStrategyType is the awakeness strategy type for all observers.
         * @return this, to provide chaining.
         */
        public Builder setAwakenessStrategyType(AwakenessStrategyFactory.Type awakenessStrategyType) {
            this.awakenessStrategyType = awakenessStrategyType;
            return this;
        }

        /**
         * Set initial round index.
         * @param currentRound is the round index to start the new simulation run from.
         * @return this, to provide chaining.
         */
        public Builder setCurrentRound(int currentRound) {
            this.currentRound = currentRound;
            return this;
        }

        /**
         * Set maximum number of rounds in simulation as the index of the last simulation round.
         * @param maxNumberOfRounds is the maximum number of rounds in simulation (index of last round).
         * @return this, to provide chaining.
         */
        public Builder setMaxNumberOfRounds(int maxNumberOfRounds) {
            this.maxNumberOfRounds = maxNumberOfRounds;
            return this;
        }

        /**
         * Set initial real board of simulation.
         * @param realBoard is the real board.
         * @return this, to provide chaining.
         */
        public Builder setRealBoard(RealBoard realBoard) {
            this.realBoard = realBoard;
            return this;
        }

        /**
         * Set simulation resolver.
         * @param resolver is the resolver.
         * @return this, to provide chaining.
         */
        public Builder setResolver(IResolver resolver) {
            this.resolver = resolver;
            return this;
        }

        /**
         * Set list of beacons associated with the simulation.
         * @param beacons is the list of beacons.
         * @return this, to provide chaining.
         */
        public Builder setBeacons(List<Beacon> beacons) {
            this.beacons = beacons;
            return this;
        }

        /**
         * Set list of observers associated with the simulation.
         * @param observers is the list of observers.
         * @return this, to provide chaining.
         */
        public Builder setObservers(List<Observer> observers) {
            this.observers = observers;
            return this;
        }

        /**
         * Set the beacons' movement strategy.
         * @param beaconMovementStrategy is the movement strategy for all beacons.
         * @return this, to provide chaining.
         */
        public Builder setBeaconMovementStrategy(IMovementStrategy beaconMovementStrategy) {
            this.beaconMovementStrategy = beaconMovementStrategy;
            return this;
        }

        /**
         * Set the observers' movement strategy.
         * @param observerMovementStrategy is the movement strategy for all observers.
         * @return this, to provide chaining.
         */
        public Builder setObserverMovementStrategy(IMovementStrategy observerMovementStrategy) {
            this.observerMovementStrategy = observerMovementStrategy;
            return this;
        }

        /**
         * Validate all simulation builder arguments are legal, when a simulation is constructed using {@code buildNew()}.
         */
        abstract void validateNewSimulationArguments();

        /**
         * Validate all simulation builder arguments are legal, when a simulation is constructed using {@code buildRestored()}.
         */
        abstract void validateRestoredSimulationArguments();

        /**
         * Create and initialize simulation observers in random initial locations using a factory, and store them in observers container.
         * Initializes the observers movement strategy according to the strategy passed to the builder.
         * Initializes the observers awakeness strategy according to the strategy type, awakeness cycle and awakeness duration
         * passed to the builder.
         */
        abstract void initializeObservers();

        /**
         * Create and initialize simulation beacons in random initial locations using a factory, and store them in observers container.
         * Create simple beacons for a tracing simulation and swapping beacons for a stalking simulation.
         * Initializes the beacons movement strategy according to the strategy passed to the builder.
         */
        abstract void initializeBeacons();

        /**
         * Construct a new simulation object.
         * @return a new Simulation object constructed with the builder parameters.
         */
        public abstract AbstractSimulation buildNew();

        /**
         * Construct a restored simulation object.
         * @return a new Simulation object constructed with the builder parameters.
         */
        public abstract AbstractSimulation buildRestored();
    }

    // A protected constructor used by the concrete simulation classes' constructors.
    protected AbstractSimulation (Builder builder) {
        this.id = builder.id;
        this.maxNumberOfRounds = builder.maxNumberOfRounds;
        this.board = builder.realBoard;
        this.resolver = builder.resolver;
        this.radius = builder.radius;
        this.beacons = ImmutableList.copyOf(builder.beacons);
        this.observers = ImmutableList.copyOf(builder.observers);
    }

    private double distance(Location firstLocation, Location secondLocation) {
        return (double) (Math.abs(firstLocation.row - secondLocation.row) + Math.abs(firstLocation.col - secondLocation.col));
    }
}
