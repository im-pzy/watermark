package cn.impzy.watermark.ui;

import static android.app.Activity.RESULT_OK;
import static java.lang.Integer.max;
import static java.lang.Integer.min;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.impzy.watermark.R;
import cn.impzy.watermark.TextWatermark;


public class AddWatermarkFragment extends Fragment {
    private TextWatermark textWatermark = new TextWatermark();
    private ImageView imageView;
    private TextInputLayout watermarkEditTextLayout;
    private EditText watermarkEditText;
    private View colorSelector;
    private SeekBar textSizeSeekBar,rotationAngleSeekBar,textAlphaSeekBar,spaceScaleSeekBar;
    private AppCompatSpinner timeTypeSpinner;
    private TextView expireLable;
    private EditText expireNumEditText;
    private AppCompatSpinner expireUnitSpinner;
    private Bitmap originalBitmap;
    private Button saveButton;
    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE_READ_MEDIA_IMAGES = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addwatermark, container, false);
        initViews(view);
        setupListeners();
        return view;
    }

    private void initViews(View view) {
        imageView = view.findViewById(R.id.imageView);
        watermarkEditTextLayout = view.findViewById(R.id.watermarkEditTextLayout);
        watermarkEditText = view.findViewById(R.id.watermarkEditText);
        colorSelector = view.findViewById(R.id.colorSelector);
        textSizeSeekBar = view.findViewById(R.id.textSizeSeekBar);
        rotationAngleSeekBar = view.findViewById(R.id.rotationAngleSeekBar);
        textAlphaSeekBar = view.findViewById(R.id.textAlphaSeekBar);
        spaceScaleSeekBar = view.findViewById(R.id.spaceScaleSeekBar);
        timeTypeSpinner = view.findViewById(R.id.timeTypeSpinner);
        expireLable = view.findViewById(R.id.expireLable);
        expireNumEditText = view.findViewById(R.id.expireNumEditText);
        expireUnitSpinner = view.findViewById(R.id.expireUnitSpinner);
        saveButton = view.findViewById(R.id.saveButton);

        // 初始化按钮的值
        watermarkEditText.setText(textWatermark.getText());
        colorSelector.setBackgroundColor(textWatermark.getTextAlpha() << 24 | textWatermark.getTextColor() & 0xFFFFFF);
        textSizeSeekBar.setProgress(textWatermark.getTextSize());
        rotationAngleSeekBar.setProgress(textWatermark.getRotationAngle());
        textAlphaSeekBar.setProgress(textWatermark.getTextAlpha());
        spaceScaleSeekBar.setProgress(textWatermark.getSpaceScale());
        timeTypeSpinner.setSelection(textWatermark.getTimeType());
        expireNumEditText.setText(textWatermark.getExpireNum());
        expireUnitSpinner.setSelection(textWatermark.getExpireUnit());
    }

    private void setupListeners() {
        // 图片选择监听器
        imageView.setOnClickListener(view -> selectPhoto());

        // 文本输入框实时更新水印
        watermarkEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textWatermark.setText(charSequence.toString());
                updateWatermark();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        // 颜色选择
        colorSelector.setOnClickListener(view -> {
            // 创建Dialog窗口
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("选择颜色");
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.color_select_dialog, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            // 为每个颜色方块设置监听器
            GridLayout colorGrid = dialogView.findViewById(R.id.colorGrid);
            for (int i = 0; i < colorGrid.getChildCount(); i++) {
                CardView colorView = (CardView) colorGrid.getChildAt(i);
                colorView.setOnClickListener(v -> {
                    int selectedColor = colorView.getCardBackgroundColor().getDefaultColor();
                    // 设置文字颜色
                    textWatermark.setTextColor(selectedColor);
                    // 更新界面显示颜色
                    updateColorDisplay();
                    // 更新水印
                    updateWatermark();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss(); // 关闭弹窗
                    }
                });
            }
            dialog.show();
        });

        // 文字大小
        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textWatermark.setTextSize(progress);
                updateWatermark();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // 旋转角度
        rotationAngleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textWatermark.setRotationAngle(progress);
                updateWatermark();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // 不透明度
        textAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textWatermark.setTextAlpha(progress);
                updateColorDisplay();
                updateWatermark();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // 水印间距
        spaceScaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textWatermark.setSpaceScale(progress);
                updateWatermark();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // 时间
        timeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                textWatermark.setTimeType(position);
                if (position == 0) {
                    expireLable.setEnabled(false);
                    expireNumEditText.setEnabled(false);
                    expireUnitSpinner.setEnabled(false);
                } else {
                    expireLable.setEnabled(true);
                    expireNumEditText.setEnabled(true);
                    expireUnitSpinner.setEnabled(true);
                }
                updateWatermark();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        // 有效期数值
        expireNumEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().isEmpty()) {
                    textWatermark.setExpireNum(charSequence.toString());
                }
                updateWatermark();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        // 有效期单位
        expireUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                textWatermark.setExpireUnit(position);
                updateWatermark();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

    }

    private void selectPhoto() {
        // 如果没有照片访问权限
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            // 请求权限并打开相册
            requestPermissions(new String[] {android.Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_READ_MEDIA_IMAGES);
            //openAlbum();
        } else {
            // 有权限则直接打开
            openAlbum();
        }
    }

    private void openAlbum() {
        Log.d("openAlbum","yes");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    // 接收权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAlbum();
            } else {
                Toast.makeText(requireContext(), "请允许读取存储权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 接收Intent回调结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                originalBitmap = scaleBitmap(MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri));
                updateWatermark();
            } catch (IOException e) {
                throw new RuntimeException(e);
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
        if (shortSide < 200) {
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
        if (bitmap == null) {
            return null;
        }
        // canvas有rotate方法
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap createSingleTextWatermarkBitmap(TextWatermark textWatermark) {

        String watermarkText = textWatermark.getText();
        if (watermarkText.isEmpty()){
            return null;
        }

        String[] text = watermarkText.split("\n");

        // 画笔
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textWatermark.getTextSize());
        textPaint.setColor(textWatermark.getTextColor());
        textPaint.setAlpha(textWatermark.getTextAlpha());
        textPaint.setAntiAlias(true);   // 抗锯齿
        textPaint.setTextAlign(Paint.Align.LEFT);   // 对齐方式

        // 计算文字的宽度和高度
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

    private Bitmap addTextWatermark(TextWatermark textWatermark, Bitmap originalBitmap) {
        if (originalBitmap == null) {
            return null;
        }

        // 创建bitmap副本
        Bitmap workingBitmap = Bitmap.createBitmap(originalBitmap);
        Canvas canvas = new Canvas(workingBitmap);

        // 处理单个水印
        Bitmap watermarkBitmap = createSingleTextWatermarkBitmap(textWatermark);
        if (watermarkBitmap == null) {
            return originalBitmap;
        }
        Bitmap rotatedWatermarkBitmap = rotateBitmap(watermarkBitmap, textWatermark.getRotationAngle());

        // 重复平铺
        Paint paint = new Paint();
        Shader.TileMode tileMode = Shader.TileMode.REPEAT;
        paint.setShader(new BitmapShader(rotatedWatermarkBitmap, tileMode, tileMode));
        canvas.drawRect(canvas.getClipBounds(), paint);
        return workingBitmap;

    }

    //saveBitmapToExternalFilesDir(this, workingBitmap, "my_bitmap.png");
    private void saveBitmapToExternalFilesDir(Context context, Bitmap bitmap, String fileName) {
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


    private void updateWatermark() {
        Bitmap watermarkedBitmap = addTextWatermark(textWatermark, originalBitmap);
        imageView.setImageBitmap(watermarkedBitmap);
    }

    // 更新colorSelector界面显示的颜色
    private void updateColorDisplay() {
        int textColor = textWatermark.getTextColor();
        int colorWithAlpha = Color.argb(textWatermark.getTextAlpha(), Color.red(textColor), Color.green(textColor), Color.blue(textColor));
        colorSelector.setBackgroundColor(colorWithAlpha);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
