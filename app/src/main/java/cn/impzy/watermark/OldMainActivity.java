package cn.impzy.watermark;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class OldMainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CALL = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_old);
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

    /*打电话*/
    public void call(View view) {
        // 权限检查
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE_CALL);
        } else {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.CALL");
            intent.addCategory("android.intent.category.DEFAULT");

            Uri uri = Uri.parse("tel:10086");
            intent.setData(uri);
            startActivity(intent);
        }
    }

     /* 打开照片 */
    public void selectPhoto(View view) {
        Intent intent = new Intent(this, SelectPhotoActivity.class);
        startActivity(intent);
    }

    /* 测试按钮 */
    public void test(View view) {
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }


}
