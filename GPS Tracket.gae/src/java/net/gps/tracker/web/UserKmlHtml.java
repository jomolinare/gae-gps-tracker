package net.gps.tracker.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.*;

public class UserKmlHtml extends HttpServlet {

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
        String user = request.getQueryString();

        PrintWriter writer = response.getWriter();
        writer.println("<head>");
        writer.println("<title>GPS Map (" + user + ")</title>");

        String key = getKey(request.getServerName());
        writer.println("<script type='text/javascript' ");
        writer.println("src='http://www.google.com/jsapi?key=" + key + "'>");
        writer.println("</script>");

        writer.println("<script type='text/javascript'>");
        writer.println("function OnBodyLoad() {");
        writer.println("  google.earth.createInstance('map3d',ge_init,ge_error)");
        writer.println("}");
        writer.println("var ge;");
        writer.println("google.load('earth', '1');");
        writer.println("function ge_init(instance) {");
        writer.println("  ge = instance;");
        writer.println("  ge.getWindow().setVisibility(true);");
        writer.println("  var link = ge.createLink('');");
        writer.println("  link.setHref('http://gps-innody-net.appspot.com/kml/user?" + user + "');");
        writer.println("  var networkLink = ge.createNetworkLink('');");
        // Sets the link, refreshVisibility, and flyToView.
        writer.println("  networkLink.set(link, true, true);");
        writer.println("  ge.getFeatures().appendChild(networkLink);");
        writer.println("}");
        writer.println("function ge_error(errorCode) {");
        writer.println("}");
        writer.println("</script>");

        writer.println("</head>");
        writer.println("<body onload='OnBodyLoad()'>");
        writer.println("<div id='map3d' style='height: 100%; width: 100%'></div>");
        writer.println("</body>");
        writer.println("</html>");
    }
}
