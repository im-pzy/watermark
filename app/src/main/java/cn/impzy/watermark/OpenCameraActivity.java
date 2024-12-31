package cn.impzy.watermark;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class OpenCameraActivity extends AppCompatActivity {
    private static final String TAG = "OpenCameraActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_camera);
    }
    public void openCamera(View view) {
        Log.d(TAG, "openCamera");

        // 第一种写法
        Intent intent = new Intent();
        intent.setClassName("com.android.camera", "com.android.camera.Camera");

        // 第二种写法
        ComponentName componentName = new ComponentName("com.android.camera", "com.android.camera.Camera");
        intent.setComponent(componentName);

        startActivity(intent);
    }
}
