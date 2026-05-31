package com.beauty.match.camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.beauty.match.R;
import com.beauty.match.analysis.SkinToneActivity;
import com.beauty.match.utils.StorageUtils;
import com.beauty.match.camera.Camera2LegacyHelper;
import com.beauty.match.utils.ImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private AutoFitTextureView textureView;
    private ImageButton btnFlash, btnFlip, btnCapture;
    private ProgressBar progressCamera;

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private CaptureRequest previewRequest;
    private Size previewSize;
    private Size imageSize;
    private ImageReader imageReader;
    private File file;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private Semaphore cameraOpenCloseLock = new Semaphore(1);
    private SharedPreferences prefs;

    private boolean isFlashOn = false;
    private boolean isBackCamera = true;

    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            CameraActivity.this.cameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            CameraActivity.this.cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            CameraActivity.this.cameraDevice = null;
            finish();
        }
    };

    private int currentJpegOrientation = 90;
    private boolean currentIsFrontCamera = true;

    private final ImageReader.OnImageAvailableListener onImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            if (image == null) return;
            
            backgroundHandler.post(new ImageSaver(image, file, currentJpegOrientation, currentIsFrontCamera));
        }
    };


    private CameraCaptureSession.CaptureCallback captureCallback =
            new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
        }
    };

    private final CameraCaptureSession.StateCallback sessionStateCallback =
            new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            handleSessionConfigured(session);
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Toast.makeText(CameraActivity.this, "Camera configuration failed", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        prefs = getSharedPreferences("BeautyMatchPrefs", MODE_PRIVATE);
        isFlashOn = prefs.getBoolean("flash_enabled", true);

        textureView = findViewById(R.id.texture_view);
        btnFlash = findViewById(R.id.btn_flash);
        btnFlip = findViewById(R.id.btn_flip);
        btnCapture = findViewById(R.id.btn_capture);
        progressCamera = findViewById(R.id.progress_camera);

        // Проверяем наличие камер
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = manager.getCameraIdList();
            Log.d(TAG, "Available cameras: " + cameraIds.length);
            if (cameraIds.length == 0) {
                Toast.makeText(this, "Камеры не найдены", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Cannot access camera manager", e);
        }

        setupListeners();
        updateFlashIcon();
    }

    private void setupListeners() {
        btnFlash.setOnClickListener(v -> toggleFlash());
        btnFlip.setOnClickListener(v -> flipCamera());
        btnCapture.setOnClickListener(v -> takePicture());
    }

    private void toggleFlash() {
        // Фронтальная камера обычно не имеет вспышки
        if (!isBackCamera) {
            Toast.makeText(this, "Вспышка недоступна на фронтальной камере", Toast.LENGTH_SHORT).show();
            return;
        }

        isFlashOn = !isFlashOn;
        updateFlashIcon();
        prefs.edit().putBoolean("flash_enabled", isFlashOn).apply();

        if (captureSession != null) {
            try {
                previewRequestBuilder.set(CaptureRequest.FLASH_MODE,
                        isFlashOn ? CameraMetadata.FLASH_MODE_TORCH : CameraMetadata.FLASH_MODE_OFF);
                captureSession.setRepeatingRequest(previewRequestBuilder.build(), captureCallback,
                        backgroundHandler);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Flash toggle error", e);
            }
        }
    }

    private void updateFlashIcon() {
        btnFlash.setImageResource(isFlashOn ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
    }

    private void flipCamera() {
        Log.d(TAG, "Flipping camera, current: " + (isBackCamera ? "back" : "front"));
        
        closeCamera();
        
        // Небольшая задержка для освобождения ресурсов
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Log.e(TAG, "Sleep interrupted", e);
        }

        isBackCamera = !isBackCamera;
        isFlashOn = false; // Сбрасываем вспышку при переключении
        updateFlashIcon();

        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            Log.e(TAG, "stopBackgroundThread", e);
        }
    }

    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }

        setUpCameraOutputs(width, height);
        configureTransform(width, height);

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Cannot access camera", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (captureSession != null) {
                captureSession.close();
                captureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (imageReader != null) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null) {
                    if (isBackCamera && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        continue;
                    }
                    if (!isBackCamera && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        continue;
                    }
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    Log.w(TAG, "StreamConfigurationMap is null for camera " + cameraId);
                    continue;
                }

                // Для фронтальной камеры берём более консервативные размеры
                Size[] jpegSizes = map.getOutputSizes(ImageFormat.JPEG);
                if (jpegSizes == null || jpegSizes.length == 0) {
                    Log.w(TAG, "No JPEG sizes available for camera " + cameraId);
                    continue;
                }

                Size largest = Collections.max(Arrays.asList(jpegSizes), new CompareSizesByArea());
                imageSize = largest;
                Log.d(TAG, "Selected image size: " + imageSize);

                int displayRotation = getDisplayRotationCompat();
                int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                Log.d(TAG, "Camera: " + cameraId + ", sensorOrientation: " + sensorOrientation 
                        + ", displayRotation: " + displayRotation + ", facing: " + facing);

                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (sensorOrientation == 90 || sensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (sensorOrientation == 0 || sensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                }

                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = MAX_PREVIEW_WIDTH;
                int maxPreviewHeight = MAX_PREVIEW_HEIGHT;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = MAX_PREVIEW_HEIGHT;
                    maxPreviewHeight = MAX_PREVIEW_WIDTH;
                }

                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);
                Log.d(TAG, "Selected preview size: " + previewSize);

                int orientation = getResources().getConfiguration().orientation;
                if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
                } else {
                    textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
                }

                // Проверяем наличие вспышки только для задней камеры
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                boolean hasFlash = available != null && available;
                if (!isBackCamera) {
                    hasFlash = false; // Фронтальная камера — вспышка отключена
                }
                isFlashOn = hasFlash && isFlashOn;
                Log.d(TAG, "Has flash: " + hasFlash + ", flash enabled: " + isFlashOn);

                // Создаём ImageReader с проверкой
                try {
                    imageReader = ImageReader.newInstance(
                            Math.min(imageSize.getWidth(), 1920),
                            Math.min(imageSize.getHeight(), 1080),
                            ImageFormat.JPEG, 2);
                    imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to create ImageReader", e);
                    continue;
                }

                String fileName = "camera_" + System.currentTimeMillis() + ".jpg";
                file = new File(StorageUtils.getPhotosFolder(this), fileName);

                this.cameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "setUpCameraOutputs", e);
        }

        // Если не нашли подходящую камеру
        Log.e(TAG, "No suitable camera found");
        Toast.makeText(this, "Камера недоступна", Toast.LENGTH_LONG).show();
        finish();
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            Surface surface = new Surface(texture);

            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            List<Surface> surfaces = Arrays.asList(surface, imageReader.getSurface());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                List<OutputConfiguration> outputConfigs = new ArrayList<>();
                for (Surface s : surfaces) {
                    outputConfigs.add(new OutputConfiguration(s));
                }

                Executor executor = ContextCompat.getMainExecutor(this);

                cameraDevice.createCaptureSession(
                        new SessionConfiguration(
                                SessionConfiguration.SESSION_REGULAR,
                                outputConfigs,
                                executor,
                                sessionStateCallback));
            } else {
                Camera2LegacyHelper.createCaptureSession(cameraDevice, surfaces, 
                        sessionStateCallback, backgroundHandler);
            }

        } catch (CameraAccessException e) {
            Log.e(TAG, "createCameraPreviewSession", e);
        }
    }

    private void handleSessionConfigured(CameraCaptureSession session) {
        if (cameraDevice == null) {
            return;
        }

        captureSession = session;
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            previewRequestBuilder.set(CaptureRequest.FLASH_MODE,
                    isFlashOn ? CameraMetadata.FLASH_MODE_TORCH : CameraMetadata.FLASH_MODE_OFF);

            previewRequest = previewRequestBuilder.build();
            captureSession.setRepeatingRequest(previewRequest, captureCallback,
                    backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "handleSessionConfigured", e);
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (textureView == null || previewSize == null) {
            return;
        }
        
        int rotation = getDisplayRotationCompat();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        
        textureView.setTransform(matrix);
    }

    private void takePicture() {
        if (cameraDevice == null || !textureView.isAvailable() || previewSize == null || imageReader == null) {
            Log.e(TAG, "Camera not ready");
            Toast.makeText(this, "Камера не готова", Toast.LENGTH_SHORT).show();
            return;
        }

        progressCamera.setVisibility(View.VISIBLE);
        btnCapture.setEnabled(false);

        try {
            String fileName = "camera_" + System.currentTimeMillis() + ".jpg";
            file = new File(StorageUtils.getPhotosFolder(this), fileName);
            
            File photoDir = StorageUtils.getPhotosFolder(this);
            if (!photoDir.exists()) {
                photoDir.mkdirs();
            }
            
            Log.d(TAG, "Target file: " + file.getAbsolutePath());

            // === СОХРАНЯЕМ ПАРАМЕТРЫ ДЛЯ ImageSaver ===
            currentJpegOrientation = calculateJpegOrientation();
            currentIsFrontCamera = !isBackCamera;
            Log.d(TAG, "Capture params: orientation=" + currentJpegOrientation 
                    + ", frontCamera=" + currentIsFrontCamera);

            final CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());

            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            if (isBackCamera && isFlashOn) {
                captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
            } else {
                captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            }

            // Не устанавливаем JPEG_ORIENTATION — поворачиваем Bitmap вручную
            // captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ...);

            CameraCaptureSession.CaptureCallback captureListener =
                    new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    Log.d(TAG, "Capture completed");
                    
                    backgroundHandler.postDelayed(() -> {
                        runOnUiThread(() -> {
                            progressCamera.setVisibility(View.GONE);
                            btnCapture.setEnabled(true);

                            if (file != null && file.exists() && file.length() > 0) {
                                Intent intent = new Intent(CameraActivity.this, SkinToneActivity.class);
                                intent.putExtra("photo_path", file.getAbsolutePath());
                                startActivity(intent);
                            } else {
                                Log.e(TAG, "File missing: " + file);
                                Toast.makeText(CameraActivity.this, 
                                        "Ошибка сохранения фото", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }, 800); // Увеличили задержку для обработки Bitmap
                }

                @Override
                public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                            @NonNull CaptureRequest request,
                                            @NonNull CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                    Log.e(TAG, "Capture failed: " + failure.getReason());
                    
                    runOnUiThread(() -> {
                        progressCamera.setVisibility(View.GONE);
                        btnCapture.setEnabled(true);
                        Toast.makeText(CameraActivity.this, 
                                "Ошибка захвата", Toast.LENGTH_SHORT).show();
                    });
                }
            };

            captureSession.stopRepeating();
            captureSession.capture(captureBuilder.build(), captureListener, backgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "takePicture error", e);
            progressCamera.setVisibility(View.GONE);
            btnCapture.setEnabled(true);
            Toast.makeText(this, "Ошибка камеры", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Корректно рассчитывает ориентацию JPEG с учётом:
     * - sensorOrientation камеры
     * - поворота дисплея
     * - фронтальная/задняя камера
     */
    private int calculateJpegOrientation() {
        try {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            
            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            int displayRotation = getDisplayRotationCompat();
            
            int deviceRotationDegrees = 0;
            switch (displayRotation) {
                case Surface.ROTATION_0:   deviceRotationDegrees = 0;   break;
                case Surface.ROTATION_90:  deviceRotationDegrees = 90;  break;
                case Surface.ROTATION_180: deviceRotationDegrees = 180; break;
                case Surface.ROTATION_270: deviceRotationDegrees = 270; break;
            }
            
            int result;
            if (isBackCamera) {
                result = (sensorOrientation - deviceRotationDegrees + 360) % 360;
            } else {
                result = (sensorOrientation + deviceRotationDegrees) % 360;
                result = (360 - result) % 360;
            }
            
            // Для portrait режима обычно нужно 90 градусов
            if (deviceRotationDegrees == 0 && result == 0) {
                result = 90;
            }
            
            Log.d(TAG, "Orientation: sensor=" + sensorOrientation 
                    + ", device=" + deviceRotationDegrees 
                    + ", result=" + result);
            
            return result;
            
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to calculate orientation", e);
            return 90;
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Camera permission is needed", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (textureView.isAvailable()) {
                    openCamera(textureView.getWidth(), textureView.getHeight());
                }
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private int getDisplayRotationCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Display display = getDisplay();
            if (display != null) {
                return display.getRotation();
            }
            return Surface.ROTATION_0;
        }
        return Camera2LegacyHelper.getDisplayRotation(getWindowManager());
    }

    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight,
                                          Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<>();
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private static class ImageSaver implements Runnable {

        private final Image image;
        private final File file;
        private final int jpegOrientation;
        private final boolean isFrontCamera;

        ImageSaver(Image image, File file, int jpegOrientation, boolean isFrontCamera) {
            this.image = image;
            this.file = file;
            this.jpegOrientation = jpegOrientation;
            this.isFrontCamera = isFrontCamera;
        }

        @Override
        public void run() {
            Log.d(TAG, "ImageSaver started, orientation: " + jpegOrientation + ", front: " + isFrontCamera);
            
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            
            // Декодируем в Bitmap
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inMutable = true;
            Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode image");
                image.close();
                return;
            }
            
            Log.d(TAG, "Original bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            
            // Поворачиваем Bitmap физически
            Bitmap rotatedBitmap = bitmap;
            
            if (jpegOrientation != 0) {
                rotatedBitmap = ImageUtils.rotateBitmap(bitmap, jpegOrientation);
                if (rotatedBitmap != bitmap) {
                    bitmap.recycle();
                }
            }
            
            // Для фронтальной камеры зеркалим (как в зеркале)
            if (isFrontCamera) {
                Bitmap flippedBitmap = ImageUtils.flipBitmapHorizontal(rotatedBitmap);
                if (flippedBitmap != rotatedBitmap) {
                    rotatedBitmap.recycle();
                }
                rotatedBitmap = flippedBitmap;
            }
            
            Log.d(TAG, "Final bitmap: " + rotatedBitmap.getWidth() + "x" + rotatedBitmap.getHeight());
            
            // Сохраняем
            boolean success = ImageUtils.saveBitmap(rotatedBitmap, file);
            
            rotatedBitmap.recycle();
            image.close();
            
            Log.d(TAG, "Save result: " + success);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
