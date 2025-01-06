package cn.impzy.watermark;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void login(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void openCamera(View view) {
        // 第一种写法
        Intent intent = new Intent();
        intent.setClassName("com.android.camera", "com.android.camera.Camera");
        startActivity(intent);
    }

    public void call(View view) {
        // 权限检查
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 4);
        } else {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.CALL");
            intent.addCategory("android.intent.category.DEFAULT");

            Uri uri = Uri.parse("tel:10086");
            intent.setData(uri);
            startActivity(intent);
        }
    }
}
