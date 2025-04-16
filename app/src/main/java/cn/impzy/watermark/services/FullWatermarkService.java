package cn.impzy.watermark.services;

import static cn.impzy.watermark.utils.TextWatermarkUtils.drawFullScreenTextWatermark;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;

import cn.impzy.watermark.TextWatermark;

public class FullWatermarkService extends Service {
    private WindowManager windowManager;
    private ImageView watermarkView;
    TextWatermark textWatermark;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }       // 不需要绑定

    @Override
    public void onCreate() {
        super.onCreate();
        watermarkView = new ImageView(FullWatermarkService.this);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 设置悬浮窗口参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |           // 不可获得焦点
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |         // 不可触摸
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |      // 允许窗口占用整个屏幕
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,     // 允许窗口延申到装饰区
                PixelFormat.TRANSLUCENT);

        // 添加悬浮视图到WindowManager
        windowManager.addView(watermarkView, params);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            textWatermark = (TextWatermark) intent.getSerializableExtra("text_watermark");
            if (textWatermark != null) {
                showWatermark(textWatermark);
            }
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
        watermarkView.setImageBitmap(drawFullScreenTextWatermark(textWatermark, screenSize));
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManager.removeView(watermarkView);
    }
}
