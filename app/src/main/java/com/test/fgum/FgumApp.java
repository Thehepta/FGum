package com.test.fgum;

import android.app.Application;

public class FgumApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LoadEntry.Entry(getApplicationContext(),getApplicationInfo().sourceDir,"");
    }
}
