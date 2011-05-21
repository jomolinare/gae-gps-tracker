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

public class UsersMap extends HttpServlet {

    private static String getKey(String server) {
        server = server.toLowerCase();
        if (server.endsWith("appspot.com")) {
            return appspot_com;
        }
        if (server.endsWith("innody.net")) {
            return innody_net;
        }
        return null;
    }
    private static final String appspot_com =
            "ABQIAAAAR7qxhD3wnCwgVNwnhjr4-RRlOb26qSyU154aZeLwOrF4C7-DphRW3y3Ihw2bOFFi85-UDjFNH9Kc0A";
    private static final String innody_net =
            "ABQIAAAAR7qxhD3wnCwgVNwnhjr4-RQ9Pcoldx_H2MLEIrh6LNcHRiy9VxSwXoqseBRyPglbHCJjqyK6Xml0_Q";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PersistencyManager pm = new PersistencyManager();
        Map<User, Coordinate> list = pm.listCoordinates();

        PrintWriter writer = response.getWriter();

        writer.println("<head>");
        writer.println("<title>GPS Map</title>");

        String key = getKey(request.getServerName());
        writer.println("<script type='text/javascript' ");
        writer.println("src='http://maps.google.com/maps?file=api&amp;v=2&amp;key=" + key + "'>");
        writer.println("</script>");

        writer.println("<script type='text/javascript'>");
        writer.println("function Location() {");
        writer.println("  var Name;");
        writer.println("  var Timestamp;");
        writer.println("  var Latitude;");
        writer.println("  var Longitude;");
        writer.println("  var Speed;");
        writer.println("  var Course;");
        writer.println("  var Altitude;");
        writer.println("}");

        DateFormat DTF = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT, request.getLocale());

        writer.println("var locations = new Array(" + list.size() + ");");

        int i = 0;
        for (User user : list.keySet()) {
            if (list.get(user) == null) {
                continue;
            }
            Coordinate coordinate = list.get(user);
            writer.println("locations[" + i + "] = new Location;");
            writer.println("locations[" + i + "].Name = '" + user.getName() + "';");
            DTF.setTimeZone(TimeZone.getTimeZone(coordinate.getTimezone()));
            writer.println("locations[" + i + "].Timestamp = '" + DTF.format(coordinate.getTimestamp()) + "';");
            writer.println("locations[" + i + "].GLatLng = new GLatLng(" + coordinate.getLatitude() + ", " + coordinate.getLongitude() + ");");
            writer.println("locations[" + i + "].Speed = " + coordinate.getSpeed() + ";");
            writer.println("locations[" + i + "].Course = " + coordinate.getCourse() + ";");
            writer.println("locations[" + i + "].Altitude = " + coordinate.getAltitude() + ";");
            i++;
        }

        writer.println("function DisplayMarkers() {");
        writer.println("    if (GBrowserIsCompatible()) {");
        writer.println("        if (locations.length == 0) return;");
        writer.println("        var map = new GMap2(document.getElementById('map'));");
        writer.println("        map.addControl(new GLargeMapControl());");
        writer.println("        map.addControl(new GMapTypeControl());");
        writer.println("        map.setCenter(locations[0].GLatLng, 14);");
        writer.println("        // Map Markers //");
        writer.println("        for (i = 0; i < locations.length; i++) {");
        writer.println("            var marker = new GMarker(locations[i].GLatLng,{ title: (i+1) });");
        writer.println("            marker.bindInfoWindow(");
        writer.println("                '<b>' + locations[i].Name + '</b><br/>' +");
        writer.println("                '<i>' + locations[i].Timestamp + '</i><br/>' +");
        writer.println("                '<u>Latitude: ' + locations[i].GLatLng.lat() + '</u><br/>' +");
        writer.println("                '<u>Longitude: ' + locations[i].GLatLng.lng() + '</u><br/>' +");
        writer.println("                '<i>Altitude: ' + locations[i].Altitude + ' mt</i><br/>' +");
        writer.println("                '<i>Course: ' + locations[i].Course + '</i><br/>' +");
        writer.println("                '<i>Speed: ' + locations[i].Speed + ' mt/sec</i>'");
        writer.println("            );");
        writer.println("            map.addOverlay(marker);");
        writer.println("        }");
        writer.println("    }");
        writer.println("}");

        writer.println("</script>");

        writer.println("</head>");
        writer.println("<body onload='DisplayMarkers()'>");
        writer.println("<table style='width:100%; height:100%'>");
        writer.println("<tr><td valign='top' style='width:100%; height:100%'>");
        writer.println("<div id='map' style='width:100%; height:100%; border-style:solid'></div>");
        writer.println("</td></tr>");
        writer.println("</table>");
        writer.println("</body>");
        writer.println("</html>");
    }
}
