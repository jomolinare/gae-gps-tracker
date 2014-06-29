package net.motion.camera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MainPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
//        if (key.equals(getString(R.string.start_on_boot_id))) {
//            if (preferences.getBoolean(key, false))
//                startService(new Intent(this, MainService.class));
//            else stopService(new Intent(this, MainService.class));
//        }
    }
}
