// Copyright 2019 Google LLC
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

package com.google.research.bleth.servlets;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.repackaged.com.google.common.base.Ascii;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.research.bleth.simulator.SimulationMetadata;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** A servlet used for displaying simulations. */
@WebServlet("/list-simulations")
public class ListSimulationsServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson = new Gson(); // Used for json serialization.

        Optional<String> sortProperty = Optional.empty();
        Optional<Query.SortDirection> sortDirection = Optional.empty();
        if (request.getParameter("sortProperty") != null) {
            sortProperty = Optional.of(request.getParameter("sortProperty"));
        }
        if (request.getParameter("sortDirection") != null) {
            String sortDirectionString = Ascii.toUpperCase(request.getParameter("sortDirection"));
            sortDirection = Optional.of(Query.SortDirection.valueOf(sortDirectionString));
        }

        LinkedHashMap<String, JsonElement> simulationsAsJson = new LinkedHashMap<>();
        SimulationMetadata.listSimulations(sortProperty, sortDirection)
                .forEach((id, metadata) -> simulationsAsJson.put(id, gson.toJsonTree(metadata)));

        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(simulationsAsJson));
    }
}
