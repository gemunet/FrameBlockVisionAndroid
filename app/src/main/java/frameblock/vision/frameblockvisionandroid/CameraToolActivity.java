package frameblock.vision.frameblockvisionandroid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.Frame;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import frameblock.vision.camera.CameraSource;
import frameblock.vision.camera.CameraSourcePreview;
import frameblock.vision.camera.FinderGraphicOverlay;
import frameblock.vision.camera.PreviewUtil;
import frameblock.vision.frameblockvisionandroid.camera.CardDetector;
import frameblock.vision.frameblockvisionandroid.camera.CardProcessor;
import frameblock.vision.geometric.Polygon;
import frameblock.vision.geometric.QuadrilateralDetector;
import frameblock.vision.image.YuvUtil;

public class CameraToolActivity extends AppCompatActivity {

    private CameraSourcePreview mPreview;
    private FinderGraphicOverlay mGraphicOverlay;

    protected CameraSource mCameraSource = null;

    CardDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        solicitarPermisos();

        setContentView(R.layout.activity_camera_tool);

        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.graphicOverlay);

        ImageView iv = findViewById(R.id.ivEdge);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraSource.autoFocus(new CameraSource.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success) {
                        System.out.println("onAutoFocus success: " + success);
//                        mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
//                            @Override
//                            public void onPictureTaken(byte[] data) {
//                                saveOnDisk("/jpegimage.jpg", data);
//                            }
//                        });
                        mCameraSource.cancelAutoFocus();
                    }
                });
            }
        });

        createCameraSource();

        Button btnSaveImage = findViewById(R.id.btnSaveImage);
        btnSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Frame frame = detector.getFrame();
                byte[] yuvImage = frame.getGrayscaleImageData().array();
                Rect yuvRect = new Rect(0, 0, frame.getMetadata().getWidth(), frame.getMetadata().getHeight());

                //Rect roi = new Rect(0, 0, frame.getMetadata().getWidth(), frame.getMetadata().getHeight());
                Rect roi = mGraphicOverlay.getScaledFinder();
                if(PreviewUtil.isPortraitMode(getApplicationContext())) {
                    yuvImage = YuvUtil.yuvPortraitToLandscape(yuvImage, yuvRect.width(), yuvRect.height());
                    yuvRect = new Rect(0, 0, yuvRect.height(), yuvRect.width());
                }
                saveOnDisk("/im" + yuvRect.width() + "x" + yuvRect.height() + ".yuv", yuvImage);

                ByteArrayOutputStream edges = new ByteArrayOutputStream();
                Polygon polygon = QuadrilateralDetector.detectLargestQuad(yuvImage, yuvRect.width(), yuvRect.height(), roi, edges);
                Log.d("polygon", ""+polygon);
                Log.d("edges", ""+edges.size());

                // dibuja la imagen roi transformada a bordes
                if(edges.size() > 0) {
                    Bitmap bmEdge = BitmapFactory.decodeByteArray(edges.toByteArray(), 0, edges.size());
                    ImageView ivEdge = findViewById(R.id.ivEdge);
                    ivEdge.setImageBitmap(bmEdge);
                }

                try {
                    saveYuvToJpeg(yuvImage, yuvRect.width(), yuvRect.height(),
                            100, roi, "/image.jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void saveYuvToJpeg(byte[] yuvImage, int width, int height, int jpegQuality, Rect crop, String pathname) throws IOException {
        YuvImage yuv = new YuvImage(yuvImage, ImageFormat.NV21, width, height, null);
        //File outputFile = File.createTempFile("image", "jpeg");
        String path = Environment.getExternalStorageDirectory().getPath() + pathname;
        File outputFile = new File(path);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
        yuv.compressToJpeg(crop, jpegQuality, out);
        out.flush();
        out.close();
        Log.w("GRABADO", path);
    }

    //** start logica solo para habilitar el permiso para la demo **
    private boolean solicitarPermisos() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            return false;
        }
        else {
            return true;
        }
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        finish();
        startActivity(getIntent());
    }
    //** end

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPreview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCameraSource();
    }

    private void stopCameraSource() {
        if(mPreview != null) {
            mPreview.stop();
        }

        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    private void startCameraSource() {
        if (mCameraSource != null) {
            try {
                PreviewUtil.checkPlayServices(this);
                mPreview.start(mCameraSource, mGraphicOverlay);
            }catch (IOException e) {
                Log.e("startCameraSource", "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * Creates the face detector and the camera.
     */
    private void createCameraSource() {
        Context context = getApplicationContext();

        //Detector<Foo> detector = new CardDetector(mGraphicOverlay);
        detector = new frameblock.vision.frameblockvisionandroid.camera.CardDetector(this, mGraphicOverlay);

        CardProcessor mProcessor = new CardProcessor(mGraphicOverlay);
        detector.setProcessor(mProcessor);

//        mCameraSource = new FocusedCameraSource.Builder(context, detector) //detector
//                //.setRequestedPreviewSize(1024, 768)
//                .setRequestedPreviewSize(1600, 1024) //-> resuelve 1280, 720 A6 y 1080x1440 S5
//                //.setRequestedPreviewSize(1280, 720) //default 1024x768 1280x720, 320x240
//                .setRequestedFps(15.0f) //60.0f 15.0f
//                .build();

        mCameraSource = new CameraSource.Builder(context, detector) //detector
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                //.setRequestedPreviewSize(1024, 768)
                .setRequestedPreviewSize(1600, 1024) //-> resuelve 1280, 720 A6 y 1080x1440 S5
                //.setRequestedPreviewSize(1280, 720) //default 1024x768 1280x720, 320x240
                .setRequestedFps(15.0f) //60.0f 15.0f
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                //.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                .build();
    }

    public void saveOnDisk(String pathname, byte[] data) {
        try {
            String path = Environment.getExternalStorageDirectory().getPath() + pathname;

            File file = new File(path);
            FileOutputStream output = new FileOutputStream(file);
            output.write(data);
            output.close();
        } catch (Exception e) {
            Log.e("Exception", "saveOnDisk error.", e);
        }
    }
}
