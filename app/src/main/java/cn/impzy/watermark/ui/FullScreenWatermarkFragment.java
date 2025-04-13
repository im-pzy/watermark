package cn.impzy.watermark.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

import cn.impzy.watermark.R;
import cn.impzy.watermark.TextWatermark;
import cn.impzy.watermark.services.FullWatermarkService;

public class FullScreenWatermarkFragment extends Fragment {
    private TextWatermark textWatermark = new TextWatermark();
    private TextInputLayout watermarkEditTextLayout;
    private EditText watermarkEditText;
    private View colorSelector;
    private SwitchMaterial timeSwitch;
    private SeekBar textSizeSeekBar;
    private SeekBar rotationAngleSeekBar;
    private SeekBar textAlphaSeekBar;
    private SeekBar spaceScaleSeekBar;
    private Button showButton;
    private boolean isWatermarkEnabled = false;     // 如果切换fragement，就会导致其又变回false
    private static final int REQUEST_CODE_PICK_OVERLAY_PERMISSION = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fullscreenwatermark, container, false);
        initViews(view);
        setupListeners();
        return view;
    }

    private void initViews(View view) {
        watermarkEditTextLayout = view.findViewById(R.id.watermarkEditTextLayout);
        watermarkEditText = view.findViewById(R.id.watermarkEditText);          // 这里会不会和添加水印里面的id重复
        colorSelector = view.findViewById(R.id.colorSelector);
        textSizeSeekBar = view.findViewById(R.id.textSizeSeekBar);
        rotationAngleSeekBar = view.findViewById(R.id.rotationAngleSeekBar);
        textAlphaSeekBar = view.findViewById(R.id.textAlphaSeekBar);
        spaceScaleSeekBar = view.findViewById(R.id.spaceScaleSeekBar);
        showButton = view.findViewById(R.id.showButton);

        // 初始化按钮的值
        watermarkEditText.setText(textWatermark.getText());
        colorSelector.setBackgroundColor(textWatermark.getTextAlpha() << 24 | textWatermark.getTextColor() & 0xFFFFFF);
        textSizeSeekBar.setProgress(textWatermark.getTextSize());
        rotationAngleSeekBar.setProgress(textWatermark.getRotationAngle());
        textAlphaSeekBar.setProgress(textWatermark.getTextAlpha());
        spaceScaleSeekBar.setProgress(textWatermark.getSpaceScale());
    }

    private void setupListeners() {
        // 文本输入框实时更新水印
        watermarkEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textWatermark.setText(charSequence.toString());
                // 权限检查、水印内容合法性检验，合法则自动开启水印
                if (checkOverlayPermission() && checkWatermarkText()) {
                    startWatermarkService();
                } else {
                    stopWatermarkService();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        // ！这里需要修改，让输入框失去焦点后，取消错误提示（我已经确认了必须要切换fragment才能失去焦点）
        // 然后还需要处理两个问题：1.有错误提示的时候，光标消失  2.从别的fragment切换到这个fragment时，由于水印文本默认是空的，又会自动触发错误
        watermarkEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                Toast.makeText(requireContext(), "我失去焦点啦", Toast.LENGTH_SHORT).show();
                watermarkEditTextLayout.setError(null);
            }
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
                    updateColorDisplay();
                    // 更新水印
                    if(isWatermarkEnabled) {
                        startWatermarkService();
                    }
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss(); // 关闭弹窗
                    }
                });
            }
            dialog.show();
        });

        // 时间


        // 文字大小
        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textWatermark.setTextSize(progress);
                if (isWatermarkEnabled) {
                    startWatermarkService();
                }
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
                if (isWatermarkEnabled) {
                    startWatermarkService();
                }
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
                if (isWatermarkEnabled) {
                    startWatermarkService();
                }
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
                if (isWatermarkEnabled) {
                    startWatermarkService();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // 开启关闭水印按钮
        showButton.setOnClickListener(view -> {
            // 权限检查、水印内容合法性检验
            if (checkOverlayPermission() && checkWatermarkText()) {
                if (!isWatermarkEnabled) {
                    startWatermarkService();
                } else {
                    stopWatermarkService();
                }
            }
        });
    }

    // 悬浮其他应用上方权限检查
    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {       // 处理SYSTEM_ALERT_WINDOW权限兼容
            if (!Settings.canDrawOverlays(requireContext())) {      // 如果没有悬浮窗权限
                Toast.makeText(requireContext(), "请开启悬浮窗权限", Toast.LENGTH_SHORT).show();
                // 跳转到设置页面
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:"+requireContext().getPackageName()));
                // 能否直接跳转到这个app的所有权限界面设置
                startActivityForResult(intent, REQUEST_CODE_PICK_OVERLAY_PERMISSION);
                Toast.makeText(requireContext(), "请选择\"印\"并开启权限", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    // 文本内容合法性检验
    private boolean checkWatermarkText() {
        // 水印为空
        if (textWatermark.getText().trim().isEmpty()) {
            // Toast.makeText(requireContext(), "水印为空", Toast.LENGTH_SHORT).show();
            watermarkEditTextLayout.setError("水印内容为空");
            return false;
        } else {
            watermarkEditTextLayout.setError(null);
            return true;
        }
    }

    // 更新colorSelector界面显示的颜色
    private void updateColorDisplay() {
        int textColor = textWatermark.getTextColor();
        int colorWithAlpha = Color.argb(textWatermark.getTextAlpha(), Color.red(textColor), Color.green(textColor), Color.blue(textColor));
        colorSelector.setBackgroundColor(colorWithAlpha);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(requireContext())) {
                Toast.makeText(requireContext(), "悬浮窗权限已开启", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "未成功开启悬浮窗权限，请重试", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startWatermarkService() {
        Intent intent = new Intent(requireContext(), FullWatermarkService.class);
        intent.putExtra("text_watermark", textWatermark);
        requireContext().startService(intent);
        isWatermarkEnabled = true;
        showButton.setText("关闭水印");
        showButton.setSelected(true);
    }

    private void stopWatermarkService() {
        requireContext().stopService(new Intent(requireContext(), FullWatermarkService.class));
        isWatermarkEnabled = false;
        showButton.setText("开启水印");
        showButton.setSelected(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopWatermarkService();
    }
}