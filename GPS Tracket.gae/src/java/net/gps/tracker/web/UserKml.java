package net.gps.tracker.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.*;
import net.gps.tracker.server.PersistencyManager;

import net.gps.tracker.shared.Coordinate;
import net.gps.tracker.shared.User;

public class UserKml extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.google-earth.kml+xml");
        PersistencyManager pm = new PersistencyManager();
        User user = pm.getUser(request.getQueryString());
        List<Coordinate> list = pm.listCoordinates(user.getId());

        response.setHeader("content-disposition",
                "attachment; filename=" + user.getName() + ".kml");

        PrintWriter writer = response.getWriter();

        writer.println("<?xml version='1.0' encoding='UTF-8'?>");
        writer.println("<kml xmlns='http://www.opengis.net/kml/2.2'>");
        writer.println("  <Document>");
        writer.println("    <name>" + user.getName() + "</name>");
        writer.println("    <description/>");
        writer.println("    <Style id='Style'>");
        writer.println("      <LineStyle>");
        writer.println("        <color>D000FFFF</color>");
        writer.println("        <width>4</width>");
        writer.println("      </LineStyle>");
        writer.println("      <PolyStyle>");
        writer.println("        <color>40FF0000</color>");
        writer.println("      </PolyStyle>");
        writer.println("    </Style>");

        List<Integer> Placemarks = new ArrayList<Integer>();

        Placemarks.add(0);
        Integer PlacemarkIndex = 0;
        for (int i = 1; i < list.size(); i++) {
            if (i > PlacemarkIndex + 10) {
                long T0 = list.get(i).getTimestamp().getTime();
                long T1 = list.get(i - 1).getTimestamp().getTime();
                if (T1 - T0 > 10 * 60 * 1000) {
                    Placemarks.add(PlacemarkIndex = i);
                }
            }
        }
        Placemarks.add(list.size());

        DateFormat DTF = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT, request.getLocale());

        for (int n = 1; n < Placemarks.size(); n++) {
            writer.println("    <Placemark>");
            writer.println("      <name>");
            Coordinate C = list.get(Placemarks.get(n - 1));
            DTF.setTimeZone(TimeZone.getTimeZone(C.getTimezone()));
            writer.println("        " + DTF.format(C.getTimestamp()));
            writer.println("      </name>");
            writer.println("      <description/>");
            writer.println("      <styleUrl>#Style</styleUrl>");
            writer.println("      <LineString>");
            writer.println("        <extrude>1</extrude>");
            writer.println("        <tessellate>1</tessellate>");
            writer.println("        <altitudeMode>relativeToGround</altitudeMode>");
            writer.println("	    <coordinates>");
            for (int i = Placemarks.get(n - 1); i < Placemarks.get(n); i++) {
                Coordinate coordinate = list.get(i);
                writer.println("	      "
                        + coordinate.getLongitude() + ", " + coordinate.getLatitude() + ", 1");
                //(coordinate.getAltitude()<0?0:coordinate.getAltitude()));
            }
            writer.println("	    </coordinates>");
            writer.println("      </LineString>");
            writer.println("    </Placemark>");
        }
        writer.println("  </Document>");
        writer.println("</kml>");
    }
}
