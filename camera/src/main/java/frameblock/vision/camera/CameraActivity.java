package frameblock.vision.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

import java.io.IOException;

public class CameraActivity extends AppCompatActivity {
    public static final String CAMERA_RESULT = "CAMERA_RESULT";

    public static final String CAMERA_FACING_FRONT = "user";
    public static final String CAMERA_FACING_BACK = "environment";

    public static final int FINDER_SHAPE_RECTANGLE = 0;
    public static final int FINDER_SHAPE_CIRCLE = 1;

    public static final String SETTING = "Setting";
    private Setting setting;

    private CameraSourcePreview mPreview;
    private FinderGraphicOverlay mGraphicOverlay;

    private CameraSource mCameraSource = null;
    private int mCameraFacing = CameraSource.CAMERA_FACING_FRONT;

    private ProgressBar mPbCapture;
    private TextView mTvWaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initialize();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initialize() {
        Bundle bundle = getIntent().getExtras();
        setting = bundle.getParcelable(SETTING);
        if(setting == null) {
            setting = new Setting();
        }

        mCameraFacing = setting.cameraFacing.equals(CAMERA_FACING_FRONT) ? CameraSource.CAMERA_FACING_FRONT : CameraSource.CAMERA_FACING_BACK;

        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.graphicOverlay);
        mGraphicOverlay.setVisibility(setting.finderVisible ? View.VISIBLE : View.GONE);
        mGraphicOverlay.setFinderShape(setting.finderShape);
        mGraphicOverlay.setAspectRatio(setting.finderAspectRatio);


        mPbCapture = findViewById(R.id.pbCapture);
        mTvWaiting = findViewById(R.id.tvWaiting);
        mTvWaiting.setText(setting.waitingMessage);

        TextView tvMessage = findViewById(R.id.tvMessage);
        if(tvMessage != null) {
            tvMessage.setText(setting.message);
        }

        ImageButton btnToggle = findViewById(R.id.btnToggle);
        if(btnToggle != null) {
            if (!setting.toggleButtonVisible) {
                btnToggle.setVisibility(View.GONE);
            }

            btnToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleCamera();
                }
            });
        }


        ImageView btnSnapshot = findViewById(R.id.btnSnapshot);
        if(btnSnapshot != null) {
            if (!setting.snapshotButtonVisible) {
                btnSnapshot.setVisibility(View.GONE);
            }

            btnSnapshot.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            Animation pushDown = AnimationUtils.loadAnimation(getBaseContext(), R.anim.push_down_rotate);
                            v.startAnimation(pushDown);
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            Animation pushDown = AnimationUtils.loadAnimation(getBaseContext(), R.anim.push_up_rotate);
                            v.startAnimation(pushDown);
                            break;
                        }
                    }
                    return false;
                }
            });

            btnSnapshot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeSnapshot(setting.forceAutoFocus);
                }
            });
        }

        startPreview();
    }

    public void takeSnapshot(boolean autofocus) {
        if(setting.showProgress) {
            if(mPbCapture != null) {
                mPbCapture.setVisibility(View.VISIBLE);
            }
            if(mTvWaiting != null) {
                mTvWaiting.setVisibility(View.VISIBLE);
            }
        }

        if(autofocus) {
            mCameraSource.autoFocus(new CameraSource.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success) {
                    System.out.println("onAutoFocus success: " + success);
                    takeSnapshot();
                    mCameraSource.cancelAutoFocus();
                }
            });
        } else {
            takeSnapshot();
        }
    }

    private void takeSnapshot(){
        mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data) {
                Bitmap fullImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                float szFactor = fullImage.getWidth() / (float)mCameraSource.getPreviewSize().getWidth();

                Matrix m = new Matrix();
                Log.d("ROTATION", ""+mCameraSource.getDisplayOrientation());
                if(mCameraSource.getCameraFacing() == CameraSource.CAMERA_FACING_FRONT) {
                    m.setRotate(-mCameraSource.getDisplayOrientation());
                    m.postScale(-1, 1);
                } else {
                    m.setRotate(mCameraSource.getDisplayOrientation());
                }


                // Full Image
                fullImage = Bitmap.createBitmap(fullImage, 0, 0, fullImage.getWidth(), fullImage.getHeight(), m, true);

                // Cropped Image
                Rect roi = mGraphicOverlay.getScaledFinder();
                Bitmap croppedImage = Bitmap.createBitmap(fullImage, (int) (roi.left * szFactor), (int) (roi.top * szFactor),
                        (int) (roi.width() * szFactor), (int) (roi.height() * szFactor));

                try {
                    final Intent resultData = new Intent();
                    resultData.putExtra(CAMERA_RESULT, new CameraResult(fullImage, croppedImage));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           success(RESULT_OK, resultData);
                        }
                  });

                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

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

    private void startPreview() {
        mCameraSource = createCameraSource(mCameraFacing);
        startCameraSource();
    }

    public void toggleCamera() {
        stopCameraSource();

        if(mCameraFacing == CameraSource.CAMERA_FACING_FRONT) {
            mCameraFacing = CameraSource.CAMERA_FACING_BACK;
        } else {
            mCameraFacing = CameraSource.CAMERA_FACING_FRONT;
        }

        startPreview();
    }


    protected CameraSource createCameraSource(int cameraFacing) {
        Context context = getApplicationContext();

        Detector<?> detector = createDetector();

        CameraSource cameraSource = new CameraSource.Builder(context, detector) //detector
                //.setRequestedPreviewSize(1024, 768)
                .setRequestedPreviewSize(1600, 1024) //-> resuelve 1280, 720 A6 y 1080x1440 S5
                //.setRequestedPreviewSize(1280, 720) //default 1024x768 1280x720, 320x240
                .setRequestedFps(15.0f) //60.0f 15.0f
                .setFacing(cameraFacing)
                .build();

        return cameraSource;
    }

    protected Detector<?> createDetector() {
        CustomDetector detector = new CustomDetector();
        CustomProcessor mProcessor = new CustomProcessor();
        detector.setProcessor(mProcessor);
        return detector;
    }

    protected void success(final int result, final Intent resultData) {
        setResult(result, resultData);
        finish();
    }

    public static class CustomDetector extends Detector {
        @Override
        public SparseArray detect(Frame frame) {
            return null;
        }
    }

    public static class CustomProcessor implements Detector.Processor {
        @Override
        public void release() {

        }

        @Override
        public void receiveDetections(Detector.Detections detections) {

        }
    }

    public static final class Setting implements Parcelable {
        private String message = null;
        private String waitingMessage = "No mueva la cámara";
        private boolean showProgress = true;
        private boolean toggleButtonVisible = true;
        private boolean snapshotButtonVisible = true;
        private String cameraFacing = CAMERA_FACING_FRONT;
        private boolean forceAutoFocus = true;

        private boolean finderVisible = true;
        private int finderShape = FINDER_SHAPE_CIRCLE;
        private float finderAspectRatio = 1;

        public Setting() {};


        protected Setting(Parcel in) {
            message = in.readString();
            waitingMessage = in.readString();
            showProgress = in.readByte() != 0;
            toggleButtonVisible = in.readByte() != 0;
            snapshotButtonVisible = in.readByte() != 0;
            cameraFacing = in.readString();
            forceAutoFocus = in.readByte() != 0;
            finderVisible = in.readByte() != 0;
            finderShape = in.readInt();
            finderAspectRatio = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(message);
            dest.writeString(waitingMessage);
            dest.writeByte((byte) (showProgress ? 1 : 0));
            dest.writeByte((byte) (toggleButtonVisible ? 1 : 0));
            dest.writeByte((byte) (snapshotButtonVisible ? 1 : 0));
            dest.writeString(cameraFacing);
            dest.writeByte((byte) (forceAutoFocus ? 1 : 0));
            dest.writeByte((byte) (finderVisible ? 1 : 0));
            dest.writeInt(finderShape);
            dest.writeFloat(finderAspectRatio);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Setting> CREATOR = new Creator<Setting>() {
            @Override
            public Setting createFromParcel(Parcel in) {
                return new Setting(in);
            }

            @Override
            public Setting[] newArray(int size) {
                return new Setting[size];
            }
        };

        public Setting message(String message) {
            this.message = message;
            return this;
        }

        public Setting waitingMessage(String waitingMessage) {
            this.waitingMessage = waitingMessage;
            return this;
        }

        public Setting showProgress(boolean showProgress) {
            this.showProgress = showProgress;
            return this;
        }

        public Setting toggleButtonVisible(boolean toggleButtonVisible) {
            this.toggleButtonVisible = toggleButtonVisible;
            return this;
        }

        public Setting snapshotButtonVisible(boolean snapshotButtonVisible) {
            this.snapshotButtonVisible = snapshotButtonVisible;
            return this;
        }

        public Setting cameraFacing(String cameraFacing) {
            this.cameraFacing = cameraFacing;
            return this;
        }

        public Setting forceAutoFocus(boolean forceAutoFocus) {
            this.forceAutoFocus = forceAutoFocus;
            return this;
        }

        public Setting finderVisible(boolean finderVisible) {
            this.finderVisible = finderVisible;
            return this;
        }

        public Setting finderShape(int finderShape) {
            this.finderShape = finderShape;
            return this;
        }

        public Setting finderAspectRatio(float finderAspectRatio) {
            this.finderAspectRatio = finderAspectRatio;
            return this;
        }
    }
}
