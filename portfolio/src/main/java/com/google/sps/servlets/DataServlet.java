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

	private List<String> comments;

 	@Override
    public void init() {
        comments = new ArrayList<>();

        comments.add("Your portfolio is amazing!");
        comments.add("It looks good, could be better");
        comments.add("Add more content!");
        comments.add("I love the design, but try to add more info");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String author = "me";
        Date date = new Date();

        for (String commentary: comments) {
            // Create comment object and convert to json string
            Comment comment = new Comment(commentary, author, date);
            String jsonComment = convertToJson(comment);

        	// Send the JSON as the response
            response.setContentType("application/json;");
        	response.getWriter().println(jsonComment);
        }
    }

    private String convertToJson(Comment comment) {
        String json = "{";
        json += "\"author\": ";
        json += "\"" + comment.getAuthor() + "\"";
        json += ", ";
        json += "\"content\": ";
        json += "\"" + comment.getContent() + "\"";
        json += ", ";
        json += "\"date\": ";
        json += comment.getDate();
        json += "}";
        return json;
 	}


}



