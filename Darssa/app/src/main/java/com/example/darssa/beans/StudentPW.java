package com.example.darssa.beans;

public class StudentPW {

    private String remarque;
    private String validation;
    private String title;

    byte[] bitmapByteArray;


    public StudentPW(String remarque, String validation, String title,byte[] bitmapByteArray) {
        this.remarque = remarque;
        this.validation = validation;
        this.title = title;
        this.bitmapByteArray=bitmapByteArray;
    }

    public String getRemarque() {
        return remarque;
    }

    public void setRemarque(String remarque) {
        this.remarque = remarque;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public byte[] getBitmapByteArray() {
        return bitmapByteArray;
    }

    public void setBitmapByteArray(byte[] bitmapByteArray) {
        this.bitmapByteArray = bitmapByteArray;
    }
}
