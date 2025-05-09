package cn.impzy.watermarker.ui;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import cn.impzy.watermarker.R;
import cn.impzy.watermarker.TextWatermark;
import cn.impzy.watermarker.utils.TextWatermarkUtils;

import static cn.impzy.watermarker.utils.TextWatermarkUtils.drawTextWatermark;
import static cn.impzy.watermarker.utils.TextWatermarkUtils.scaleBitmap;

import com.google.android.material.snackbar.Snackbar;


public class AddWatermarkFragment extends Fragment {
    private final TextWatermark textWatermark = TextWatermark.forAddWatermark();
    private ImageView imageView;
    private EditText watermarkEditText;
    private View colorSelector;
    private SeekBar textSizeSeekBar,rotationAngleSeekBar,textAlphaSeekBar,spaceScaleSeekBar;
    private AppCompatSpinner timeTypeSpinner;
    private TextView expireLabel;
    private EditText expireNumEditText;
    private AppCompatSpinner expireUnitSpinner;
    private Bitmap originalBitmap;
    private Bitmap watermarkedBitmap;
    private Button saveButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addwatermark, container, false);
        initViews(view);
        setupListeners();
        return view;
    }

    private void initViews(View view) {
        imageView = view.findViewById(R.id.imageView);
        watermarkEditText = view.findViewById(R.id.watermarkEditText);
        colorSelector = view.findViewById(R.id.colorSelector);
        textSizeSeekBar = view.findViewById(R.id.textSizeSeekBar);
        rotationAngleSeekBar = view.findViewById(R.id.rotationAngleSeekBar);
        textAlphaSeekBar = view.findViewById(R.id.textAlphaSeekBar);
        spaceScaleSeekBar = view.findViewById(R.id.spaceScaleSeekBar);
        timeTypeSpinner = view.findViewById(R.id.timeTypeSpinner);
        expireLabel = view.findViewById(R.id.expireLabel);
        expireNumEditText = view.findViewById(R.id.expireNumEditText);
        expireUnitSpinner = view.findViewById(R.id.expireUnitSpinner);
        saveButton = view.findViewById(R.id.saveButton);

        // 初始化按钮的值
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
        imageView.setOnClickListener(view -> {
            // Android 13及以上使用READ_MEDIA_IMAGES，以下使用READ_EXTERNAL_STORAGE
            String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                    android.Manifest.permission.READ_MEDIA_IMAGES :
                    android.Manifest.permission.READ_EXTERNAL_STORAGE;
            // 检查权限并执行对应代码
            requestPermissionLauncher.launch(permission);
        });

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
                    expireLabel.setEnabled(false);
                    expireNumEditText.setEnabled(false);
                    expireUnitSpinner.setEnabled(false);
                } else {
                    expireLabel.setEnabled(true);
                    expireNumEditText.setEnabled(true);
                    expireUnitSpinner.setEnabled(true);
                    // 根据不同的timeType设置有效期单位
                    int arrayResId = (position == 1) ?
                            R.array.expire_datetime_spinner:
                            R.array.expire_date_spinner;
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), arrayResId, android.R.layout.simple_spinner_dropdown_item);
                    expireUnitSpinner.setAdapter(adapter);
                    expireUnitSpinner.setSelection(textWatermark.getExpireUnit());
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

        // 保存按钮
        saveButton.setOnClickListener(view -> {
            // 没有选择图片
            if (originalBitmap == null) {
                Toast.makeText(requireContext(), "请先选择图片", Toast.LENGTH_SHORT).show();
                return;
            }
            // 水印内容判空
            String editText = watermarkEditText.getText().toString().trim();
            if (editText.isEmpty()) {
                Toast.makeText(requireContext(), "水印内容为空", Toast.LENGTH_SHORT).show();
                return;
            }
            // 检查权限并执行对应代码(执行由回调函数完成)
            checkStoragePermission();
        });

    }

    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    // 纠正图片显示方向
    public void correctDirection(Uri selectedImageUri) {
        try {
            ExifInterface exif = new ExifInterface(Objects.requireNonNull(requireContext().getContentResolver().openInputStream(selectedImageUri)));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            // 读取旋转角度
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
            }

            // 旋转缩放后的图片
            if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
            }
        } catch (IOException | NullPointerException e) {
            Log.e("ExifInterface", "读取EXIF发生数据错误",e);
            Toast.makeText(requireContext(), "读取图片信息失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 保存图片
    private void saveBitmapToExternalFilesDir(Context context, Bitmap bitmap) {
        StringBuilder fileName = new StringBuilder();
        // 替换水印文字的非法字符
        fileName.append(textWatermark.getText().replaceAll("\n","_").replaceAll("[\\\\/:*?\"<>|]",""));
        if (textWatermark.getTimeType() > 0) {
            fileName.append("-").append(textWatermark.getTimeString().replaceAll("[/: ]", ""));
            fileName.append("-有效期").append(textWatermark.getExpireNum()).append(TextWatermarkUtils.EXPIRE_UNIT_DATETIME[textWatermark.getExpireUnit()]);
        }
        fileName.append(".png");

        // Android 10 及以上使用MediaStore API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName.toString());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/印");

            Uri imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            try {
                if (imageUri != null && bitmap != null) {
                    // 保存图片
                    try (OutputStream outputStream = context.getContentResolver().openOutputStream(imageUri)) {
                        if (outputStream != null) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        }
                        Snackbar.make(requireView(), "图片已保存到：Pictures/印/" + fileName, Snackbar.LENGTH_LONG).show();
                    }
                }
            } catch (IOException e) {
                Log.e("WatermarkPicSave", "图片保存失败", e);
                Toast.makeText(requireContext(), "图片保存失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Android 10以下使用传统文件系统API
            File picturesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "印");
            if (!picturesDir.exists() && !picturesDir.mkdirs()) {
                Toast.makeText(requireContext(), "创建目录失败", Toast.LENGTH_SHORT).show();
                return;
            }

            File imageFile = new File(picturesDir, fileName.toString());
            try {
                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    // 通知媒体库更新
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
                    Snackbar.make(requireView(), "图片已保存到：Pictures/印/" + fileName, Snackbar.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                Log.e("WatermarkPicSave", "图片保存失败", e);
                Toast.makeText(requireContext(), "图片保存失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 打开图库请求结果回调
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    try {
                        // 先缩放图片
                        originalBitmap = scaleBitmap(MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri));
                        // 再旋转
                        correctDirection(selectedImageUri);
                        updateWatermark();
                    } catch (IOException e) {
                        Log.e("ImageLoading", "图片加载失败", e);
                        Toast.makeText(requireContext(), "图片加载失败，请重试", Toast.LENGTH_SHORT).show();
                    } catch (OutOfMemoryError e) {
                        Log.e("ImageLoading", "内存不足", e);
                        Toast.makeText(requireContext(), "图片太大，内存不足", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // 读取图库权限请求结果回调
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openAlbum();
                } else {
                    Toast.makeText(requireContext(), "请允许读取存储权限", Toast.LENGTH_SHORT).show();
                }
            });

    // 检查外部存储权限
    private void checkStoragePermission() {
        // Android 10及以上使用MediaStore API不需要存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveBitmapToExternalFilesDir(requireContext(), watermarkedBitmap);
        } else {
            storagePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    // 存储权限请求结果回调
    private final ActivityResultLauncher<String> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    saveBitmapToExternalFilesDir(requireContext(), watermarkedBitmap);
                } else {
                    Toast.makeText(requireContext(), "需要存储权限才能保存图片", Toast.LENGTH_SHORT).show();
                }
            });

    private void updateWatermark() {
        watermarkedBitmap = drawTextWatermark(textWatermark, originalBitmap);
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
