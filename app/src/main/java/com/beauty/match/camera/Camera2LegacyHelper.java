package com.beauty.match.camera;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.os.Handler;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.List;

/**
 * Хелпер для изоляции deprecated Camera2 вызовов.
 * Все методы этого класса используют deprecated API и не проверяются lint.
 */
public final class Camera2LegacyHelper {

    private Camera2LegacyHelper() {}

    public static void createCaptureSession(CameraDevice device, List<Surface> surfaces,
                                            CameraCaptureSession.StateCallback callback,
                                            Handler handler) throws CameraAccessException {
        device.createCaptureSession(surfaces, callback, handler);
    }

    public static int getDisplayRotation(WindowManager windowManager) {
        return windowManager.getDefaultDisplay().getRotation();
    }

    public static Display getDefaultDisplay(WindowManager windowManager) {
        return windowManager.getDefaultDisplay();
    }
}
