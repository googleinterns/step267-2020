import { toQueryString } from './utils.js';

window.retrieveSimulations = retrieveSimulations; // Add function to global scope.

/**
 * Fetch url and retrieve a JSON object storing simulations' metadata,
 * and display as an html table.
 */
function retrieveSimulations() {
    fetch('/list-simulations')
    .then(response => response.json())
    .then(simulations => { 
        displaySimulationAsTable(simulations); 
    });
}

/**
 * Given a json object storing simulations' metadata, write all data to an html table.
 * @param {Object} simulations is an object storing simulations' unique id and metadata.
 */
function displaySimulationAsTable(simulations) {
    const allSimulationIds = Object.keys(simulations);
    if (allSimulationIds.length === 0) { return; }
    
    const firstSimulationId = allSimulationIds[0];
    const firstSimulation = simulations[firstSimulationId];
    const simulationProperties = Object.keys(firstSimulation);

    var table = document.getElementById("simulations-table");
    addSimulationHeader(table, simulationProperties);
    addSimulationRows(table, simulations);
}

/**
 * Given a table and an array of strings, add a header based on the array's items.
 * @param {HTMLElement} table is the html table to be updated.
 * @param {String[]} properties is an array of header properties.
 */
function addSimulationHeader(table, properties) {
    var header = table.createTHead();
    var row = header.insertRow(0);
    // cell 0 contains the simulation button, therefore header starts at cell 1.
    row.insertCell(0).innerHTML = '';
    for (var i = 0; i < properties.length; i++) {
        row.insertCell(i + 1).innerHTML = properties[i];
    }
}

/**
 * Given a table and an object storing simulations' metadata, add a row for each simulation.
 * @param {HTMLElement} table is the table to be updated.
 * @param {Object} simulations is an object storing simulations' id and metadata.
 */
function addSimulationRows(table, simulations) {
    for (const id in simulations) {
        const simulation = simulations[id];
        var row = table.insertRow(-1);
        const visualizeSimulationButton = createVisualizationButton(simulation, id);
        const deleteSimulationButton = createDeletionButton(id);
        const cell = row.insertCell(0);
        cell.appendChild(visualizeSimulationButton);
        cell.appendChild(deleteSimulationButton);
        var i = 1; // cell index (cell 0 is a button).
        for (const property in simulation) {
            row.insertCell(i++).innerHTML = simulation[property];
        }
    }
}

/**
 * Create a button for simulation visualization.
 * @param {Object} simulation is the simulation object.
 * @param {String} id is the simulation id. 
 */
function createVisualizationButton(simulation, id) {
    var visualizeSimulationButton = document.createElement('button');
    visualizeSimulationButton.innerText = 'Visualize Simulation';
    visualizeSimulationButton.addEventListener('click', () => {
        var simulationWithId = JSON.parse(JSON.stringify(simulation)); // Deep copy.
        simulationWithId['id'] = id;
        window.location.replace('simulation_visualization.html?' + toQueryString(simulationWithId));
    });

    return visualizeSimulationButton;
}

/**
 * Create a button for simulation deletion.
 * @param {String} id is the simulation id. 
 */
function createDeletionButton(id) {
    var deleteSimulationButton = document.createElement('button');
    deleteSimulationButton.innerText = 'Delete Simulation';
    deleteSimulationButton.addEventListener('click', () => {
        var confirmed = confirm('Do you want to delete this simulation?');
        if (confirmed) {
            var params = new URLSearchParams();
            params.append('simulationId', id);
            fetch('/delete-simulation', {method: 'POST', body: params})
            .then(response => response.text())
            .then(message => window.alert(message))
            .then(() => location.reload())
        }
    });

    return deleteSimulationButton;
}