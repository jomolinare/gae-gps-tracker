package net.gps.tracker.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.Query;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import net.gps.tracker.shared.Coordinate;
import net.gps.tracker.shared.User;

public class PersistencyManager {

    private static final PersistenceManagerFactory pmf =
            JDOHelper.getPersistenceManagerFactory("transactions-optional");

    public User getUser(String name) {
        PersistenceManager pm = pmf.getPersistenceManager();
        Query query = pm.newQuery(User.class);
        query.setFilter("name == user_name");
        query.declareParameters("String user_name");
        query.setUnique(true);
        return (User) query.execute(name);
    }

    @SuppressWarnings("unchecked")
    public List<User> listUsers() {
        PersistenceManager pm = pmf.getPersistenceManager();
        String query = "SELECT FROM " + User.class.getName();
        return (List<User>) pm.newQuery(query).execute();
    }

    public void createUser(User user) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            pm.makePersistent(user);
        } finally {
            pm.close();
        }
    }

    public void updateUser(User user) {
        PersistenceManager pm = pmf.getPersistenceManager();

        try {
            pm.currentTransaction().begin();
            // We don't have a reference to the selected item
            User c = pm.getObjectById(User.class, user.getId());
            c.setName(user.getName());
            c.setPhone(user.getPhone());
            c.setEmail(user.getEmail());
            pm.makePersistent(c);
            pm.currentTransaction().commit();
        } catch (Exception ex) {
            pm.currentTransaction().rollback();
            throw new RuntimeException(ex);
        } finally {
            pm.close();
        }
    }

    public void deleteUser(User user) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            pm.currentTransaction().begin();
            // We don't have a reference to the selected item
            user = pm.getObjectById(User.class, user.getId());
            pm.deletePersistent(user);
            pm.currentTransaction().commit();
        } catch (Exception ex) {
            pm.currentTransaction().rollback();
            throw new RuntimeException(ex);
        } finally {
            pm.close();
        }
    }

    public void addCoordinate(Coordinate coordinate) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            pm.makePersistent(coordinate);
        } finally {
            pm.close();
        }
    }

    public Map<User, Coordinate> listCoordinates() {
        List<User> users = listUsers();
        PersistenceManager pm = pmf.getPersistenceManager();
        Query query = pm.newQuery(Coordinate.class);
        query.setFilter("userId == user_id");
        query.declareParameters("java.lang.Long user_id");
        query.setOrdering("timestamp descending");
        query.setRange(0, 1);
        query.setUnique(true);
        Map<User, Coordinate> coordinates = new HashMap<User, Coordinate>();
        for (User user : users) {
            Object coordinate = query.execute(user.getId());
            coordinates.put(user, (Coordinate) coordinate);
        }
        return coordinates;
    }

    @SuppressWarnings("unchecked")
    public List<Coordinate> listCoordinates(Long UserID) {
        PersistenceManager pm = pmf.getPersistenceManager();
        Query query = pm.newQuery(Coordinate.class);
        query.setFilter("userId == user_id");
        query.declareParameters("Long user_id");
        query.setOrdering("timestamp descending");
        return (List<Coordinate>) query.execute(UserID);
    }

    @SuppressWarnings("unchecked")
    public List<Coordinate> listCoordinates(java.lang.Long UserID, java.util.Date StartDate) {
        PersistenceManager pm = pmf.getPersistenceManager();
        Query query = pm.newQuery(Coordinate.class);
        query.setFilter("userId == user_id && timestamp > start_date");
        query.declareParameters("java.lang.Long user_id, java.util.Date start_date");
        query.setOrdering("timestamp descending");
        return (List<Coordinate>) query.execute(UserID, StartDate);
    }

    public void removeCoordinate(Coordinate coordinate) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            pm.currentTransaction().begin();
            // We don't have a reference to the selected item
            coordinate = pm.getObjectById(Coordinate.class, coordinate.getId());
            pm.deletePersistent(coordinate);
            pm.currentTransaction().commit();
        } catch (Exception ex) {
            pm.currentTransaction().rollback();
            throw new RuntimeException(ex);
        } finally {
            pm.close();
        }
    }

    public void updateCoordinate(Coordinate coordinate) {
        PersistenceManager pm = pmf.getPersistenceManager();

        try {
            pm.currentTransaction().begin();
            // We don't have a reference to the selected item
            Coordinate c = pm.getObjectById(Coordinate.class, coordinate.getId());
            c.setUserId(coordinate.getUserId());
            c.setTimestamp(coordinate.getTimestamp());
            c.setSpeed(coordinate.getSpeed());
            c.setCourse(coordinate.getCourse());
            c.setLatitude(coordinate.getLatitude());
            c.setLongitude(coordinate.getLongitude());
            c.setAltitude(coordinate.getAltitude());
            pm.makePersistent(c);
            pm.currentTransaction().commit();
        } catch (Exception ex) {
            pm.currentTransaction().rollback();
            throw new RuntimeException(ex);
        } finally {
            pm.close();
        }
    }
}
