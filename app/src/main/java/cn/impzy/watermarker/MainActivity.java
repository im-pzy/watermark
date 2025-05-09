package cn.impzy.watermarker;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 底部导航
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // 导航控制器
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            // 导航栏与控制器的联动
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem infoItem = menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "关于");
        infoItem.setIcon(R.drawable.ic_info);
        infoItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == Menu.FIRST) {
            View infoView = getLayoutInflater().inflate(R.layout.dialog_info, null);

            TextView versionTextView = infoView.findViewById(R.id.versionTextView);
            versionTextView.setText("版本 " + BuildConfig.VERSION_NAME);

            infoView.findViewById(R.id.projectAddressButton).setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/im-pzy/watermarker"));
                startActivity(intent);
            });

            infoView.findViewById(R.id.feedbackButton).setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:impzy@qq.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "水印工具反馈");
                startActivity(Intent.createChooser(intent, "发送反馈"));
            });

            new AlertDialog.Builder(this)
                    .setView(infoView)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
