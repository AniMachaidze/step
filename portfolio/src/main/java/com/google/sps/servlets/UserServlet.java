package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/user")
public class UserServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
        String page = request.getParameter("page");

        UserService userService = UserServiceFactory.getUserService();
        response.setContentType("application/json;");

        if (userService.isUserLoggedIn()) {
            String userEmail = userService.getCurrentUser().getEmail();
            String urlToRedirectToAfterUserLogsOut = page;
            String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);

            String json = "{";
            json += "\"loggedin\": ";
            json += "\"true\"";
            json += ", ";
            json += "\"logoutUrl\": ";
            json += "\"" + logoutUrl + "\"";
            json += "}";

            response.getWriter().println(json);
        } else {
            String urlToRedirectToAfterUserLogsIn = page;
            String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);

            String json = "{";
            json += "\"loggedin\": ";
            json += "\"false\"";
            json += ", ";
            json += "\"loginUrl\": ";
            json += "\"" + loginUrl + "\"";
            json += "}";

            response.getWriter().println(json);
        }
    }
}
