package com.tobbetu.en4s;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;

public class Image {

	
	private byte[] imageByteArray;
	private Bitmap bmp;
	
	
	public byte[] getImageByteArray() {
		return imageByteArray;
	}
	public void setImageByteArray(byte[] imageByteArray) {
		this.imageByteArray = imageByteArray;
	}
	public Bitmap getBmp() {
		return bmp;
	}
	public void setBmp(Bitmap bmp) {
		this.bmp = bmp;
	}
	
	
	public byte[] bitmapToByteArray(Bitmap btmp) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		btmp.compress(Bitmap.CompressFormat.PNG, 100, out);

		return out.toByteArray();

	}
	
	
}
