package net.gps.tracker;

import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.location.Location;
import javax.microedition.location.Coordinates;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;
import javax.microedition.midlet.MIDlet;

public class Midlet extends MIDlet implements LocationListener {

    private Display display;
    private Form InfoPanel;
    private StringItem
            Timestamp, Speed, Course,
            Latitude, Longitude, Altitude,
            Message;

    public Midlet() {
        display = Display.getDisplay(this);
        InfoPanel = new Form("GPS Tracker");
        try {
            InfoPanel.append(Timestamp = new StringItem("DateTime:", null));
            InfoPanel.append(Speed = new StringItem("Speed:", null));
            InfoPanel.append(Course = new StringItem("Course:", null));
            InfoPanel.append(Latitude = new StringItem("Latitude:", null));
            InfoPanel.append(Longitude = new StringItem("Longitude:", null));
            InfoPanel.append(Altitude = new StringItem("Altitude:", null));
            InfoPanel.append(Message = new StringItem(null, null));

            Timestamp.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER);
            Speed.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER);
            Course.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER);
            Latitude.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER);
            Longitude.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER);
            Altitude.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER);
            Message.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_AFTER);
            LocationProvider provider = LocationProvider.getInstance(null);
            provider.setLocationListener(this, -1, -1, -1);
        }
        catch (Exception ex) {
            showMessage("Exception:", ex.toString());
        }
        display.setCurrent(InfoPanel);
    }

    public void providerStateChanged(LocationProvider provider, int state) {
        showMessage(new Date().toString(),"ProviderStateChanged: "+state);
    }

    private Coordinates CoordinatesOnFile;

    public void locationUpdated(LocationProvider provider, Location location) {
        Coordinates coordinates = location.getQualifiedCoordinates();
        if (location == null || coordinates == null) return;
        Timestamp.setText(new Date().toString());
        Speed.setText(Float.toString(location.getSpeed()));
        Course.setText(Float.toString(location.getCourse()));
        Latitude.setText(Double.toString(coordinates.getLatitude()));
        Longitude.setText(Double.toString(coordinates.getLongitude()));
        Altitude.setText(Float.toString(coordinates.getAltitude()));

        try {
            if (Distance(CoordinatesOnFile, coordinates) >= 0.0003) {
                CoordinatesOnFile = coordinates;
                SendToServer();
            }
        }
        catch (Exception ex) {
            showMessage("Exception:", ex.toString());
            try { Thread.sleep(60000); } catch (Exception e) {}
            showMessage(null, null);
        }
    }

    Vector queue = new Vector();
    private void SendToServer() throws Exception {
        queue.addElement(getAppProperty("GPS-Server-URL")+"?"+
            "ID="+getAppProperty("GPS-Device-ID")+
            "&T="+System.currentTimeMillis()+
            "&TZ="+URLEncode(TimeZone.getDefault().getID())+
            "&LAT="+Latitude.getText()+"&LON="+Longitude.getText()+
            "&C="+Course.getText()+"&S="+Speed.getText()+"&A="+Altitude.getText()
        );
        while (!queue.isEmpty()) {
            String url = (String)queue.elementAt(0);
            HttpConnection c = (HttpConnection)Connector.open(url);
            String msg = c.getResponseMessage(); c.close();
            if (msg.equals("OK")) queue.removeElementAt(0);
            else break;
        }
    }

    public void startApp(){} public void pauseApp(){}
    public void destroyApp(boolean unconditional){}

    private void showMessage(String lbl, String msg) {
        Message.setLabel(lbl);
        Message.setText(msg);
    }

    private static double Distance(Coordinates C0, Coordinates C1) {
        if (C0 == null || C1 == null) return Double.MAX_VALUE;
        double dLatitude = Math.abs(C1.getLatitude() - C0.getLatitude());
        double dLongitude = Math.abs(C1.getLongitude() - C0.getLongitude());
        return Math.sqrt(dLatitude*dLatitude + dLongitude*dLongitude);
        //return dLatitude > dLongitude? dLatitude : dLongitude;
    }

    private static String URLEncode(String s) {
        StringBuffer sbuf = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            switch(ch) {
                case ' ': { sbuf.append("+"); break;}
                case '!': { sbuf.append("%21"); break;}
                case '*': { sbuf.append("%2A"); break;}
                case '\'': { sbuf.append("%27"); break;}
                case '(': { sbuf.append("%28"); break;}
                case ')': { sbuf.append("%29"); break;}
                case ';': { sbuf.append("%3B"); break;}
                case ':': { sbuf.append("%3A"); break;}
                case '@': { sbuf.append("%40"); break;}
                case '&': { sbuf.append("%26"); break;}
                case '=': { sbuf.append("%3D"); break;}
                case '+': { sbuf.append("%2B"); break;}
                case '$': { sbuf.append("%24"); break;}
                case ',': { sbuf.append("%2C"); break;}
                case '/': { sbuf.append("%2F"); break;}
                case '?': { sbuf.append("%3F"); break;}
                case '%': { sbuf.append("%25"); break;}
                case '#': { sbuf.append("%23"); break;}
                case '[': { sbuf.append("%5B"); break;}
                case ']': { sbuf.append("%5D"); break;}
                default: sbuf.append((char)ch);
            }
        }
        return sbuf.toString();
    }
}
