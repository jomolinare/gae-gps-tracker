package net.motion.camera;

//<editor-fold defaultstate="collapsed" desc="imports">
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
//</editor-fold>

public class MainActivity extends Activity implements SurfaceHolder.Callback,
        Camera.PreviewCallback, Camera.PictureCallback, Camera.ErrorCallback {

    TextView text;
    Camera camera;
    int Width, Height;
    SurfaceView surface;
    boolean monitoring;
    boolean notify;

    int N; // Camera number
    String dir = "MoCam"+File.separator;
    String account, password, subject, recipients;
    int threshold, tolerance, file_delay, email_delay;
    
    Format DT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    String path = Environment.getExternalStorageDirectory()
            .getAbsolutePath()+File.separator;

    @Override
    public void onCreate(Bundle state) { log("onCreate");
        try {
            super.onCreate(state);
            setContentView(R.layout.main);
            text = (TextView) findViewById(R.id.text);
            surface = (SurfaceView) findViewById(R.id.surface);
            int type = SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS;
            surface.getHolder().setType(type);//REQUIRED:API10
            surface.getHolder().addCallback(this);
        } catch (Exception e) {
            showException(e);
        }
    }
    @Override
    public void onStart() { log("onStart");
        try {
            super.onStart();
            N = getValueBoolean(R.string.front_camera)?
                    CameraInfo.CAMERA_FACING_FRONT:
                    CameraInfo.CAMERA_FACING_BACK;
            account = "MoCam@innody.com";//getValueString(R.string.email_account);
            password = "Anacond@";//getValueString(R.string.email_password);
            subject= getValueString(R.string.email_subject);
            recipients = getValueString(R.string.email_recipient);
            threshold = getValueInteger(R.string.motion_threshold);
            tolerance = getValueInteger(R.string.pixel_tolerance);
            file_delay = getValueInteger(R.string.file_delay)*1000;
            email_delay = getValueInteger(R.string.email_delay)*1000;
            startCamera();
        } catch (Exception e) {
            showException(e);
        }
    }
    void startCamera() {
        try {camera = Camera.open(N);}
        catch (Exception ex) {
            writeException(ex);
            camera = Camera.open(N=0);
        }
        camera.setErrorCallback(this);
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(N, info);
        info("Camera Orientation: "+info.orientation);
        int rotation = getWindowManager().getDefaultDisplay().getRotation()*90;
        info("Screen Rotation: "+rotation);
        int orientation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
            orientation = (2*360 - rotation - info.orientation) % 360;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
            orientation = (2*360 - rotation + info.orientation) % 360;
        info("Display Orientation: "+orientation);
        camera.setDisplayOrientation(orientation);
        Parameters params = camera.getParameters();
        Size s = params.getPreviewSize();
        Width = orientation%180==0?s.width:s.height;
        Height = orientation%180==0?s.height:s.width;
        if (params.getFlashMode() != null)
            params.setFlashMode(Parameters.FLASH_MODE_OFF);
//info("Focus = "+params.getFocusMode());        
//for (String i : params.getSupportedFocusModes()) info("Focus: "+i);
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT)
            if (orientation == 90) orientation = 270;
            else if (orientation == 270) orientation = 90;
        info("Image Rotation: "+orientation);
        params.setRotation(orientation);
        camera.setParameters(params);
    }
    @Override
    public void onStop() { log("onStop");
        super.onStop();
        stopCamera();
    }
    void stopCamera() {
        camera.setPreviewCallback(null);
        camera.release();
    }
    @Override
    public void onDestroy() { log("onDestroy");
        super.onDestroy();
    }
    
    //<editor-fold defaultstate="collapsed" desc="menu">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.preferences:
                startActivity(new Intent(this,MainPreferences.class));
                return true;
            case R.id.view:
                Intent intent = new Intent(this, ViewActivity.class);
                intent.putExtra("path",path+dir);
                startActivity(intent);
                return true;
            case R.id.motion:
                item.setChecked(!item.isChecked());
                item.setTitle(item.isChecked()?
                        getText(R.string.motion_on):
                        getText(R.string.motion_off));
                if (item.isChecked()) {
                    showMessage("Monitoring in 10 seconds ...");
                    handler.postDelayed(new Runnable() {
                        public void run() {monitoring = true;}
                    }, 10000);
                }
                else monitoring = false;
                return true;
            case R.id.email:
                item.setChecked(!item.isChecked());
                item.setTitle(item.isChecked()?
                        getText(R.string.email_on):
                        getText(R.string.email_off));
                notify = item.isChecked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>
    
    public void surfaceCreated(SurfaceHolder s) { log("surfaceCreated"); }
    public void surfaceDestroyed(SurfaceHolder s) { log("surfaceDestroyed"); }
    public void surfaceChanged(SurfaceHolder s, int f, int w, int h) { log("surfaceChanged");
        try {
            camera.stopPreview();
            info("Surface size: "+w+"x"+h);
            LayoutParams p = surface.getLayoutParams();
            if (w<h) {p.width=w; p.height=w*Height/Width;}
            if (h<w) {p.height=h; p.width=h*Width/Height;}
            info("Layout size: "+p.width+"x"+p.height);
            surface.setLayoutParams(p);
            camera.setPreviewDisplay(s);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (Exception ex) {
            showException(ex);
        }
    }
    byte[] pixels; boolean busy; long millis;
    public void onPreviewFrame(byte[] data, Camera camera) { log("onPreviewFrame");
        long T = System.currentTimeMillis();
        if (!busy && (T-millis) > 500) try {
            if (pixels == null) pixels = data;
            else {
                int count = 0;
                for (int i = 0; i < data.length && i < pixels.length; i++)
                    if (Math.abs(data[i]-pixels[i])>tolerance) count++;
                final int percent = count*100/data.length;
                pixels = data;
                writeMessage(String.format("%02d%%",percent));
                if (monitoring && percent > threshold) {
                    busy = true; info("Camera.takePicture");
                    camera.takePicture(null, null, null, this);
                }
            }
        } catch (Exception ex) {
            writeException(ex);
        }
        finally {millis = T;}
    }

    long file_delay_millis, email_delay_millis;
    public void onPictureTaken(byte[] data, Camera camera) { log("onPictureTaken");
        long T = System.currentTimeMillis();
        if (T - file_delay_millis > file_delay) try { 
            File file = new File(path+dir+DT.format(new Date())+".jpg");
            showMessage(file.getName()); file.getParentFile().mkdirs();
            FileOutputStream f = new FileOutputStream(file.getAbsolutePath());
            f.write(data); f.close(); file_delay_millis = T;
            if (notify && (T - email_delay_millis > email_delay)) {
                showMessage(recipients); email_delay_millis = T;
                new Mail(account, password).send(recipients, subject, 
                    file.getName(),file.getAbsolutePath());                    
            }
        } catch (Exception ex) {
            writeException(ex);
        } finally {
            busy = false;
            camera.startPreview();
        }
    }
    //<editor-fold defaultstate="collapsed" desc="messages">
    final static String TAG = "MoCam";
    final Handler handler = new Handler();
    void writeMessage(final String message) {
        android.util.Log.v(TAG, message);
        handler.post(new Runnable() {
            public void run() {
                text.setText(message);
            }
        });
    }
    void showMessage(final String message) {
        android.util.Log.v(TAG, message);
        final Context context = this;
        handler.post(new Runnable() {
            public void run() {
                android.widget.Toast.makeText(context, message,
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
    void writeException(Exception ex) {
        android.util.Log.wtf(TAG, ex);
        writeMessage(ex.getMessage());
    }
    void showException(Exception ex) {
        android.util.Log.wtf(TAG, ex);
        showMessage(ex.toString());
    }
    static void log(String message) {
        android.util.Log.d(TAG,message);
    }
    static void info(String message) {
        android.util.Log.i(TAG,message);
    }
    //</editor-fold>
    
    boolean getValueBoolean(int resource) {
        return PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(getString(resource),false);
    }
    String getValueString(int resource) {
        return PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(getString(resource),null);
    }
    int getValueInteger(int resource) {
        return Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(getString(resource),"0"));
    }
    public void onError(int i, Camera camera) {
        showMessage("CAMERA ERROR !!! ");
    }
    final static CharSequence ImageFormatName(int format) {
        switch (format) {
            case ImageFormat.JPEG: return "JPEG";
            case ImageFormat.RGB_565: return "RGB_565";
            case ImageFormat.NV21: return "NV21";
            case ImageFormat.NV16: return "NV16";
            case ImageFormat.YUY2: return "YUY2";
            case ImageFormat.YV12: return "YV12";
            case ImageFormat.UNKNOWN: return "UNKNOWN";
            default: return "#"+format;
        }
    }
}
/*
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import java.io.ByteArrayOutputStream;

void saveImage(Camera camera, byte[] data) {
        try {
            Camera.Parameters params = camera.getParameters();
            Camera.Size s = params.getPreviewSize();
            int format = params.getPreviewFormat();
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            switch (format) {
                case ImageFormat.JPEG:
                case ImageFormat.RGB_565:
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    break;
                case ImageFormat.NV21:
                case ImageFormat.NV16:
                case ImageFormat.YUY2:
                    YuvImage img = new YuvImage(data, format, s.width, s.height, null);
                    img.compressToJpeg(new Rect(0, 0, s.width, s.height), 50, out);
                    break;
                default:
                    throw new RuntimeException("Invalid ImageFormat #" + format);
            }
            handler.post(new Runnable() {public void run() { 
                try {
                    File file = new File(path+dir+DT.format(new Date())+".jpg");
                    showMessage(file.getName()); file.getParentFile().mkdirs();
                    FileOutputStream f = new FileOutputStream(file.getAbsolutePath());
                    f.write(out.toByteArray()); f.close();
                    if (notify) {
                        showMessage(recipients);
                        mail.send(recipients, subject, 
                            file.getName(),file.getAbsolutePath());                    
                    }        
                } catch (Exception ex) {
                    showException(ex);
                }
            }});
        } catch (Exception ex) {
            showException(ex);
        }
    }
*/
        /*
        Display display = getWindowManager().getDefaultDisplay();
        info("Rotation: "+(90*display.getRotation()));
        if(display.getRotation() == Surface.ROTATION_0) {
            camera.setDisplayOrientation(CameraRotation);
            params.setRotation(CameraRotation);
        }
        if(display.getRotation() == Surface.ROTATION_90) {
            camera.setDisplayOrientation(CameraRotation-90);
            params.setRotation(CameraRotation-90);
        }
        if(display.getRotation() == Surface.ROTATION_180) {// ?
            camera.setDisplayOrientation(CameraRotation+90);
            params.setRotation(CameraRotation+90);
        }
        if(display.getRotation() == Surface.ROTATION_270) {
            camera.setDisplayOrientation(CameraRotation+90);
            params.setRotation(CameraRotation+90);
        }*/
        /*
        int fmt = params.getPreviewFormat();
        info("FMT = "+ImageFormatName(fmt));
        StringBuilder FMT = new StringBuilder("FMT: ");
        for (int i : params.getSupportedPreviewFormats())
            {FMT.append(ImageFormatName(i)).append(' ');}
        info(FMT.toString());
        
        Size size = params.getPreviewSize();
        StringBuilder SIZE = new StringBuilder("SIZE: ");
        for (Size i : params.getSupportedPreviewSizes())
            {SIZE.append(i.width).append('x').append(i.height).append(' ');
            if (i.width*i.height > size.width*size.height) size = i;}
        info(SIZE.toString());
        params.setPreviewSize(size.width,size.height);
        info("SIZE = "+size.width+'x'+size.height);
        
        int fps = params.getPreviewFrameRate();
        StringBuilder FPS = new StringBuilder("FPS: ");
        for (int i : params.getSupportedPreviewFrameRates())
            {FPS.append(i).append(' '); if (i < fps) fps = i;}
        info(FPS.toString());
        params.setPreviewFrameRate(fps);
        info("FPS = "+fps);
        */
