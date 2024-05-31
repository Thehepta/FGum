package com.test.fgum;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class KeepAliveService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        // 执行一些初始化操作
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(){
            @Override
            public void run() {
                while (true){
                    Log.e("RZx","onStartCommand");
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.start();
        // 返回 START_STICKY 确保服务被系统终止后重启
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}