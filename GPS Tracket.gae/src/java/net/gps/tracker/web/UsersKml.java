package net.gps.tracker.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.*;
import net.gps.tracker.server.PersistencyManager;

import net.gps.tracker.shared.Coordinate;
import net.gps.tracker.shared.User;

public class UsersKml extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.google-earth.kml+xml");
        PersistencyManager pm = new PersistencyManager();
        Map<User, Coordinate> list = pm.listCoordinates();

        response.setHeader("content-disposition",
                "attachment; filename=Coordinates.kml");

        PrintWriter writer = response.getWriter();

        writer.println("<?xml version='1.0' encoding='UTF-8'?>");
        writer.println("<kml xmlns='http://www.opengis.net/kml/2.2'>");
        writer.println("  <Document>");
        writer.println("    <name>Coordinates</name>");
        writer.println("    <description/>");

        DateFormat DTF = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT, request.getLocale());

        for (User user : list.keySet()) {
            if (list.get(user) == null) {
                continue;
            }
            Coordinate coordinate = list.get(user);
            writer.println("    <Placemark>");
            writer.println("      <name>" + user.getName() + "</name>");
            writer.println("      <description>");
            DTF.setTimeZone(TimeZone.getTimeZone(coordinate.getTimezone()));
            writer.println(DTF.format(coordinate.getTimestamp()));
            //writer.println(coordinate.getLatitude()+","+coordinate.getLongitude());
            writer.println("speed: " + coordinate.getSpeed() + " mt/sec");
            writer.println("altitude: " + coordinate.getAltitude() + " mt");
            writer.println("course: " + coordinate.getCourse());
            writer.println("      </description>");
            writer.println("      <Point>");
            writer.println("	    <coordinates>");
            writer.println(coordinate.getLongitude() + ", " + coordinate.getLatitude() + ", 1");
            writer.println("	    </coordinates>");
            writer.println("      </Point>");
            writer.println("    </Placemark>");
        }
        writer.println("  </Document>");
        writer.println("</kml>");
    }
}
