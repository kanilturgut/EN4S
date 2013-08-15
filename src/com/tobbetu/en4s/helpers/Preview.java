package com.tobbetu.en4s.helpers;

import java.io.IOException;

import com.tobbetu.en4s.Utils;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {

	SurfaceHolder mHolder;
	public Camera camera;
	public static int pictureWidth, pictureHeight;

	@SuppressWarnings("deprecation")
	public Preview(Context context) {
		super(context);
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		int[] sizes = Utils.deviceSupportedScreenSize();
		pictureWidth = sizes[0];
		pictureHeight = sizes[1];

		Log.i("Preview", "Constructor");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		camera.setDisplayOrientation(90);
		camera.startPreview();
		Log.i("Preview", "surfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		camera = Camera.open();

		try {
			camera.setPreviewDisplay(holder);
			Camera.Parameters parameters = camera.getParameters();
			// List<Size> sizes=parameters.getSupportedPictureSizes();
			parameters.setPictureSize(pictureWidth, pictureHeight);
			camera.setParameters(parameters);
			camera.startPreview();

			Log.i("Preview", "surfaceCreated");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		camera.stopPreview();
		camera.release();
		camera = null;

		Log.i("Preview", "surfaceDestroyed");
	}

}
