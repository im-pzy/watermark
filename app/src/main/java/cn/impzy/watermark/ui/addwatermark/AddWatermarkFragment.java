package cn.impzy.watermark.ui.addwatermark;

import static android.app.Activity.RESULT_OK;
import static java.lang.Integer.max;
import static java.lang.Integer.min;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.impzy.watermark.R;
import cn.impzy.watermark.TextWatermark;

public class AddWatermarkFragment extends Fragment {
    private TextWatermark textWatermark;

    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 2;
    private ImageView imageView;
    private EditText textEditText;
    private SeekBar textSizeSeekBar, textColorSpinner, textAlphaSeekBar;
    private Spinner fontSpinner;
    private SeekBar rotationAngleSeekBar;
    private Button saveButton;

    private Bitmap watermarkedBitmap;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addwatermark, container, false);
        textWatermark = new TextWatermark();
        // 初始化控件
        imageView = view.findViewById(R.id.imageView);
        textEditText = view.findViewById(R.id.textEditText);
        textSizeSeekBar = view.findViewById(R.id.textSizeSeekBar);
        //textColorSpinner = view.findViewById(R.id.textColorSpinner);
        textAlphaSeekBar = view.findViewById(R.id.textAlphaSeekBar);
        fontSpinner = view.findViewById(R.id.fontSpinner);
        rotationAngleSeekBar = view.findViewById(R.id.rotationAngleSeekBar);
        saveButton = view.findViewById(R.id.saveButton);

        // 控件值初始化
        textSizeSeekBar.setProgress(textWatermark.getTextSize());
        textAlphaSeekBar.setProgress(textWatermark.getTextAlpha());
        rotationAngleSeekBar.setProgress(textWatermark.getRotationAngle());

        // 设置监听器
        imageView.setOnClickListener(imageSelectListener);
        textEditText.addTextChangedListener(textChangeListener);
        textSizeSeekBar.setOnSeekBarChangeListener(textSizeChangeListener);
        textAlphaSeekBar.setOnSeekBarChangeListener(textAlphaChangeListener);
        rotationAngleSeekBar.setOnSeekBarChangeListener(rotationAngleChangeListener);
//        textColorSpinner.setOnSeekBarChangeListener(textColorChangeListener);

//        fontSpinner.setOnItemSelectedListener(fontSelectedListener);

//        int scale = 30;
//        int w = originalBitmap.getWidth();
//        int h = originalBitmap.getHeight();
//        this.size = Math.min(w/scale, h/scale);
//        Log.d("addText", String.valueOf(this.size));
        return view;
    }

    public void selectPhoto(View view) {
        // 运行时请求权限
        if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_READ_EXTERNAL_STORAGE);
        } else {
            openAlbum();
        }
    }

    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    // 接收结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==  REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
            // 加载图片
            try {
                Bitmap originalBitmap = scaleBitmap(MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri));
                textWatermark.setOriginalBitmap(originalBitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAlbum();
            } else {
                Toast.makeText(requireActivity(), "请允许读取存储权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap) {
        // canvas有scale方法
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int longSide = max(width, height);
        int shortSide = min(width, height);

        // 符合条件：短边大于200px，长边小于1080px
        if (shortSide >= 200 && longSide <= 1080) {
            return bitmap;
        }

        // 计算比例
        float scaleFactor = 1f;
        if (longSide > 1080) {
            scaleFactor = 1080f / longSide;
        }
        if (shortSide < 200){
            scaleFactor = 200f / shortSide;
        }

        // 计算新的宽高
        int newWidth = (int) (width * scaleFactor);
        int newHeight = (int) (height * scaleFactor);

        // 创建缩放后的Bitmap
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
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

    private Bitmap addTextWatermark(TextWatermark textWatermark) {
        Bitmap originalBitmap = textWatermark.getOriginalBitmap();
        if (originalBitmap == null) {
            return null;
        }

        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        Log.d("SelectPhotoActivity", "宽度: " + width + ", h: " + height);

        // 创建bitmap副本
        // （不能基于src修改，这样会导致每次调整都在上一次水印绘制结果的基础上进行再绘制）
        Bitmap workingBitmap = Bitmap.createBitmap(width, height, originalBitmap.getConfig());
        Canvas canvas = new Canvas(workingBitmap);
        canvas.drawBitmap(originalBitmap, 0, 0, null);


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

    public void saveBitmapToExternalFilesDir(Context context, Bitmap bitmap, String fileName) {
        // 获取应用专属的外部存储目录
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir != null) {
            // 构建文件路径，这里使用了一个简单的文件名，你可以根据需要生成唯一的文件名
            File bitmapFile = new File(externalFilesDir, "my_bitmap.png");

            // 将Bitmap写入文件
            try (FileOutputStream out = new FileOutputStream(bitmapFile)) {
                // 将Bitmap压缩为PNG格式并写入文件输出流
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                // 成功保存Bitmap
                Log.d("BitmapSave", "Bitmap saved to " + bitmapFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                // 处理保存失败的情况
                Log.e("BitmapSave", "Failed to save bitmap", e);
            }
        } else {
            // 处理获取外部存储目录失败的情况
            Log.e("BitmapSave", "Failed to get external files directory");
        }
        Log.d("Imagehahah", String.valueOf(externalFilesDir));
    }


    /*控件监听器*/
    private View.OnClickListener imageSelectListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            selectPhoto(view);
        }
    };
    private TextWatcher textChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            textWatermark.setText(charSequence.toString());
            Log.d("Text", String.valueOf(textWatermark.getText()));
            updateWatermark();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private SeekBar.OnSeekBarChangeListener textSizeChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            textWatermark.setTextSize(30 + progress);
            Log.d("TextSize", String.valueOf(textWatermark.getTextSize()));
            updateWatermark();
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private SeekBar.OnSeekBarChangeListener textAlphaChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            textWatermark.setTextAlpha(i);
            Log.d("TextAlpha", String.valueOf(textWatermark.getTextAlpha()));
            updateWatermark();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private SeekBar.OnSeekBarChangeListener rotationAngleChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            textWatermark.setRotationAngle(progress);
            updateWatermark();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private void updateWatermark() {
        watermarkedBitmap = addTextWatermark(textWatermark);
        imageView.setImageBitmap(watermarkedBitmap);
    }
}
