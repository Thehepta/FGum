package com.test.fgum;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KeepAliveService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        // 执行一些初始化操作
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 返回 START_STICKY 确保服务被系统终止后重启
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}