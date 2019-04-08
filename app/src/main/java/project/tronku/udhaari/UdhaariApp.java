package project.tronku.udhaari;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import timber.log.Timber;

public class UdhaariApp extends Application {

    private SharedPreferences pref;
    private static UdhaariApp instance;

    @Override
    public void onCreate() {
        super.onCreate();

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance = this;

        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
    }

    public static UdhaariApp getInstance() {
        return instance;
    }

    public void saveToPref(String key, String value) {
        pref.edit().putString(key, value).apply();
    }

    public String getDataFromPref(String key) {
        return pref.getString(key, "");
    }

    public void clearPrefs() {
        pref.edit().clear().apply();
    }

    public SharedPreferences getPref() {
        return pref;
    }

}
