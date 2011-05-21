package net.gps.tracker.server;

import java.util.List;

//import com.google.gwt.user.client.rpc.RemoteService;
//import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import net.gps.tracker.shared.Coordinate;
import net.gps.tracker.shared.User;

//@RemoteServiceRelativePath("service")
public interface GpsLocation {//extends RemoteService

    public void createUser(User user);

    public void updateUser(User user);

    public void deleteUser(User user);

    public List<User> listUsers();

    public List<Coordinate> listCoordinates(Long UserID);

    public void createCoordinate(Coordinate coordinate);

    public void updateCoordinate(Coordinate coordinate);

    public void deleteCoordinate(Coordinate coordinate);
}
