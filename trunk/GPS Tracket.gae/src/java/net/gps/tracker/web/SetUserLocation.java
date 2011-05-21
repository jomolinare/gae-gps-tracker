package net.gps.tracker.web;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.http.*;
import net.gps.tracker.server.PersistencyManager;

import net.gps.tracker.shared.Coordinate;
import net.gps.tracker.shared.User;

public class SetUserLocation extends HttpServlet {

    private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");

        String ID = request.getParameter("ID");
        String DT = request.getParameter("DT");
        String T = request.getParameter("T");
        String TZ = request.getParameter("TZ");
        String LAT = request.getParameter("LAT");
        String LON = request.getParameter("LON");
        String S = request.getParameter("S");
        String C = request.getParameter("C");
        String A = request.getParameter("A");

        Date timestamp = new Date();
        String timezone = TimeZone.getDefault().getID();
        try {
            if (DT != null) {
                timestamp = fmt.parse(DT);
            }
            if (T != null) {
                timestamp = new Date(Long.parseLong(T));
            }
            if (TZ != null) {
                timezone = TimeZone.getTimeZone(TZ).getID();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        PersistencyManager pm = new PersistencyManager();
        User user = pm.getUser(ID);
        if (user == null) throw new RuntimeException("Invalid user ID");

        Coordinate coordinate = new Coordinate(
                user.getId(), timestamp, timezone,
                Double.parseDouble(LAT), Double.parseDouble(LON),
                S == null ? 0 : Float.parseFloat(S),
                C == null ? 0 : Float.parseFloat(C),
                A == null ? 0 : Float.parseFloat(A));
        pm.addCoordinate(coordinate);
        response.getWriter().print("ACK");
    }
}
