package cn.impzy.watermark;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ColorPickerAlertActivity extends AppCompatActivity {
    private int selectedColor = Color.BLACK; // 默认颜色
    private int alpha = 255; // 默认透明度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Button colorPickerButton = findViewById(R.id.colorPickerButton);
        colorPickerButton.setOnClickListener(v -> showColorPickerDialog());

        // 显示颜色的 TextView
        final TextView colorDisplay = findViewById(R.id.colorDisplay);
        colorDisplay.setBackgroundColor(selectedColor);

        // 透明度调节 SeekBar
        SeekBar alphaSeekBar = findViewById(R.id.alphaSeekBar);
        alphaSeekBar.setMax(255);
        alphaSeekBar.setProgress(alpha);
        alphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                alpha = progress;
                updateColorDisplay(colorDisplay);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void showColorPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择颜色");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.color_picker_dialog, null);
        GridLayout colorGrid = dialogView.findViewById(R.id.colorGrid);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // 添加颜色方块
        for (int i = 0; i < colorGrid.getChildCount(); i++) {
            View colorView = colorGrid.getChildAt(i);
            colorView.setOnClickListener(v -> {
                selectedColor = ((ColorDrawable) colorView.getBackground()).getColor();
                updateColorDisplay(findViewById(R.id.colorDisplay));
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss(); // 关闭弹窗
                }
            });
        }
        dialog.show();
    }

    private void updateColorDisplay(TextView colorDisplay) {
        int colorWithAlpha = Color.argb(alpha, Color.red(selectedColor), Color.green(selectedColor), Color.blue(selectedColor));
        colorDisplay.setBackgroundColor(colorWithAlpha);
    }
}