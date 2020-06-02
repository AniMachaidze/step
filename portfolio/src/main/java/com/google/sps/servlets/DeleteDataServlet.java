package com.google.sps.servlets;

import com.google.sps.data.Comment;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Query query = new Query("Comment");
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);
        
        List<Entity> comments = new ArrayList<>();
        for (Entity entity : results.asIterable()) {
            comments.add(entity);
        }
        
        for (int i = 0; i < comments.size(); i++) {
            Key commentEntityKey = comments.get(i).getKey();
            datastore.delete(commentEntityKey);
        }

        response.setContentType("text/plain");
        response.getWriter().println("");
    }
}
