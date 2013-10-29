package com.tobbetu.en4s.helpers;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.bugsense.trace.BugSenseHandler;
import com.tobbetu.en4s.LauncherActivity;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {

    SurfaceHolder mHolder;
    public Camera camera = null;
    public static int pictureWidth;
    public static int pictureHeight;

    @SuppressWarnings("deprecation")
    public Preview(Context context) {
        super(context);

        mHolder = getHolder();
        mHolder.addCallback(this);

        // This constant was deprecated in API level 11. this is ignored, this
        // value is set automatically when needed.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        pictureWidth = LauncherActivity.firstTimeControlPref.getInt(
                "deviceWidth", 0);
        pictureHeight = LauncherActivity.firstTimeControlPref.getInt(
                "deviceHeight", 0);

        Log.i("Preview", "Constructor");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {

        try {
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();

            BugSenseHandler.sendExceptionMessage(
                    "on Preview --> surfaceChanged", "camera.startPreview();",
                    e);
        }

        Log.i("Preview", "surfaceChanged");
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {

        if (camera != null)
            camera.release();

        try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();

            BugSenseHandler.sendExceptionMessage(
                    "on Preview --> surfaceCreated", "Camera.open() failed", e);
            // calismayi bitirmeli yoksa uygulama crash eder.
            return;
        }

        camera.setDisplayOrientation(90);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureSize(pictureWidth, pictureHeight);
        camera.setParameters(parameters);
        camera.startPreview();

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();

            BugSenseHandler.sendExceptionMessage(
                    "on Preview --> surfaceCreated",
                    "camera.setPreviewDisplay", e);
        }

        Log.i("Preview", "surfaceCreated");

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
        Log.i("Preview", "surfaceDestroyed");
    }

}
