package net.gps.tracker.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class Main extends Service implements LocationListener {

    private static final long MIN_DISTANCE = 100; // Meters
    private static final long MIN_TIME = 60000; // Milliseconds

    @Override
    public void onCreate() {
        super.onCreate();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    int count = 1;
    List<String> queue = new ArrayList<String>();
    public void onLocationChanged(Location location) {
        try {
            queue.add(getString(R.string.GPS_Server_URL) + "?"
                    + "ID=" + getString(R.string.GPS_Device_ID)
                    + "&T=" + location.getTime()
                    + "&TZ=" + URLEncode(TimeZone.getDefault().getID())
                    + "&LAT=" + Double.toString(location.getLatitude())
                    + "&LON=" + Double.toString(location.getLongitude())
                    + "&C=" + Float.toString(location.getBearing())
                    + "&S=" + Float.toString(location.getSpeed())
                    + "&A=" + Double.toString(location.getAltitude()));

            while (!queue.isEmpty()) {
                URL url = new URL(queue.get(0));
                log("[" + (count++) + "] " + url.toString());
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                String msg = c.getResponseMessage();
                if (!msg.equals("OK")) break;
                else queue.remove(0);
                c.disconnect();
            }
        } catch (Exception ex) {
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
    private DateFormat DF =
            new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");

    public void log(String msg) {/*
        String T = DF.format(new Date());
        text.setText(T+' '+msg+'\n'+text.getText());
    */}

    private static String URLEncode(String s) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            switch (ch) {
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
