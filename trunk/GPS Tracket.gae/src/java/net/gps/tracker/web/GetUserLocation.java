package net.gps.tracker.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.*;
import net.gps.tracker.server.PersistencyManager;

import net.gps.tracker.shared.Coordinate;
import net.gps.tracker.shared.User;

public class GetUserLocation extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        PersistencyManager pm = new PersistencyManager();
        User user = pm.getUser(request.getQueryString());
        List<Coordinate> list = pm.listCoordinates(user.getId());
        PrintWriter writer = response.getWriter();
        writer.println("TimeStamp,TimeZone,Speed,Course,Latitude,Longitude,Altitude");
        for (Coordinate coordinate : list) {
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
