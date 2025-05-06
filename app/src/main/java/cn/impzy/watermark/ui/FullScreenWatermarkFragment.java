package cn.impzy.watermark.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import cn.impzy.watermark.R;
import cn.impzy.watermark.TextWatermark;
import cn.impzy.watermark.services.FullWatermarkService;

public class FullScreenWatermarkFragment extends Fragment {
    private TextWatermark textWatermark = new TextWatermark();
    private EditText watermarkEditText;
    private View colorSelector;
    private SeekBar textSizeSeekBar,rotationAngleSeekBar,textAlphaSeekBar,spaceScaleSeekBar;
    private AppCompatSpinner timeTypeSpinner;
    private TextView expireLabel;
    private EditText expireNumEditText;
    private AppCompatSpinner expireUnitSpinner;
    private Button showButton;
    private boolean isWatermarkEnabled = false;
    private static final int REQUEST_CODE_PICK_OVERLAY_PERMISSION = 1001;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fullscreenwatermark, container, false);
        initViews(view);
        setupListeners();
        return view;
    }

    private void initViews(View view) {
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
        showButton = view.findViewById(R.id.showButton);

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
        // 文本输入框实时更新水印
        watermarkEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // 非用户实际输入
                if (!watermarkEditText.hasFocus()) {
                    return;
                }
                // 权限检查
                if (!checkOverlayPermission()) {
                    return;
                }
                // 水印内容判空(trim()放到if中，是为了可以通过换行调整水印间距)
                String editText = charSequence.toString();
                if (editText.trim().isEmpty()) {
                    stopWatermarkService();
                    return;
                }
                // 水印内容不为空，将输入框内容传递给水印类并自动开启水印
                textWatermark.setText(editText);
                startWatermarkService();
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
                if (isWatermarkEnabled) {
                    startWatermarkService();
                }
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
                if (isWatermarkEnabled) {
                    startWatermarkService();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        // 有效期单位
        expireUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                textWatermark.setExpireUnit(position);
                if (isWatermarkEnabled) {
                    startWatermarkService();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        // 开启关闭水印按钮
        showButton.setOnClickListener(view -> {
            // 权限检查
            if (!checkOverlayPermission()) {
                return;
            }
            // 水印内容判空
            String editText = watermarkEditText.getText().toString().trim();
            if (editText.isEmpty()) {
                Toast.makeText(requireContext(), "水印内容为空", Toast.LENGTH_SHORT).show();
                return;
            }
            // 设置水印内容
            textWatermark.setText(editText);
            // 按钮逻辑
            if (!isWatermarkEnabled) {
                startWatermarkService();
            } else {
                stopWatermarkService();
            }
        });
    }

    // 悬浮其他应用上方权限检查
    private boolean checkOverlayPermission() {
        if (!Settings.canDrawOverlays(requireContext())) {      // 如果没有悬浮窗权限
            Toast.makeText(requireContext(), "请开启悬浮窗权限", Toast.LENGTH_SHORT).show();
            // 跳转到设置页面
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:"+requireContext().getPackageName()));
            // 能否直接跳转到这个app的所有权限界面设置
            overlayPermissionLauncher.launch(intent);
            Toast.makeText(requireContext(), "请选择\"印\"并开启权限", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    // 请求悬浮其他应用上方权限回调函数
    private final ActivityResultLauncher<Intent> overlayPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Settings.canDrawOverlays(requireContext())) {
                    Toast.makeText(requireContext(), "悬浮窗权限已开启", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "未成功开启悬浮窗权限，请重试", Toast.LENGTH_SHORT).show();
                }
            });

    // 更新colorSelector界面显示的颜色
    private void updateColorDisplay() {
        int textColor = textWatermark.getTextColor();
        int colorWithAlpha = Color.argb(textWatermark.getTextAlpha(), Color.red(textColor), Color.green(textColor), Color.blue(textColor));
        colorSelector.setBackgroundColor(colorWithAlpha);
    }

    private void startWatermarkService() {
        if (textWatermark.getText().isEmpty()) {
            return;
        }
        Intent intent = new Intent(requireContext(), FullWatermarkService.class);
        intent.putExtra("text_watermark", textWatermark);
        ComponentName startFlag = requireContext().startService(intent);
        if (startFlag != null) {
            isWatermarkEnabled = true;
            showButton.setText("关闭水印");
            showButton.setSelected(true);
        }
    }

    private void stopWatermarkService() {
        boolean stopFlag = requireContext().stopService(new Intent(requireContext(), FullWatermarkService.class));
        if (stopFlag) {
            isWatermarkEnabled = false;
            showButton.setText("开启水印");
            showButton.setSelected(false);
        }
    }


    @Override
    public void onDestroy() {
        // 关闭水印
        stopWatermarkService();
        super.onDestroy();
    }
}