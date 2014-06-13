package net.gps.tracker.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.*;
import net.gps.tracker.server.PersistencyManager;

import net.gps.tracker.shared.Coordinate;
import net.gps.tracker.shared.User;

public class UserHtml extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PersistencyManager pm = new PersistencyManager();
        User user = pm.getUser(request.getQueryString());
        List<Coordinate> list = pm.listLastCoordinates(user.getId());

        PrintWriter writer = response.getWriter();

        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>" + user.getName() + "</title>");
        writer.println("<meta http-equiv='refresh' content='60'/>");
        writer.println("</head>");

        writer.println("<body>");
        writer.println("<center>");

        writer.println("<a href='javascript:location.reload(true)'>");

        writer.print("<img border='0' style='height:100%' src='");
        writer.print("http://maps.google.com/maps/api/staticmap?");
        writer.print("size=480x640&sensor=true&maptype=roadmap");
        if (list.size() > 0) {
            writer.print("&markers=");
            writer.print(list.get(0).getLatitude()
                    + "," + list.get(0).getLongitude());

            writer.print("&path=");
            int count = list.size() > 30 ? 30 : list.size();
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    writer.print("|");
                }
                writer.print(list.get(i).getLatitude()
                        + "," + list.get(i).getLongitude());
            }
        }
        writer.println("'/>");

        writer.println("</a>");

        writer.println("</center>");
        writer.println("</body>");
        writer.println("</html>");
    }
}
