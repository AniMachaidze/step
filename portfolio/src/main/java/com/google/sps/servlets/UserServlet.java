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

        if (userService.isUserLoggedIn()) {
            String userEmail = userService.getCurrentUser().getEmail();
            //TODO: change hardcoded redirection link to a variable
            String urlToRedirectToAfterUserLogsOut = "/career.html";
            String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);

            response.setContentType("application/json;");

            String json = "{";
            json += "\"loggedin\": ";
            json += "\"" + true + "\"";
            json += ", ";
            json += "\"logoutUrl\": ";
            json += "\"" + logoutUrl + "\"";
            json += "}";

            response.getWriter().println(json);
        } else {
            //TODO: change hardcoded redirection link to a variable
            String urlToRedirectToAfterUserLogsIn = "/career.html";
            String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);

            response.sendRedirect(loginUrl);
        }
    }
}
