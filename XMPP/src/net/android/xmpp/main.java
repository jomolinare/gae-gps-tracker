package net.android.xmpp;

//<editor-fold defaultstate="collapsed" desc="imports">
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
//</editor-fold>

public class main extends Activity implements PacketListener, FileTransferListener {

    //<editor-fold defaultstate="collapsed" desc="params">
    public static final String SERVICE = "gmail.com";
    public static final String HOST = "talk.google.com";
    public static final int PORT = 5222;
    private XMPPConnection connection;
    FileTransferManager transfer;
    private TextView text;
    private Spinner recipients;
    private EditText username, password;
    private EditText message;
    private View credentials;
    private SharedPreferences preferences;
    
    private myTextToSpeech voice;
    private mySpeechRecognizer speech;
    //</editor-fold>

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        credentials = this.findViewById(R.id.credentials);
        username = (EditText) this.findViewById(R.id.ID);
        password = (EditText) this.findViewById(R.id.PW);
        recipients = (Spinner)findViewById(R.id.roster);
        text = (TextView) this.findViewById(R.id.list);
        message = (EditText) this.findViewById(R.id.msg);
        try {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            username.setText(preferences.getString("username", null));
            password.setText(preferences.getString("password", null));
        }
        catch (Exception ex) {showException(ex);}
        try {
            voice = new myTextToSpeech(this);
            speech = new mySpeechRecognizer(this);            
        }
        catch (Exception ex) {showException(ex);}
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(getResources().getString(R.string.disconnect))
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {Disconnect(); return true;}
        });
        return true;
    }
    public void Connect(View v) {
        try {
            connection = new XMPPConnection(
                new ConnectionConfiguration(HOST, PORT, SERVICE));
            connection.addConnectionListener(new AbstractConnectionListener() {
                @Override public void connectionClosedOnError(Exception e) {Reconnect();}
            });
            //<editor-fold defaultstate="collapsed" desc="configuration">
            //ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);
            //sdm.addFeature("http://jabber.org/protocol/disco#info");
            //sdm.addFeature("jabber:iq:privacy");
            //XMPPConnection.DEBUG_ENABLED = true;
//            ProviderManager pm = ProviderManager.getInstance();
//            pm.addIQProvider("si", "http://jabber.org/protocol/si",new StreamInitiationProvider());
//            pm.addIQProvider("query","http://jabber.org/protocol/bytestreams",new BytestreamsProvider());
//            pm.addIQProvider("open","http://jabber.org/protocol/ibb", new OpenIQProvider());
//            pm.addIQProvider("data","http://jabber.org/protocol/ibb", new DataPacketProvider());
//            pm.addIQProvider("close","http://jabber.org/protocol/ibb", new CloseIQProvider());
//            pm.addExtensionProvider("data","http://jabber.org/protocol/ibb", new DataPacketProvider());
            
//            ProviderManager PM = ProviderManager.getInstance();
//            PM.addIQProvider("query",
//                    "http://jabber.org/protocol/bytestreams",
//                    new BytestreamsProvider());
//            PM.addIQProvider("query",
//                    "http://jabber.org/protocol/disco#items",
//                    new DiscoverItemsProvider());
//            PM.addIQProvider("query",
//                    "http://jabber.org/protocol/disco#info",
//                    new DiscoverInfoProvider());
            //</editor-fold>
            connection.connect();
            //SASLAuthentication.supportSASLMechanism("PLAIN", 0);
            connection.login(
                    username.getText().toString(), 
                    password.getText().toString());
            try {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", username.getText().toString());
                editor.putString("password", password.getText().toString());
                editor.commit();            
            }
            catch (Exception ex) {showException(ex);}
            credentials.setVisibility(View.GONE);
            Presence presence = new Presence(Presence.Type.available);
            connection.sendPacket(presence);
            //PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
            connection.addPacketListener(this, null);
            FileTransferNegotiator.setServiceEnabled(connection, true);
            transfer = new FileTransferManager(connection);
            transfer.addFileTransferListener(this);
            showMessage("Connected");

            Roster roster = connection.getRoster();
            List<RosterEntryInfo> list = new ArrayList<RosterEntryInfo>();
            for (RosterEntry entry : roster.getEntries())
                list.add(new RosterEntryInfo(entry));
            list.add(0,new RosterEntryInfo(null));
            recipients.setAdapter(new ArrayAdapter<RosterEntryInfo>(this, 
                    android.R.layout.simple_spinner_item, list));
        }
        catch (Exception ex) {showException(ex);}
    }
    void Reconnect() {
        showMessage("Connection lost ...");
        handler.postDelayed(new Runnable() {
            public void run() {
                credentials.setVisibility(View.VISIBLE);
                Connect(null);
            }
        },10000);
    }
    public void Disconnect() {
        try {
            connection.disconnect();
            credentials.setVisibility(View.VISIBLE);
        }
        catch (Exception ex) {showException(ex);}
    }
    
    public void Listen(View view) {
        handler.post(new Runnable() {
            public void run() {
                showMessage("listening ...");
                speech.listen(new mySpeechRecognizer.CallBack() {
                    public void process(List<String> phrases) {
                        if (phrases.isEmpty()) return;
                        message.setText(phrases.get(0));
                    }
                    public void error(String message) {
                        showMessage(message);
                    }
                });
            }
        });
    }
    public void SendMessage(View view) {
        try {
            String to = getRecipientID(); 
            String body = message.getText().toString();
            send(to,body);
            String from = StringUtils
                .parseBareAddress(connection.getUser());
            text.append(from + '\n' + body + '\n');
            message.setText(null);
        } 
        catch (Exception ex) {showException(ex);}
    }
    void send(String to, String body) {
        if (connection != null) try {
            Message msg = new Message(to);
            msg.setType(Message.Type.chat);
            msg.setBody(body);
            connection.sendPacket(msg);
            if (body.equals("bytes")) {
                showMessage("Transfering bytes to " + getRecipientName());
                sendByteArray(getRecipientID(), "bytes", new byte[1024]);
            }
        } 
        catch (Exception ex) {showException(ex);}
    }

    @Override
    public void processPacket(Packet packet) {
        final Message msg = (Message) packet;
        if (msg.getBody() == null) return;
        handler.post(new Runnable() {
            public void run() {
              try { 
                selectRecipient(msg.getFrom());
                setRecipientJID(msg.getFrom());
                String body = msg.getBody();
                if (body.charAt(0)=='#') {
                    body = body.toUpperCase();
                    if (body.contains("SEND LOCATION"))
                        SendLocation(getRecipientID(), false);
                    if (body.contains("SEND GPS LOCATION"))
                        SendLocation(getRecipientID(), true);
                    if (body.contains("IP")) 
                        send(getRecipientID(),getIPs());
                    return;
                }
                text.append(getRecipientName()+ '\n');
                //text.append(getRecipientJID()+ '\n');
                text.append(body + '\n');
                voice.speakAndWait(body);
                if (body.contains("?")) {
                    showMessage("listening ...");
                    speech.listen(new mySpeechRecognizer.CallBack() {
                        public void process(List<String> phrases) {
                            if (phrases.isEmpty()) return;
                            message.setText(phrases.get(0));
                            SendMessage(null);
                        }
                        public void error(String message) {
                            showMessage(message);
                        }
                    });
                }
              }
              catch (Exception ex) {showException(ex);}
            }
        });
    }

    //<editor-fold defaultstate="collapsed" desc="file transfer">
    public void sendByteArray(String UserID, String name, byte[] array) {
        try {
            OutgoingFileTransfer oft = transfer.createOutgoingFileTransfer(UserID);
            ByteArrayInputStream is = new ByteArrayInputStream(array);
            oft.sendStream(is, name, array.length, null);
            while (!oft.isDone()) Thread.sleep(1000);
            showMessage(""+oft.getProgress()+"%");
            //if (oft.getStatus().equals(FileTransfer.Status.error))
            if (oft.getError() != null)
                showMessage(oft.getError().getMessage());
        } 
        catch (Exception ex) {showException(ex);}
    }
    
    public void fileTransferRequest(FileTransferRequest ftr) {
        IncomingFileTransfer ift = ftr.accept();
        String name = ift.getFileName();
        try {
            showMessage("Receiving " + name + " ...");
            InputStream is = ift.recieveFile();
            byte[] data = new byte[(int) ift.getFileSize()];
            int n = is.read(data); is.close();
            showMessage("Received " + n + " bytes");
        } 
        catch (Exception ex) {showException(ex);}
    }
    //</editor-fold>
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connection != null) {
            Presence presence = new Presence(Presence.Type.unavailable);
            connection.sendPacket(presence);
            connection.disconnect();
        }
    }
    
    protected String getRecipientID() {
        return ((RosterEntryInfo)recipients.getSelectedItem()).getUser();
    }
    protected String getRecipientName() {
        return ((RosterEntryInfo)recipients.getSelectedItem()).getName();
    }
    protected String getRecipientJID() {
        return ((RosterEntryInfo)recipients.getSelectedItem()).JID;
    }
    protected void setRecipientJID(String JID) {
        ((RosterEntryInfo)recipients.getSelectedItem()).JID = JID;
    }
    protected void selectRecipient(String info) {
        ArrayAdapter a = (ArrayAdapter)recipients.getAdapter();
        for (int n = 0; n < a.getCount(); n++) {
            RosterEntryInfo i = (RosterEntryInfo)a.getItem(n);
            if (i.contains(info)) {
                int x = a.getPosition(i);
                recipients.setSelection(x);
                break;
            }
        }
    }
    
    String app_name = "XMPP";
    Handler handler = new Handler();

    public void showMessage(final String message) {
        android.util.Log.d(app_name, message);
        final Context context = this;
        handler.post(new Runnable() {
            public void run() {
                text.append(message + '\n');
                android.widget.Toast.makeText(context, message,
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showException(Exception ex) {
        android.util.Log.wtf(app_name, ex);
        showMessage(ex.toString());
    }

    void SendLocation(final String to, boolean GPS) {
        try {
            final LocationManager mgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            if (GPS) mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,
                new LocationListener() {
                    public void onLocationChanged(Location l) {
                        try {
                            send(to,"lat/lon: "
                                    +l.getLatitude()+','
                                    +l.getLongitude());
                            mgr.removeUpdates(this);                
                        } 
                        catch (Exception ex) {showException(ex);}
                    }
                    public void onStatusChanged(String p, int s, Bundle e) {}
                    public void onProviderEnabled(String p) {}
                    public void onProviderDisabled(String p) {}
                });
            else {
                Location l = mgr.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if (l != null) {
                    CharSequence msg = "lat/lon: "
                            +l.getLatitude()+','
                            +l.getLongitude();
                    send(to,msg.toString());                
                }
                else SendLocation(to,true);
            }
        } 
        catch (Exception ex) {showException(ex);}
    }
    public String getIPs() {
        StringBuilder info = new StringBuilder();
        try {
            Enumeration<NetworkInterface> NETs 
                    = NetworkInterface.getNetworkInterfaces(); 
            while (NETs.hasMoreElements()) {
                NetworkInterface NET = NETs.nextElement();
                Enumeration<InetAddress> IPs = NET.getInetAddresses(); 
                while (IPs.hasMoreElements()) {
                    InetAddress IP = IPs.nextElement();
                    if (!IP.isLoopbackAddress()) {
                        info.append(IP.getHostAddress());
                        info.append('\n');
                    }
                }
            }
        }
        catch (Exception ex) {showException(ex);}
        return info.toString().trim();
    }
}

class RosterEntryInfo {
    public String JID;
    private final RosterEntry entry;
    public RosterEntryInfo(RosterEntry entry) {
        this.entry = entry;
    }
    public String getUser() {
        return entry==null?"":entry.getUser();
    }
    public String getName() {
        return entry==null?"":entry.getName();
    }
    public boolean contains(String info) {
        if (entry == null) return false;
        if (entry.getName().contains(info)) return true;
        if (info.contains(entry.getName())) return true;
        if (entry.getUser().contains(info)) return true;
        if (info.contains(entry.getUser())) return true;
        return false;
    }
    @Override
    public String toString() {
        return entry==null?"":entry.getName();
    }
}
