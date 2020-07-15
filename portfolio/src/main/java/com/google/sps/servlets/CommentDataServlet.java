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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Maintain and return data on number of submitted comments as JSON object. */
@WebServlet("/comment-data")
public class CommentDataServlet extends HttpServlet {

  private static final int MAX_QUERIES = 1000;

  // Map from time of chart update request to number of submitted comments.
  private final LinkedHashMap<Date, Integer> totalDailyComments = new LinkedHashMap<>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(totalDailyComments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(Comment.getEntityKind());
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    int comments = datastore.prepare(query).countEntities(
        FetchOptions.Builder.withLimit(MAX_QUERIES)
    );
    
    Date date = new Date();
    totalDailyComments.put(date, comments);

    response.sendRedirect("/random.html");
  }
}
