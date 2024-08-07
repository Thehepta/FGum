package com.test.fgum;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.test.fgum.databinding.ActivityMainBinding;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'fgum' library on application startup.


    private ActivityMainBinding binding;
    static public MainActivity INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        INSTANCE = this;

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        Button bt = binding.pocBtn;
        Button hook_bt = binding.hookBtn;
        Button load_script = binding.loadScript;
        load_script.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    @Override
                    public void run() {
//                        LoadEntry.test();
                    }
                };

            }
        });
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv.setText("test");
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadEntry.Entry(getApplicationContext(),getApplicationInfo().sourceDir,"");
            }
        });
        hook_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("Rzx","231321312");
            }
        });
    }

    static public void update_text(String msg) {
        MainActivity.INSTANCE.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = MainActivity.INSTANCE.binding.sampleText;
                tv.append("\n" + msg + "\n");
            }
        });
    }

    public boolean load_so_flag = false;

    public void tryfridagumjs(View view) throws IOException {
        // 演示项目 只加载一次
        if (load_so_flag) return;
        load_so_flag = true;
        // 从 assets 中释放js
        String jsPath = getApplicationInfo().dataDir + "/hook.js";
        InputStream is = getAssets().open("hook.js");
        FileOutputStream fos = new FileOutputStream(jsPath);
        byte[] buffer = new byte[1024];
        int byteCount = 0;
        while ((byteCount = is.read(buffer)) != -1) {
            fos.write(buffer, 0, byteCount);
        }
        fos.flush();// 刷新缓冲区
        is.close();
        fos.close();

//        loadJS(jsPath);
    }

    /**
     * A native method that is implemented by the 'fgum' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();

//    public native boolean loadJS(String jspath);
}