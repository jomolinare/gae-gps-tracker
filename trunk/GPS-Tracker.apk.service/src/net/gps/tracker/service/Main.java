package net.gps.tracker.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class Main extends Service implements LocationListener {

    private static final long MIN_DISTANCE = 100; // Meters
    private static final long MIN_TIME = 10000; // Milliseconds
    private static final long STATUS_CHECK_INTERVAL = 600000; // Milliseconds
    private Handler  handler = new Handler();
    
    private void startGPS() {
        try { log("Start GPS");
            setMobileDataEnabled(this, true);
            setGlobalPositionSystemEnabled(this, true);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        }
        catch (Exception ex) { log(ex); }
    }
    private void stopGPS() {
        try { log("Stop GPS");
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.removeUpdates(this);
        }
        catch (Exception ex) { log(ex); }
    }
    private boolean isActive() {
        try {
            URL url = new URL(getString(R.string.GPS_Status_URL) 
                    + "?" + getString(R.string.GPS_Device_ID));
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            String msg = c.getResponseMessage(); 
            if (!msg.equals("OK")) return false;
            InputStream is = c.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader r = new BufferedReader(isr);
            String txt = r.readLine();
            c.disconnect();
            log(url+" -> "+txt);
            if (txt.equals("ON")) return true;
        }
        catch (Exception ex) { log(ex); }
        return false;
    }

    @Override
    public void onCreate() {
        log("onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int ID) {
        log("onStartCommand: "+intent);
        int x = super.onStartCommand(intent, flags, ID);
        handler.postDelayed(new Runnable() {
            public void run() { log("STATUS_CHECK");
               if (isActive()) startGPS(); else stopGPS(); 
               handler.postDelayed(this,STATUS_CHECK_INTERVAL);
            }
        }, STATUS_CHECK_INTERVAL);
        return x;
    }

    @Override
    public void onDestroy() {
        log("onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        log("onBind: "+intent);
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
                String msg = c.getResponseMessage(); c.disconnect();
                if (msg.equals("OK")) queue.remove(0); else break;
            }
        } catch (Exception ex) { log(ex); }
    }

    public void onProviderEnabled(String s) {
        log("Provider Enabled: "+s);
    }

    public void onProviderDisabled(String s) {
        log("Provider Disabled: "+s);
    }

    public void onStatusChanged(String s, int i, Bundle b) {
        log("Status Changed: "+i+" "+s);
    }
    private final static DateFormat DF =
            new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    public void log(String msg) {
        String T = DF.format(new java.util.Date());
        android.util.Log.d(getPackageName(),T+' '+msg);
    }
    public void log(Exception ex) {
        String T = DF.format(new java.util.Date());
        android.util.Log.e(getPackageName(),T);
        android.util.Log.wtf(getPackageName(),ex);
    }
    protected void setGlobalPositionSystemEnabled(Context context, boolean enabled) {
        try {
            String provider = Settings.Secure.getString(
                    context.getContentResolver(), 
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if(enabled != provider.contains("gps")){
                final Intent intent = new Intent();
                intent.setClassName("com.android.settings", 
                        "com.android.settings.widget.SettingsAppWidgetProvider");
                intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
                intent.setData(Uri.parse("3"));
                context.sendBroadcast(intent);
            }
        }
        catch (Exception e) {log(e);}
    }
    protected void setMobileDataEnabled(Context context, boolean enabled) {
        try {
            final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        }
        catch (Exception e) {log(e);}
    }

    //<editor-fold defaultstate="collapsed" desc="URL Encode">
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
    //</editor-fold>
 
}
