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
import java.util.ArrayList;
import java.util.List;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
        // TODO: Delete comments with id instead of parameters
        String page = request.getParameter("page");
        String text = request.getParameter("text");
        String author = request.getParameter("author");
        String emotion = request.getParameter("emotion");

        Query query = new Query("Comment-" + page);

        if (!text.equals("undefined")) {
            Filter textPropertyFilter = new FilterPredicate("text",
                FilterOperator.EQUAL, text);
            Filter authorPropertyFilter = new FilterPredicate("author",
                FilterOperator.EQUAL, author);
            Filter emotionPropertyFilter = new FilterPredicate("emotion",
                FilterOperator.EQUAL, emotion);
            CompositeFilter filter = CompositeFilterOperator
                .and(textPropertyFilter, authorPropertyFilter,
                    emotionPropertyFilter);
            query.setFilter(filter);
        }

        DatastoreService datastore = DatastoreServiceFactory
            .getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        List<Key> keys = new ArrayList<> ();
        for (Entity entity: results.asIterable()) {
            keys.add(entity.getKey());
        }

        datastore.delete(keys);
        response.setContentType("text/plain");
        response.getWriter().println("");
    }
}