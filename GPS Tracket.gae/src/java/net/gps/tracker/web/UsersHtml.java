package net.gps.tracker.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.*;
import net.gps.tracker.server.PersistencyManager;

import net.gps.tracker.shared.Coordinate;
import net.gps.tracker.shared.User;

public class UsersHtml extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PersistencyManager pm = new PersistencyManager();
        Map<User, Coordinate> list = pm.listCoordinates();

        PrintWriter writer = response.getWriter();

        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Users Coordinates</title>");
        writer.println("<meta http-equiv='refresh' content='60'/>");
        writer.println("</head>");

        writer.println("<body>");
        writer.println("<center>");

        writer.println("<a href='javascript:location.reload(true)'>");

        writer.print("<img border='0' style='height:100%' src='");
        writer.print("http://maps.google.com/maps/api/staticmap?");
        writer.print("size=480x640&sensor=true&maptype=roadmap");

        int i = 1;
        StringBuilder marks = new StringBuilder();
        for (User user : list.keySet()) {
            if (list.get(user) == null) {
                continue;
            }
            Coordinate coordinate = list.get(user);
            writer.print("&markers=label:" + i + "|"
                    + coordinate.getLatitude() + "," + coordinate.getLongitude());
            marks.append("" + i + ":" + user.getName() + "\n");
            i++;
        }
        writer.println("'/>");

        writer.println("</a>");

        writer.println("<div>");
        writer.println(marks);
        writer.println("</div>");

        writer.println("</center>");
        writer.println("</body>");
        writer.println("</html>");
    }
}
