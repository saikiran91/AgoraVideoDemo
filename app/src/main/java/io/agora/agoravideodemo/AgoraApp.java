package io.agora.agoravideodemo;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import timber.log.Timber;

/**
 * Created by saikiran on 02-06-2018.
 **/
public class AgoraApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
    }
}