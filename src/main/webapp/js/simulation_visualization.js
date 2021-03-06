// Copyright 2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import { toQueryString, getUrlVars, sleep } from './utils.js';

// Add function to global scope.
window.visualize = startVisualization;

// The timeout variable.
var simulationTimeout;

// Holds the simulation's metadata.
var simulation;

// Holds the simulation's visualization state.
var state = {
    currentRound: 0,
    isPaused: false,
    isFinished: false,
}

/**
 * Update simulation to holds simulation's metadata. 
 * Initialize the visualization of the first round.
*/
async function startVisualization() {
    simulation = getUrlVars();
    var mainHeader = document.getElementById('simulation-visualization-header');
    mainHeader.innerText = 'Visualizing Simulation\n' + simulation.id;

    // Define the pause-button behavior.
    var pauseButton = document.getElementById('pause-button');
    pauseButton.onclick = function() {
        if (pauseButton.innerHTML === "Pause") {
            pause();
        } else {
            resume();
        }
    }
    nextRound();
}

/** Visualize the next round of the simulation. */
async function nextRound() {
    var roundHeader = document.getElementById('current-round-header');
    roundHeader.innerText = 'Current Round: ' + state.currentRound;

    var params = {
        simulationId: simulation.id,
        round: state.currentRound,
    };

    // Request real board state.
    params['isReal'] = true;
    await fetchReadBoardState(params, 'real-board-table');

    // Request estimated board state.
    params['isReal'] = false;
    await fetchReadBoardState(params, 'estimated-board-table');
    
    // Update the simulation's state and the timeout.
    updateState();
    if (!state.isPaused && !state.isFinished) {
        var delay = 1/document.getElementById('speed-controller').value;
        simulationTimeout = setTimeout(nextRound, delay);
    }

    // Visualize the statistics if the simulation is over.
    if (state.isFinished) {
        resetOrDisplayStats();
    }
}

/** Allow the user to decide whether they want to display the statistics or visualize the simulation again. */
function resetOrDisplayStats() {
    var flowButtons = document.getElementById('flow-buttons');
    var resetButton = document.createElement("button");
    resetButton.innerHTML = "reset";
    resetButton.addEventListener("click", reset);
    flowButtons.appendChild(resetButton);

    var displayStatsButton = document.createElement("button");
    displayStatsButton.innerHTML = "display statistics";
    displayStatsButton.addEventListener("click", displayStats);
    flowButtons.appendChild(displayStatsButton);
}

/** Update the current round. */
function updateState() {
    state.currentRound += 1;
    if (state.currentRound >= simulation.roundsNum) {
        state.isFinished = true;
    }
}

/** Continue the visualization. */
function resume() {
    var pauseButton = document.getElementById('pause-button');
    pauseButton.innerHTML = "Pause";

    state.isPaused = false;
    var delay = 1/document.getElementById('speed-controller').value;
    simulationTimeout = setTimeout(nextRound, delay);
}

/** Stop the visualization. */
function pause() {
    var pauseButton = document.getElementById('pause-button');
    pauseButton.innerHTML = "Play";
    
    state.isPaused = true;
    clearTimeout(simulationTimeout);
}

/**
 * Fetch board state from servlet by given parameters, and update an HTML table corresponding to given
 * board element id to visualize the board's state.
 * @param {Object} params is an object storing parameters for http request.
 * @param {String} boardElementId is the id of the html table element to be updated.
 */
async function fetchReadBoardState(params, boardElementId) {
    const queryString = toQueryString(params);
    fetch(`/read-board-state?${queryString}`)
    .then(response => response.json())
    .then(boardState => visualizeBoardState(boardState.array, boardElementId));
}

/**
 * Given a board state and an id of an html table element, update the table to display the board state.
 * @param {String[][]} board is the board state to visualize. 
 * @param {String} tableId is the id of the html table element to be updated.
 */
function visualizeBoardState(board, tableId) {

    // Set table and table body
    var boardTable = document.getElementById(tableId);
    var boardTableBody = document.createElement('tbody');

    // Change cells' classes according to board.
    board.forEach(function(rowData) {

        var row = document.createElement('tr');
        row.classList.add('board-tr')

        rowData.forEach(function(cellData) {
            var cell = document.createElement('td');
            cell.classList.add('board-td');
            updateCell(cell, cellData);
            row.appendChild(cell);
        });

        boardTableBody.appendChild(row);
    });

    // Clear last state of table (if table body exists).
    var oldTableBodies = boardTable.tBodies;
    if (oldTableBodies.length > 0) {
        boardTable.removeChild(oldTableBodies[0]);
    }
    
    // Append updated table body.
    boardTable.appendChild(boardTableBody);
}

/**
 * Update the cell according to the agents inside it.
 * @param {HTMLTableDataCellElement} cell is an HTML element from type <td>, representing a cell on the board. 
 * @param {String[]} agents is a list of agents ids located inside the cell. 
 */
function updateCell(cell, agents) {
    var beaconsIdsInsideCell = extractBeaconsIds(agents);
    cell.setAttribute('title', beaconsIdsInsideCell.join(","));

    var beaconToFocusOn = document.getElementById("beacon-id").value;
    var isBeaconToFocusOnInCell = beaconToFocusOn != "" && beaconsIdsInsideCell.includes(beaconToFocusOn);
    cell.classList.add(determineCellClass(agents, beaconsIdsInsideCell.length, isBeaconToFocusOnInCell));
}

/**
 * return the css class name representing the correct state of the cell (empty/contains beacons/observers only).
 * @param {String[]} agents a list of agents ids located in the same cell of the board.
 * @returns {String} the correct css class name.
 */
function determineCellClass(agents, numberOfBeacons, isBeaconToFocusOnInCell) {
    if (agents.length === 0) { 
        return 'board-td-empty'; 
    }
    if (isBeaconToFocusOnInCell) {
        return 'board-td-contains-wanted-beacon';
    }
    if (numberOfBeacons > 0) {
       return 'board-td-contains-beacon';
    }
    return 'board-td-observers-only';
}

/**
 * Returns a sorted list of all the beacons' ids in the given list.
 * @param {String[]} agents is a list of agents ids located in the same cell of the board. 
 */
function extractBeaconsIds(agents) {
    var beaconsIds = agents.filter(agent => agent.charAt(0) === 'B')
                    .map(agent => agent.slice(6));
    return beaconsIds.sort((a, b) => parseInt(a) - parseInt(b));
}

/** Display the statistical data of a simulation. */
function displayStats() {
    clearFlowButtons();
    clearVisualizationElements();
    var params = {simulationId: simulation.id};
    const queryString = toQueryString(params);

    fetch(`/read-distance-stats?${queryString}`)
    .then(response => response.json())
    .then(stats => createStatsTable(stats));

    fetch(`/read-observed-stats?${queryString}`)
    .then(response => response.text())
    .then(response => JSON.parse(response.replace(/\bNaN\b/g, "null")))
    .then(stats => createObservedStatsTable(stats.rowMap));
}

/**
 * Create an HTML table element and fill it with statistical data.
 * @param {Object} stats is an object storing the simulations' stats.
 */
function createStatsTable(stats) {
    var title = document.createElement('h4');
    var table = document.createElement('table');
    table.classList.add('stats-table');
    var header = table.createTHead();
    var headerRow = header.insertRow(0);
    var dataRow = table.insertRow(1);

    var i = 0;
    Object.keys(stats).forEach(measure => {
        headerRow.insertCell(i).innerText = measure;
        dataRow.insertCell(i).innerText = stats[measure].toFixed(3);
        i++;
    });
    document.body.appendChild(title);
    document.body.appendChild(table);
}

/**
 * Create an HTML table element and fill it with statistics of observed intervals.
 * @param {Object} stats is an object storing the observed statistics.
 */
function createObservedStatsTable(stats) {
    var title = document.createElement('h4');
    title.innerText = "Observed Statistics";

    var table = document.createElement('table');
    table.classList.add('stats-table');
    var measures = Object.keys(stats[Object.keys(stats)[0]]);
    addHeader(table, measures);
    addStatisticsRows(table, stats);

    document.body.appendChild(title);
    document.body.appendChild(table);
}

/**
 * Given a table and an object that contains the header properties, add a header based on the object's items.
 * @param {HTMLElement} table is the html table to be updated.
 * @param {Object} properties is an object that contains the header properties.
 */
function addHeader(table, properties) {
    var header = table.createTHead();
    var row = header.insertRow(0);

    row.insertCell(0).innerHTML = "beacon's Id";
    for (var i = 0; i < properties.length; i++) {
        row.insertCell(i + 1).innerHTML = properties[i];
    }
}

/**
 * Given a table and an object storing beacons' stats, add a row for each beacon.
 * @param {HTMLElement} table is the table to be updated.
 * @param {Object} stats is an object storing beacons' id and stats.
 */
function addStatisticsRows(table, stats) {
    var prefix = 'beacon #';
    var hasNoValue = "-";

    for (const id in stats) {
        var row = table.insertRow(-1);
        row.insertCell(0).innerHTML = prefix + id;
        var i = 1; // cell index (cell 0 is a button).
        for (const property in stats[id]) {
            var value = stats[id][property]
            row.insertCell(i++).innerHTML = (value !== null) ? value.toFixed(3) : hasNoValue;
        }
    }
}

/** Clear all HTML element used for visualization. */
function clearVisualizationElements() {
    document.getElementsByClassName('boards')[0].innerHTML = '';
    document.getElementsByClassName('choose-beacon')[0].innerHTML = '';
    document.getElementsByClassName('speed-controller')[0].innerHTML = '';
    document.getElementsByClassName('pause-button')[0].innerHTML = '';
    document.getElementsByClassName('legend')[0].innerHTML = '';
    document.getElementById('current-round-header').innerHTML = '';
}

/** Start the simulation from the first round. */
function reset() {
    clearFlowButtons();
    state.isFinished = false;
    state.currentRound = 0;
    nextRound();
}

/** Clear the buttons that allow the user to decide whether they want to display the statistics or visualize the simulation again. */
function clearFlowButtons() {
    var flowButtons = document.getElementById('flow-buttons');
    flowButtons.innerHTML = '';
}