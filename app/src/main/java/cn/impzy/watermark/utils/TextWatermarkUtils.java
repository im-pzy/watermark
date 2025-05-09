package cn.impzy.watermarker.utils;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.impzy.watermarker.TextWatermark;

public class TextWatermarkUtils {
    public static final String[] EXPIRE_UNIT_DATE = {"年","个月","天"};
    public static final String[] EXPIRE_UNIT_DATETIME = {"年","个月","天","小时","分钟"};
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
    public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());

    // 创建单个水印
    public static Bitmap createSingleTextWatermarkBitmap(TextWatermark textWatermark) {
        // 文本内容判空
        String text = textWatermark.getText();
        if (text.isEmpty()) {
            return null;
        }

        // 画笔
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textWatermark.getTextSize());
        textPaint.setColor(textWatermark.getTextColor());
        textPaint.setAlpha(textWatermark.getTextAlpha());
        textPaint.setAntiAlias(true);   // 抗锯齿
        textPaint.setTextAlign(Paint.Align.LEFT);   // 对齐方式

        // 设置水印文本
        String time = "";
        int timeType = textWatermark.getTimeType();
        String expireNum = textWatermark.getExpireNum();
        int expireUnit = textWatermark.getExpireUnit();
        switch (timeType) {
            case 0: // off
                break;
            case 1: // datetime
                time = DATETIME_FORMAT.format(new Date());
                text += "\n" + time + "\n" + "有效期" + expireNum + EXPIRE_UNIT_DATETIME[expireUnit];
                textWatermark.setTimeString(time);
                break;
            case 2: // date
                time = DATE_FORMAT.format(new Date());
                text += "\n" + time + "\n" + "有效期" + expireNum + EXPIRE_UNIT_DATE[expireUnit];
                textWatermark.setTimeString(time);
                break;
        }

        // 计算文字的宽度和高度
        String[] texts = text.split("\n");
        float maxWidth = 0f;
        for (String line : texts) {
            if (textPaint.measureText(line) > maxWidth) {
                maxWidth = textPaint.measureText(line);     // 计算最大宽度
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

        // 绘图
        Bitmap singleWatermark = Bitmap.createBitmap((int) Math.ceil(textWidth), (int) Math.ceil(textHeight), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(singleWatermark);
        staticLayout.draw(canvas);
        return singleWatermark;
    }

    // 旋转单个水印
    public static Bitmap rotateBitmap(Bitmap bitmap, int rotationAngle) {
        if (bitmap == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // 图片缩放
    public static Bitmap scaleBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int longSide = max(width, height);
        int shortSide = min(width, height);

        // 计算缩放比例
        float scaleFactor = 1f;
        if (shortSide >= 200 && longSide <= 1080) {
            // 短边大于200px，长边小于1080px，则不需要缩放
            return bitmap;
        } else if (longSide > 1080) {
            scaleFactor = 1080f / longSide;
        } else if (shortSide < 200) {
            scaleFactor = 200f / shortSide;
        }

        // 计算新的宽高
        int newWidth = (int) (width * scaleFactor);
        int newHeight = (int) (height * scaleFactor);

        // 创建缩放后的Bitmap
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    // 添加图片水印
    public static Bitmap drawTextWatermark(TextWatermark textWatermark, Bitmap originalBitmap) {
        if (originalBitmap ==  null) {
            return null;
        }

        // 创建bitmap副本和Canvas工具
        Bitmap workingBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(workingBitmap);

        // 处理单个水印
        Bitmap watermarkBitmap = createSingleTextWatermarkBitmap(textWatermark);
        Bitmap rotatedWatermarkBitmap = rotateBitmap(watermarkBitmap, textWatermark.getRotationAngle());
        if (rotatedWatermarkBitmap == null) {
            return originalBitmap;
        }

        // 重复平铺
        Paint paint = new Paint();
        Shader.TileMode tileMode = Shader.TileMode.REPEAT;
        paint.setShader(new BitmapShader(rotatedWatermarkBitmap, tileMode, tileMode));
        canvas.drawRect(canvas.getClipBounds(), paint);

        return workingBitmap;
    }

    // 全屏水印绘制
    public static Bitmap drawFullScreenTextWatermark(TextWatermark textWatermark, Point screenSize) {

        int screenWidth = screenSize.x;
        int screenHeight = screenSize.y;

        Bitmap workingBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(workingBitmap);

        // 处理单个水印
        Bitmap watermarkBitmap = createSingleTextWatermarkBitmap(textWatermark);
        Bitmap rotatedWatermarkBitmap = rotateBitmap(watermarkBitmap, textWatermark.getRotationAngle());
        if (rotatedWatermarkBitmap == null) {
            return null;
        }

        // 重复平铺
        Paint paint = new Paint();
        Shader.TileMode tileMode = Shader.TileMode.REPEAT;
        paint.setShader(new BitmapShader(rotatedWatermarkBitmap, tileMode, tileMode));
        canvas.drawRect(canvas.getClipBounds(), paint);

        return workingBitmap;
    }
}
