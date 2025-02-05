package cn.impzy.watermark.ui.fullscreenwatermark;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDiskIOException;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.serialization.InternalNavType;

import java.util.GregorianCalendar;

import cn.impzy.watermark.R;
import cn.impzy.watermark.TextWatermark;

public class FullScreenWatermarkFragment extends Fragment {

    private TextWatermark textWatermark;
    private WindowManager windowManager;
    private View floatingView;

    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_CODE_PICK_OVERLAY_PERMISSION = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fullscreenwatermark, container, false);


        // 初始化windowManager
        windowManager = (WindowManager) requireContext().getSystemService(Context.WINDOW_SERVICE);



        textWatermark = new TextWatermark();
        drawTextWatermark(textWatermark);
        //showWatermark();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 移除悬浮视图
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }

    private void showWatermark() {
        // 创建悬浮视图
        TextView textView = new TextView(requireContext());
        textView.setText("12323123123");

        // 设置悬浮窗口参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        // 设置悬浮窗口位置
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;
        // 添加悬浮视图到WindowManager
        windowManager.addView(textView, params);
    }

    // 悬浮其他应用上方权限检查
    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(requireContext())) {
                // 如果没有权限，则跳转到设置页面
                Toast.makeText(requireContext(), "需要悬浮窗权限，请开启", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:"+requireContext().getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_PICK_OVERLAY_PERMISSION);
            } else {
                // 权限已开启，显示悬浮窗
                showWatermark();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(requireContext())) {
                // 权限已开启，创建悬浮窗
                showWatermark();
            } else {
                Toast.makeText(requireContext(), "未开启悬浮窗权限，请重试", Toast.LENGTH_SHORT).show();
            }
        }
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

        Canvas canvas = new Canvas();
        for (int i = 0; i <= screenWidth; i += maxWidth) {
            for (int j = 0; i <= screenHeight; i += textHeight) {
                canvas.save();
                canvas.rotate(textWatermark.getRotationAngle(),i,j);
                canvas.drawText(watermarkText, i ,j, textPaint);
                canvas.restore();
            }
        }

//        Bitmap workingBitmap = Bitmap.createBitmap(width, height, originalBitmap.getConfig());
//        Canvas canvas = new Canvas(workingBitmap);
//        canvas.drawBitmap(originalBitmap, 0, 0, null);
//
//
//        // 处理单个水印
//        Bitmap watermarkBitmap = createSingleTextWatermarkBitmap(textWatermark);
//        Bitmap rotatedWatermarkBitmap = rotateBitmap(watermarkBitmap, textWatermark.getRotationAngle());
//
//        // 重复平铺
//        Paint paint = new Paint();
//        Shader.TileMode tileMode = Shader.TileMode.REPEAT;
//        paint.setShader(new BitmapShader(rotatedWatermarkBitmap, tileMode, tileMode));
//        canvas.drawRect(canvas.getClipBounds(), paint);


        //saveBitmapToExternalFilesDir(this, workingBitmap, "my_bitmap.png");

        //return workingBitmap;
        return null;
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

}

//以下是ai生成的参考代码，用来思考如何将绘制好的图层显示到屏幕上方

//
//private void createWatermarkWindow() {
//    WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//    if (windowManager == null) return;
//
//    // 创建悬浮窗布局
//    FrameLayout watermarkLayout = new FrameLayout(this);
//    watermarkLayout.setLayoutParams(new ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT));
//    watermarkLayout.setBackgroundColor(Color.TRANSPARENT);
//
//    // 设置悬浮窗的参数
//    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
//                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//            PixelFormat.TRANSLUCENT);
//
//    // 添加悬浮窗到WindowManager
//    windowManager.addView(watermarkLayout, params);
//
//    // 绘制水印
//    drawWatermark(watermarkLayout);
//}
//
//private void drawWatermark(FrameLayout watermarkLayout) {
//    // 获取屏幕宽高
//    DisplayMetrics displayMetrics = new DisplayMetrics();
//    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//    int screenWidth = displayMetrics.widthPixels;
//    int screenHeight = displayMetrics.heightPixels;
//
//    // 创建画布
//    Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
//    Canvas canvas = new Canvas(bitmap);
//
//    // 设置画笔
//    Paint paint = new Paint();
//    paint.setColor(Color.GRAY);
//    paint.setTextSize(50);
//    paint.setAlpha(100); // 设置透明度
//    paint.setAntiAlias(true);
//
//    // 获取文字宽度和高度
//    String watermarkText = "我是水印";
//    float textWidth = paint.measureText(watermarkText);
//    Paint.FontMetrics fontMetrics = paint.getFontMetrics();
//    float textHeight = fontMetrics.descent - fontMetrics.ascent;
//
//    // 计算每个水印的间隔
//    float intervalX = textWidth + 50;
//    float intervalY = textHeight + 50;
//
//    // 绘制水印
//    for (int i = 0; i * intervalY < screenHeight; i++) {
//        for (int j = 0; j * intervalX < screenWidth; j++) {
//            float x = j * intervalX;
//            float y = i * intervalY;
//            canvas.save();
//            canvas.rotate(45, x + textWidth / 2, y + textHeight / 2); // 旋转45度
//            canvas.drawText(watermarkText, x, y - fontMetrics.ascent, paint);
//            canvas.restore();
//        }
//    }
//
//    // 将Bitmap设置为悬浮窗的背景
//    watermarkLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
//}