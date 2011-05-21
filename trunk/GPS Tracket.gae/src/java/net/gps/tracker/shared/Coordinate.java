package net.gps.tracker.shared;

import java.io.Serializable;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.Date;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Coordinate implements Serializable {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long ID;
    @Persistent
    private Long userId;
    @Persistent
    private Date timestamp;
    @Persistent
    private String timezone;
    @Persistent
    private float speed;
    @Persistent
    private float course;
    @Persistent
    private double latitude;
    @Persistent
    private double longitude;
    @Persistent
    private float altitude;

    public Coordinate() {
    }

    public Coordinate(Long userId, Date timestamp, String timezone,
            double latitude, double longitude,
            float speed, float course, float altitude) {
        super();
        this.userId = userId;
        this.timestamp = timestamp;
        this.timezone = timezone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.course = course;
        this.altitude = altitude;
    }

    public void setId(Long ID) {
        this.ID = ID;
    }

    public void setUserId(Long ID) {
        userId = ID;
    }

    public void setTimestamp(Date value) {
        timestamp = value;
    }

    public void setTimezone(String value) {
        timezone = value;
    }

    public void setSpeed(float value) {
        speed = value;
    }

    public void setCourse(float value) {
        course = value;
    }

    public void setLatitude(double value) {
        latitude = value;
    }

    public void setLongitude(double value) {
        longitude = value;
    }

    public void setAltitude(float value) {
        altitude = value;
    }

    public Long getId() {
        return this.ID;
    }

    public Long getUserId() {
        return userId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getTimezone() {
        return timezone;
    }

    public float getSpeed() {
        return speed;
    }

    public float getCourse() {
        return course;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getAltitude() {
        return altitude;
    }
}
