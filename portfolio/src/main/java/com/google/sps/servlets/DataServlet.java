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

import com.google.sps.data.Comment;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import com.google.gson.Gson; 

@WebServlet("/data")
public class DataServlet extends HttpServlet {

	private List<Comment> comments;
    static final int DEFAULT_COMMENTS_NUMBER = 5;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int maxNumComments = 0;
        String maxNumCommentsStr = request.getParameter("comments-number");
        if (maxNumCommentsStr.isEmpty()) {
            maxNumComments = DEFAULT_COMMENTS_NUMBER;
        } else {
            try {
                maxNumComments = Integer.parseInt(maxNumCommentsStr);
            } catch (NumberFormatException e) {
                System.err.println("Could not convert to int: " + maxNumCommentsStr);
            }
        }

        String page = request.getParameter("page");

        Query query = new Query("Comment-" + page).addSort("date", SortDirection.DESCENDING);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        comments = new ArrayList<>();
        String author, text;
        Date date;
        for (Entity entity : results.asIterable()) {
            try {
                author = (String) entity.getProperty("author");
                text = (String) entity.getProperty("text");
                date = (Date) entity.getProperty("date");
            } catch (ClassCastException e) {
                System.err.println("Could not cast entry property");
                break;                
            }

            Comment comment = new Comment(text, author, date);
            comments.add(comment);

            maxNumComments --; 
            if (maxNumComments <= 0) break;
        }

        Gson gson = new Gson();

        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(comments));
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String author = getParameter(request, "author", "unknown");
        String text = getParameter(request, "text", "");
        String page = getParameter(request, "page", "unknown");
        Date date = new Date();

        Entity commentEntity = new Entity("Comment-" + page);
        commentEntity.setProperty("author", author);
        commentEntity.setProperty("text", text);
        commentEntity.setProperty("date", date);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);

        response.sendRedirect(page);
    }

    /**
    * Gets parameter from the list and changes the value by default if empty
    */
    private String getParameter(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

}
