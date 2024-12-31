package cn.impzy.watermark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private EditText mAccount;
    private EditText mPassword;
    private Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initListener();
    }

    private void initListener() {
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 登录按钮被点击逻辑
                Log.d(TAG, "按钮被点击了");
                handleLogin();
            }
        });
    }

    private void handleLogin() {
        String accountText = mAccount.getText().toString().trim();
        if (TextUtils.isEmpty(accountText)) {
            Toast.makeText(this, "输入的账号为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String passwordText = mPassword.getText().toString().trim();
        if (TextUtils.isEmpty(passwordText)) {
            Toast.makeText(this, "输入的密码为空", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, LoginokActivity.class);
        intent.putExtra("account", accountText);
        intent.putExtra("password", passwordText);
        startActivity(intent);
    }

    private void init() {
        mAccount = (EditText) this.findViewById(R.id.account);
        mPassword = (EditText) this.findViewById(R.id.password);
        mLogin = (Button) this.findViewById(R.id.login);
    }
}