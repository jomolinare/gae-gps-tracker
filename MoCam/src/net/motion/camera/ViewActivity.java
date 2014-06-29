package net.motion.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewActivity extends Activity {

    private List<File> files;
    private int index;
    ImageView image;
    
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.view);
        image = (ImageView)findViewById(R.id.image);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Object data = extras.get("path");
            if (data != null) {
                path=(String)data;
                loadFiles();
            }
        }
    }
    String path;
    void loadFiles() {
        try {
            File[] list = new File(path).listFiles();
            MainActivity.info("files: "+list.length);
            files = Arrays.asList(list);
            files = new ArrayList<File>(files);
            if (list.length == 0) return;
            index = list.length;
            back(null);
        }
        catch (Exception e) {MainActivity.log(e.toString());}
    }
    
    public void next(View v) {
        if (index+1 < files.size()) index++;
        MainActivity.info("index: "+index);
        view(index);
    }
    public void back(View v) {
        if (index > 0) index--;
        MainActivity.info("index: "+index);
        view(index);
    }
    void view (Integer i) {
        if (i == null)
            image.setImageBitmap(null);
        else try {
            String f = files.get(i).getPath();
            MainActivity.info("file: "+f);
            Bitmap bmp = BitmapFactory.decodeFile(f);
            image.setImageBitmap(bmp);
        }
        catch (Exception e) {MainActivity.log(e.toString());}
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem i = menu.add(getResources().getString(R.string.delete));
        i.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) { delete(); return true;}
        });
        return true;
    }
    public void delete() {
        try {
            File file = files.get(index);
            MainActivity.info("delete: "+index);
            files.remove(file);
            file.delete();
            view(null);
            back(null);
        }
        catch (Exception e) {MainActivity.log(e.toString());}
    }
}
