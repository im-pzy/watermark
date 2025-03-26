package cn.impzy.watermark.services;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static androidx.core.content.ContextCompat.getSystemService;
import static cn.impzy.watermark.utils.TextWatermarkUtils.drawFullScreenTextWatermark;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.impzy.watermark.TextWatermark;

public class FullWatermarkService extends Service {
    private WindowManager windowManager;
    private ImageView watermarkView;
    private TextWatermark textWatermark;

    // 不需要绑定
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String watermarkText = intent.getStringExtra("watermark_text");
            int textAlpha = intent.getIntExtra("text_alpha", 128);
            int textSize = intent.getIntExtra("text_size", 24);
            int rotationAngle = intent.getIntExtra("rotation_angle", 45);
            showWatermark(textWatermark);
        }
        return START_STICKY;
    }

    private void showWatermark(TextWatermark textWatermark) {
        // 获取屏幕宽高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        Point screenSize = new Point(screenWidth, screenHeight);

        // 设置时间格式
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间（这里注意内存销毁）
        //String formattedTime = sdf.format(new Date());
        //textWatermark.setText(formattedTime);

        // ImageView imageView = new ImageView(this);
        watermarkView.setImageBitmap(drawFullScreenTextWatermark(textWatermark, screenSize));

        // 设置悬浮窗口参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |         // 不可获得焦点
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |         // 不可触摸
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,      // 全屏
                PixelFormat.TRANSLUCENT);
        // 设置悬浮窗口位置
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;
        // 添加悬浮视图到WindowManager
        windowManager.addView(watermarkView, params);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (watermarkView != null) {
            windowManager.removeView(watermarkView);
        }
    }
}
