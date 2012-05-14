package net.gps.tracker;

import android.app.Activity;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.List;

public class Main extends Activity implements LocationListener {

    protected TextView text, time,
            latitude, longitude, accuracy,
            speed, altitude, course;
    private static final long MIN_DISTANCE = 100; // Meters
    private static final long MIN_TIME = 10000; // Milliseconds

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        text = (TextView)findViewById(R.id.text);
        time = (TextView)findViewById(R.id.date_time);
        latitude = (TextView)findViewById(R.id.latitude);
        longitude = (TextView)findViewById(R.id.longitude);
        speed  = (TextView)findViewById(R.id.speed);
        altitude  = (TextView)findViewById(R.id.altitude);
        course = (TextView)findViewById(R.id.course);
        accuracy = (TextView)findViewById(R.id.accuracy);
        try {
            LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,MIN_DISTANCE,this);
        }
        catch (Exception ex) {
            log(ex.toString());
        }
    }
    @Override
    public void onStart() {super.onStart();log("START");}
    @Override
    public void onStop() {super.onStop();log("STOP");}
    @Override
    public void onPause() {super.onPause();log("PAUSE");}
    @Override
    public void onResume() {super.onResume();log("RESUME");}

    public void onLocationChanged(Location location) {
        try {
            time.setText(new Date(location.getTime()).toString());
            longitude.setText(Double.toString(location.getLongitude()));
            latitude.setText(Double.toString(location.getLatitude()));
            speed.setText(Float.toString(location.getSpeed()));
            altitude.setText(Double.toString(location.getAltitude()));
            course.setText(Float.toString(location.getBearing()));
            accuracy.setText(Float.toString(location.getAccuracy()));
            SendToServer();
        }
        catch (Exception ex) {
            log(ex.toString());
        }
    }

    public void onProviderEnabled(String s) {
        //log("Provider Enabled: "+s);
    }
    public void onProviderDisabled(String s) {
        //log("Provider Disabled: "+s);
    }
    public void onStatusChanged(String s, int i, Bundle b) {
        //log("Status Changed: "+i+" "+s);
    }

    int count = 1;
    List<String> queue = new ArrayList<String>();
    private void SendToServer() throws Exception {
        queue.add(getString(R.string.GPS_Server_URL) + "?"
            + "ID=" + getString(R.string.GPS_Device_ID)
            + "&T=" + System.currentTimeMillis()
            + "&TZ=" + URLEncode(TimeZone.getDefault().getID())
            + "&LAT=" + latitude.getText() + "&LON=" + longitude.getText()
            + "&C=" + course.getText() + "&S=" + speed.getText() + "&A=" + altitude.getText()
        );

        while (!queue.isEmpty()) {
            URL url = new URL(queue.get(0));
            log("["+(count++)+"] "+url.toString());
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            String msg = c.getResponseMessage(); c.disconnect();
            if (msg.equals("OK")) queue.remove(0);
        }
    }

    private DateFormat DF =
            new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
    public void log(String msg) {
        String T = DF.format(new Date());
        text.setText(T+' '+msg+'\n'+text.getText());
    }

    private static String URLEncode(String s) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            switch(ch) {
                case ' ': { str.append("+"); break;}
                case '!': { str.append("%21"); break;}
                case '*': { str.append("%2A"); break;}
                case '\'': { str.append("%27"); break;}
                case '(': { str.append("%28"); break;}
                case ')': { str.append("%29"); break;}
                case ';': { str.append("%3B"); break;}
                case ':': { str.append("%3A"); break;}
                case '@': { str.append("%40"); break;}
                case '&': { str.append("%26"); break;}
                case '=': { str.append("%3D"); break;}
                case '+': { str.append("%2B"); break;}
                case '$': { str.append("%24"); break;}
                case ',': { str.append("%2C"); break;}
                case '/': { str.append("%2F"); break;}
                case '?': { str.append("%3F"); break;}
                case '%': { str.append("%25"); break;}
                case '#': { str.append("%23"); break;}
                case '[': { str.append("%5B"); break;}
                case ']': { str.append("%5D"); break;}
                default: str.append((char)ch);
            }
        }
        return str.toString();
    }
}
