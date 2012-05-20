package net.gps.tracker.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.*;
import net.gps.tracker.server.PersistencyManager;

import net.gps.tracker.shared.User;

public class GetUserStatus extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        PersistencyManager pm = new PersistencyManager();
        String query = request.getQueryString();
        int i = query.indexOf('=');
        String name = i>=0?query.substring(0,i):query;
        String value = i>=0?query.substring(i+1):null;
        User user = pm.getUser(name);
        PrintWriter writer = response.getWriter();
        if (user == null) writer.println("UNKNOWN");
        if (value == null) writer.println(user.getStatus());
        else {user.setStatus(value); pm.updateUser(user);}
    }
}
