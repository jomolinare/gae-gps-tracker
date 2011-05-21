package net.gps.tracker.server;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
//import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import net.gps.tracker.shared.Coordinate;
import net.gps.tracker.shared.User;

public class GpsLocationService implements GpsLocation {//extends RemoteServiceServlet

    private PersistencyManager pm = new PersistencyManager();

    public void createUser(User user) {
        pm.createUser(user);
    }

    public void updateUser(User user) {
        pm.updateUser(user);
    }

    public void deleteUser(User user) {
        pm.deleteUser(user);
    }

    public List<User> listUsers() {
        List<User> listUsers = pm.listUsers();
        return new ArrayList<User>(listUsers);
    }

    public List<Coordinate> listCoordinates(Long UserID) {
        List<Coordinate> listCoordinates = pm.listCoordinates(UserID);
        return new ArrayList<Coordinate>(listCoordinates);
    }

    public List<Coordinate> listCoordinates(Long UserID, Date StartDate) {
        List<Coordinate> listCoordinates = pm.listCoordinates(UserID, StartDate);
        return new ArrayList<Coordinate>(listCoordinates);
    }

    public void createCoordinate(Coordinate coordinate) {
        pm.addCoordinate(coordinate);
    }

    public void updateCoordinate(Coordinate coordinate) {
        pm.updateCoordinate(coordinate);
    }

    public void deleteCoordinate(Coordinate coordinate) {
        pm.removeCoordinate(coordinate);
    }
}
