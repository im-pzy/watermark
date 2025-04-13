package cn.impzy.watermark.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;

import cn.impzy.watermark.TextWatermark;

public class TextWatermarkUtils {
    // 创建单个水印
    public static Bitmap createSingleTextWatermarkBitmap(TextWatermark textWatermark) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textWatermark.getTextSize());
        textPaint.setColor(textWatermark.getTextColor());
        textPaint.setAlpha(textWatermark.getTextAlpha());
        textPaint.setAntiAlias(true);   // 抗锯齿
        textPaint.setTextAlign(Paint.Align.LEFT);   // 对齐方式

        // 计算文字的宽度和高度
        String text = textWatermark.getText();
        String[] texts = text.split("\n");
        float maxWidth = 0f;
        for (String line : texts) {
            if (textPaint.measureText(line) > maxWidth) {
                maxWidth = textPaint.measureText(line);
            }
        }
        float textWidth = maxWidth;

        StaticLayout.Builder builder = StaticLayout.Builder.obtain(text, 0, text.length(), textPaint, (int) Math.ceil(textWidth));
        StaticLayout staticLayout = builder.build();
        float textHeight = staticLayout.getHeight();

        // 添加水印间距
        int spaceScale = textWatermark.getSpaceScale();
        textWidth += textWidth * spaceScale / 500;
        textHeight += textHeight * spaceScale / 100;

        Bitmap singleWatermark = Bitmap.createBitmap((int) Math.ceil(textWidth), (int) Math.ceil(textHeight), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(singleWatermark);

        staticLayout.draw(canvas);
        return singleWatermark;
    }

    // 旋转单个水印
    public static Bitmap rotateBitmap(Bitmap bitmap, int rotationAngle) {
        // canvas有rotate方法
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap drawFullScreenTextWatermark(TextWatermark textWatermark, Point screenSize) {

        int screenWidth = screenSize.x;
        int screenHeight = screenSize.y;

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
    public static Bitmap drawAddTextWatermark() {

        return null;
    }
}
