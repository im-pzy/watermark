package cn.impzy.watermark.services;

import static cn.impzy.watermark.utils.TextWatermarkUtils.drawFullScreenTextWatermark;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import cn.impzy.watermark.TextWatermark;

public class FullWatermarkService extends Service {
    private WindowManager windowManager;
    private ImageView watermarkView;
    TextWatermark textWatermark;
    public static final String ACTION_SERVICE_STARTED = "cn.impzy.watermark.SERVICE_STARTED";
    public static final String ACTION_SERVICE_DESTROYED = "cn.impzy.watermark.SERVICE_DESTROYED";

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
        // 安卓12新特性，透明度≤0.8才可穿透触摸事件
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.alpha = 0.8f;
        }

        // 添加悬浮视图到WindowManager
        windowManager.addView(watermarkView, params);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            textWatermark = (TextWatermark) intent.getSerializableExtra("text_watermark");
            if (textWatermark != null) {
                showWatermark(textWatermark);
                // 发送广播通知 Service 被启动
                Log.d("WatermarkDebug", "发送Service启动广播");
                sendBroadcast(new Intent(ACTION_SERVICE_STARTED));
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
        // 发送广播通知 Service 被销毁
        Log.d("WatermarkDebug", "发送Service销毁广播");
        Intent intent = new Intent(ACTION_SERVICE_DESTROYED);
        sendBroadcast(intent);
    }
}
