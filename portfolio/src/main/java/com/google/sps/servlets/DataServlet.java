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


 

@WebServlet("/data")
public class DataServlet extends HttpServlet {

	private List<Comment> comments;

 	@Override
    public void init() {
        comments = new ArrayList<>();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Query query = new Query("Comment").addSort("date", SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        for (Entity entity : results.asIterable()) {
            String author = (String) entity.getProperty("author");
            String text = (String) entity.getProperty("text");
            Date date = (Date) entity.getProperty("date");

            Comment comment = new Comment(text, author, date);
            comments.add(comment);
        }

        // TODO: use GSON instead   
        String json = "{ \"comments\": [";

        for (int i = 0 ; i < comments.size(); i++) {
            json += convertToJson(comments.get(i));
            if (i != comments.size() - 1) {
                json += ",";
            }
        }
        
        json += "] }";

        response.setContentType("application/json;");
        response.getWriter().println(json);

/*
        Gson gson = new Gson();

        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(tasks));
*/
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String author = getParameter(request, "author", "unknown");
        String text = getParameter(request, "text", "");
        long date = System.currentTimeMillis();

        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("author", author);
        commentEntity.setProperty("text", text);
        commentEntity.setProperty("date", new Date());

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);

        response.sendRedirect("/index.html");
    }

	/**
    * Converts Comment object to JSON object
    */
    private String convertToJson(Comment comment) {
        String json = "{";
        json += "\"author\": ";
        json += "\"" + comment.getAuthor() + "\"";
        json += ", ";
        json += "\"content\": ";
        json += "\"" + comment.getContent() + "\"";
        json += ", ";
        json += "\"date\": ";
        json += "\"" + comment.getDate() + "\"";
        json += "}"; 
        return json;
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
