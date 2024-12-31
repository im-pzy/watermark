package cn.impzy.watermark;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginokActivity extends AppCompatActivity {
    private static final String TAG = "LoginokActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginok);

        TextView accountBox = this.findViewById(R.id.accountBox);
        TextView passwordBox = this.findViewById(R.id.passwordBox);

        Intent intent = getIntent();
        String account = intent.getStringExtra("account");
        String password = intent.getStringExtra("password");

        Log.d(TAG, "账号："+account);
        Log.d(TAG, "密码："+password);

        accountBox.setText("账号："+account);
        passwordBox.setText("密码："+password);
    }
}
