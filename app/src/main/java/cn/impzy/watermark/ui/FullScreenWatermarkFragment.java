package cn.impzy.watermark.ui;

import android.content.Context;
import android.content.Intent;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cn.impzy.watermark.R;
import cn.impzy.watermark.TextWatermark;
import cn.impzy.watermark.services.FullWatermarkService;

public class FullScreenWatermarkFragment extends Fragment {
    private EditText watermarkText;
    private View colorSelector;
    private Switch timeSwitch;
    private SeekBar textSizeSeekBar;
    private SeekBar rotationAngleSeekBar;
    private SeekBar textAlphaSeekBar;
    private Button showButton;
    private boolean isWatermarkEnabled = false;     // 如果切换fragement，就会导致其又变回false


    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_CODE_PICK_OVERLAY_PERMISSION = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fullscreenwatermark, container, false);
        initViews(view);
        setupListeners();
        return view;
    }

    private void initViews(View view) {
        watermarkText = view.findViewById(R.id.watermarkText);          // 这里会不会和添加水印里面的id重复
        colorSelector = view.findViewById(R.id.colorSelector);
        textSizeSeekBar = view.findViewById(R.id.textSizeSeekBar);
        rotationAngleSeekBar = view.findViewById(R.id.rotationAngleSeekBar);
        textAlphaSeekBar = view.findViewById(R.id.textAlphaSeekBar);
        showButton = view.findViewById(R.id.showButton);
    }
    private void setupListeners() {
        // 开启/关闭水印按钮
        showButton.setOnClickListener(view -> {
            if (checkOverlayPermission()) {
                isWatermarkEnabled = !isWatermarkEnabled;
                if (isWatermarkEnabled) {
                    startWatermarkService();
                    showButton.setText("关闭水印");
                } else {
                    stopWatermarkService();
                    showButton.setText("开启水印");
                    // 再变个颜色
                }
            }
        });

        // 文本输入框实时更新水印
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isWatermarkEnabled) {
                    startWatermarkService();
                } else {
                    isWatermarkEnabled = false;
                    //showButton.setText("开启水印");     // 权限被拒绝时
                    Toast.makeText(requireContext(), "请开启悬浮窗权限", Toast.LENGTH_SHORT).show();       // 是否多余
                }
            }
        };
        watermarkText.addTextChangedListener(textWatcher);

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (isWatermarkEnabled) {
                    startWatermarkService();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        };
        textSizeSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        textSizeSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        rotationAngleSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(requireContext())) {
                // 权限已开启，创建悬浮窗
                startWatermarkService();
                showButton.setText("关闭水印");     // 多余？
            } else {
                Toast.makeText(requireContext(), "未成功开启悬浮窗权限，请重试", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startWatermarkService() {
        Intent intent = new Intent(requireContext(), FullWatermarkService.class);
        intent.putExtra("watermark_text", watermarkText.getText().toString());
        intent.putExtra("text_size", textSizeSeekBar.getProgress());
        intent.putExtra("rotation_angle", rotationAngleSeekBar.getProgress());
        intent.putExtra("text_alpha", textAlphaSeekBar.getProgress());
        Log.d("ceshi", String.valueOf(textSizeSeekBar.getProgress()));
        requireContext().startService(intent);
    }

    private void stopWatermarkService() {
        requireContext().stopService(new Intent(requireContext(), FullWatermarkService.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopWatermarkService();
    }
}