package net.gps.tracker.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.*;
import net.gps.tracker.server.PersistencyManager;

import net.gps.tracker.shared.Coordinate;
import net.gps.tracker.shared.User;

public class GetUsersLocation extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        PersistencyManager pm = new PersistencyManager();
        Map<User, Coordinate> list = pm.listCoordinates();
        PrintWriter writer = response.getWriter();
        writer.println("User,TimeStamp,TimeZone,Speed,Course,Latitude,Longitude,Altitude");
        for (User user : list.keySet()) {
            if (list.get(user) == null) {
                continue;
            }
            Coordinate coordinate = list.get(user);
            writer.print(user.getName());
            writer.print(',');
            writer.print(coordinate.getTimestamp());
            writer.print(',');
            writer.print(coordinate.getTimezone());
            writer.print(',');
            writer.print(coordinate.getSpeed());
            writer.print(',');
            writer.print(coordinate.getCourse());
            writer.print(',');
            writer.print(coordinate.getLatitude());
            writer.print(',');
            writer.print(coordinate.getLongitude());
            writer.print(',');
            writer.print(coordinate.getAltitude());
            writer.print('\n');
        }
    }
}
