package cn.impzy.watermark.ui;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
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

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import cn.impzy.watermark.R;
import cn.impzy.watermark.TextWatermark;
import static cn.impzy.watermark.utils.TextWatermarkUtils.drawTextWatermark;
import static cn.impzy.watermark.utils.TextWatermarkUtils.scaleBitmap;


public class AddWatermarkFragment extends Fragment {
    private TextWatermark textWatermark = TextWatermark.forAddWatermark();
    private ImageView imageView;
    private EditText watermarkEditText;
    private View colorSelector;
    private SeekBar textSizeSeekBar,rotationAngleSeekBar,textAlphaSeekBar,spaceScaleSeekBar;
    private AppCompatSpinner timeTypeSpinner;
    private TextView expireLable;
    private EditText expireNumEditText;
    private AppCompatSpinner expireUnitSpinner;
    private Bitmap originalBitmap;
    private Bitmap watermarkedBitmap;
    private Button saveButton;
    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE_READ_MEDIA_IMAGES = 2;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 3;


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
        expireLable = view.findViewById(R.id.expireLabel);
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
            if (checkStoragePermission()) {
                saveBitmapToExternalFilesDir(requireContext(), watermarkedBitmap);
            }
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

    // 接收Intent回调结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
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
            fileName.append("-有效期").append(textWatermark.getExpireNum()).append(textWatermark.getExpireUnit());
        }
        fileName.append(".png");

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName.toString());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/印");

        Uri imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            if (imageUri != null) {
                // 保存图片
                try (OutputStream outputStream = context.getContentResolver().openOutputStream(imageUri)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    Toast.makeText(requireContext(), "图片已保存："+fileName, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            Log.e("WatermarkPicSave", "图片保存失败", e);
            Toast.makeText(requireContext(), "图片保存失败", Toast.LENGTH_SHORT).show();
        }
        // 还要写入exif信息：水印内容，水印添加时间，添加软件名，水印参数
    }


    // 检查外部存储权限
    private boolean checkStoragePermission() {
        // Android 10及以上使用MediaStore API不需要存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    // 检查外部存储权限结果回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 读取图片权限回调
        if (requestCode == REQUEST_CODE_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAlbum();
            } else {
                Toast.makeText(requireContext(), "请允许读取存储权限", Toast.LENGTH_SHORT).show();
            }
        }
        // 外部存储写入权限回调
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "存储权限已授予", Toast.LENGTH_SHORT).show();
                // 权限获取成功后继续保存操作
                saveBitmapToExternalFilesDir(requireContext(), watermarkedBitmap);
            } else {
                Toast.makeText(requireContext(), "需要存储权限才能保存图片", Toast.LENGTH_SHORT).show();
            }
        }

    }


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
