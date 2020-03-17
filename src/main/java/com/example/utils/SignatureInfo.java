package com.example.utils;

import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;

public class SignatureInfo {
    private String reason; //理由
    private String location;//位置
    private Image image;//图章路径
    private String fieldName;//表单域名称
    private Rectangle visibleSignature;//四个参数的分别是，图章左下角x，图章左下角y，图章右上角x，图章右上角y
    private int page;//签名页

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Rectangle getVisibleSignature() {
        return visibleSignature;
    }

    public void setVisibleSignature(Rectangle visibleSignature) {
        this.visibleSignature = visibleSignature;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
