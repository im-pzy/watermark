package cn.impzy.watermark.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.os.Build;
import android.os.IBinder;
import android.text.StaticLayout;
import android.text.TextPaint;
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
    private View watermarkView;
    private WindowManager.LayoutParams params;

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
            showWatermark(watermarkText, textAlpha, textSize, rotationAngle);
        }
        return START_STICKY;
    }

    private void showWatermark() {
        // 创建悬浮视图
        textWatermark = new TextWatermark();

        // 设置时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间（这里注意内存销毁）
        String formattedTime = sdf.format(new Date());
        textWatermark.setText(formattedTime);

        ImageView imageView = new ImageView(requireContext());
        imageView.setImageBitmap(drawTextWatermark(textWatermark));

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
        windowManager.addView(imageView, params);
    }

    private Bitmap drawTextWatermark(TextWatermark textWatermark) {
        // 获取屏幕宽高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textWatermark.getTextSize());
        textPaint.setColor(textWatermark.getTextColor());
        textPaint.setAlpha(textWatermark.getTextAlpha());
        textPaint.setAntiAlias(true);   // 抗锯齿
        textPaint.setTextAlign(Paint.Align.LEFT);   // 对齐方式

        // 计算文字的宽度和高度
        String watermarkText = textWatermark.getText();
        String[] text = watermarkText.split("\n");
        float maxWidth = 0f;
        for (String line : text) {
            if (textPaint.measureText(line) > maxWidth) {
                maxWidth = textPaint.measureText(line);
            }
        }
        float textWidth = maxWidth;
        StaticLayout.Builder builder = StaticLayout.Builder.obtain(watermarkText, 0, watermarkText.length(), textPaint, (int) Math.ceil(textWidth));
        StaticLayout staticLayout = builder.build();
        float textHeight = staticLayout.getHeight();


        Bitmap workingBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(workingBitmap);


        // 处理单个水印
        Bitmap watermarkBitmap = createSingleTextWatermarkBitmap(textWatermark);
        Bitmap rotatedWatermarkBitmap = rotateBitmap(watermarkBitmap, textWatermark.getRotationAngle());

        // 重复平铺
        Paint paint = new Paint();
        Shader.TileMode tileMode = Shader.TileMode.REPEAT;
        paint.setShader(new BitmapShader(rotatedWatermarkBitmap, tileMode, tileMode));
        canvas.drawRect(canvas.getClipBounds(), paint);


        //saveBitmapToExternalFilesDir(this, workingBitmap, "my_bitmap.png");

        return workingBitmap;
    }
    // 旋转单个水印
    private Bitmap rotateBitmap(Bitmap bitmap, int rotationAngle) {
        // canvas有rotate方法
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap createSingleTextWatermarkBitmap(TextWatermark textWatermark) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textWatermark.getTextSize());
        textPaint.setColor(textWatermark.getTextColor());
        textPaint.setAlpha(textWatermark.getTextAlpha());
        textPaint.setAntiAlias(true);   // 抗锯齿
        textPaint.setTextAlign(Paint.Align.LEFT);   // 对齐方式

        // 计算文字的宽度和高度
        String watermarkText = textWatermark.getText();
        String[] text = watermarkText.split("\n");
        float maxWidth = 0f;
        for (String line : text) {
            if (textPaint.measureText(line) > maxWidth) {
                maxWidth = textPaint.measureText(line);
            }
        }
        float textWidth = maxWidth;

        StaticLayout.Builder builder = StaticLayout.Builder.obtain(watermarkText, 0, watermarkText.length(), textPaint, (int) Math.ceil(textWidth));
        StaticLayout staticLayout = builder.build();
        float textHeight = staticLayout.getHeight();

        Bitmap singleWatermark = Bitmap.createBitmap((int) Math.ceil(textWidth), (int) Math.ceil(textHeight), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(singleWatermark);

        staticLayout.draw(canvas);
        return singleWatermark;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (watermarkView != null) {
            windowManager.removeView(watermarkView);
        }
    }
}
