package com.google.research.bleth.servlets;

import com.google.research.bleth.services.DatabaseService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/read-hard-coded-real-board-state")
public class ReadHardCodedRealBoardStateServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;");

        String randomBoardState = DatabaseService.getInstance().getRealBoardState("1", "1");
        response.getWriter().println(randomBoardState);
    }
}