package cn.impzy.watermarker;

import android.graphics.Color;

import java.io.Serializable;


public class TextWatermark implements Serializable{
    private String text;
    private int textSize;
    private int textColor;
    private int textAlpha;
    private int rotationAngle;
    private int spaceScale;
    private int timeType;      // off,datetime,time
    private String timeString;
    private String expireNum;
    private int expireUnit;


    // getter and setter
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextAlpha() {
        return textAlpha;
    }

    public void setTextAlpha(int textAlpha) {
        this.textAlpha = textAlpha;
    }

    public int getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(int rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public int getSpaceScale() {
        return spaceScale;
    }

    public void setSpaceScale(int spaceScale) {
        this.spaceScale = spaceScale;
    }

    public int getTimeType() {
        return timeType;
    }

    public void setTimeType(int timeType) {
        this.timeType = timeType;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    public String getExpireNum() {
        return expireNum;
    }

    public void setExpireNum(String expireNum) {
        this.expireNum = expireNum;
    }

    public int getExpireUnit() {
        return expireUnit;
    }

    public void setExpireUnit(int expireUnit) {
        this.expireUnit = expireUnit;
    }


    // 构造方法
    public TextWatermark() {
        this.text = "";
        this.textSize = 30;
        this.textColor = Color.BLACK;
        this.textAlpha = 60;
        this.rotationAngle = 315;
        this.spaceScale = 0;
        this.timeType = 0;      // off
        this.timeString = "";
        this.expireNum = "3";
        this.expireUnit = 2;    // day
    }
    public TextWatermark(String text, int textSize, int textColor, int textAlpha, int rotationAngle, int spaceScale, int timeType, String timeString, String expireNum, int expireUnit) {
        this.text = text;
        this.textSize = textSize;
        this.textColor = textColor;
        this.textAlpha = textAlpha;
        this.rotationAngle = rotationAngle;
        this.spaceScale = spaceScale;
        this.timeType = timeType;
        this.timeString = timeString;
        this.expireNum = expireNum;
        this.expireUnit = expireUnit;
    }


    // 静态工厂方法
    public static TextWatermark forAddWatermark() {
        TextWatermark watermark = new TextWatermark();
        watermark.setTextColor(Color.WHITE);
        watermark.setTextAlpha(128);
        return watermark;
    }
}