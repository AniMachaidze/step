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
import java.util.UUID;
import com.google.gson.Gson;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private List<Comment> comments;
    static final int DEFAULT_COMMENTS_NUMBER = 5;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
        int maxNumComments = 0;
        String maxNumCommentsStr = request.getParameter("comments-number");
        if (maxNumCommentsStr.isEmpty()) {
            maxNumComments = DEFAULT_COMMENTS_NUMBER;
        } else {
            try {
                maxNumComments = Integer.parseInt(maxNumCommentsStr);
            } catch (NumberFormatException e) {
                System.err.println("Could not convert to int: " +
                    maxNumCommentsStr);
            }
        }

        String page = request.getParameter("page");

        String currentUserEmail = null;
        UserService userService = UserServiceFactory.getUserService();
        if (userService.getCurrentUser() != null) {
            currentUserEmail = userService.getCurrentUser().getEmail();
        }

        Query query = new Query("Comment-" + page)
            .addSort("date", SortDirection.DESCENDING);
        DatastoreService datastore = DatastoreServiceFactory
            .getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        comments = new ArrayList<>();
        String userName, userEmail, text, emotion, id;
        Date date;
        boolean isAbleToDelete = false;
        for (Entity entity: results.asIterable()) {
            try {
                userName = (String) entity.getProperty("userName");
                userEmail = (String) entity.getProperty("userEmail");
                text = (String) entity.getProperty("text");
                date = (Date) entity.getProperty("date");
                emotion = (String) entity.getProperty("emotion");
                id = (String) entity.getProperty("uuid");
                if (currentUserEmail != null && userEmail.equals(currentUserEmail)) {
                    isAbleToDelete = true;
                } else {
                    isAbleToDelete = false;
                }
            } catch (ClassCastException e) {
                System.err.println("Could not cast entry property");
                break;
            }

            comments.add(new Comment(text, userName, userEmail, date,
                emotion, isAbleToDelete, id));
            maxNumComments--;
            if (maxNumComments <= 0) break;
        }

        Gson gson = new Gson();

        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(comments));
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
        String userName = getParameter(request, "author", "unknown");
        String text = getParameter(request, "text", "");
        String page = getParameter(request, "page", "unknown");
        String emotion = getParameter(request, "emotion", "");
        Date date = new Date();

        UserService userService = UserServiceFactory.getUserService();
        String userEmail = userService.getCurrentUser().getEmail();

        UUID id = UUID.randomUUID();
        while (collides(id, page)) {
            id = UUID.randomUUID();
        }

        Entity commentEntity = new Entity("Comment-" + page);
        commentEntity.setProperty("userEmail", userEmail);
        commentEntity.setProperty("userName", userName);
        commentEntity.setProperty("text", text);
        commentEntity.setProperty("date", date);
        commentEntity.setProperty("emotion", emotion);
        commentEntity.setProperty("uuid", id.toString());

        DatastoreService datastore = DatastoreServiceFactory
            .getDatastoreService();
        datastore.put(commentEntity);

        response.sendRedirect(page);
    }

    /**
     * Gets parameter from the list and changes the value by default if empty
     */
    private String getParameter(HttpServletRequest request, String name,
        String defaultValue) {
        String value = request.getParameter(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Checks if the id collides with other ids in datastore
     */
    private boolean collides(UUID id, String page) {
        Query query = new Query("Comment-" + page);

        Filter uuidPropertyFilter = new FilterPredicate("uuid",
            FilterOperator.EQUAL, id.toString());
        query.setFilter(uuidPropertyFilter);
        DatastoreService datastore = DatastoreServiceFactory
            .getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        for (Entity entity: results.asIterable()) {
            String entityId = (String) entity.getProperty("uuid");
            if (entityId.equals(id.toString())) {
                return true;
            }
        }

        return false;
    }
}
