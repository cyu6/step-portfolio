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

package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Returns COVID-19 data as a JSON object, e.g. {"2020-01-27": {totalCases: 2820, newCases: 20}} */
@WebServlet("/covid-data")
public class CovidDataServlet extends HttpServlet {

  private static class CovidData {
    private final int totalCases;
    private final int newCases;

    private CovidData(int totalCases, int newCases) {
      this.totalCases = totalCases;
      this.newCases = newCases;
    }
  }

  // Map from date to total cases and new cases of COVID-19.
  private LinkedHashMap<Date, Object> covidTotals = new LinkedHashMap<>();
  private static final Logger LOGGER = Logger.getLogger( CovidDataServlet.class.getName() );
  
  @Override
  public void init() {
    Scanner scanner = new Scanner(getServletContext().getResourceAsStream(
        "/WEB-INF/owid-covid-data.csv"));
    int row = 0;
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      row++;
      String[] cells = line.split(",");

      Date date = new Date();
      try {
        date = new SimpleDateFormat("yyyy-MM-dd").parse(cells[0]);
      } catch (ParseException pex) {
        pex.printStackTrace();
        LOGGER.log(Level.WARNING, "invalid date string");
        continue;
      }    

      // Validation checks for parsing COVID-19 numbers.
      int totalCases = 0;
      int newCases = 0;
      try {
        totalCases = Integer.valueOf(cells[1]);
        newCases = Integer.valueOf(cells[2]);
      } catch (NumberFormatException nfex) {
        LOGGER.log(Level.WARNING, "invalid parsing for row :" + row);
        continue;
      }

      if (totalCases < 0 || newCases < 0) {
        LOGGER.log(Level.WARNING, "number of cases cannot be negative");
        continue;
      }
      
      CovidData newData = new CovidData(totalCases, newCases);      
      covidTotals.put(date, newData);
    }
    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(covidTotals);
    response.getWriter().println(json);
  }
}
