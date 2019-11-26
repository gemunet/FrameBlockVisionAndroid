package frameblock.vision.camera;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;

import com.google.android.gms.vision.Detector;

import frameblock.vision.image.YuvUtil;

public class FocusedCameraSource extends CameraSource {

    private static final float MIN_FOCUS_SCORE = 6;

    private long mAutoFocusStartedAt;
    private long mAutoFocusCompletedAt;

    private FocusedCameraSource() {
    }

    @Override
    protected Camera.PreviewCallback getCameraPreviewCallback() {
        return new CameraPreviewCallback();
    }

    /**
     * Called when the camera has a new preview frame.
     */
    private class CameraPreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

            int centerX = getPreviewSize().getWidth()/3;
            int centerY = getPreviewSize().getHeight()/3;
            Rect roi = new Rect(centerX, centerY, centerX*2, centerY*2);
            float score = YuvUtil.nativeFocusScore(data, getPreviewSize().getWidth(), getPreviewSize().getHeight(), roi);

            if (score < MIN_FOCUS_SCORE) {
                if(!isAutoFocusing()) {
                    mAutoFocusStartedAt = System.currentTimeMillis();
                    camera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            mAutoFocusCompletedAt = System.currentTimeMillis();
                        }
                    });
                }
                camera.addCallbackBuffer(data);
            } else {
                mFrameProcessor.setNextFrame(data, camera);
            }
        }
    }

    /**
     * True if autoFocus is in progress
     */
    boolean isAutoFocusing() {
        return mAutoFocusCompletedAt < mAutoFocusStartedAt;
    }

    public static class Builder {
        private final Detector<?> mDetector;
        private FocusedCameraSource mCameraSource = new FocusedCameraSource();

        public Builder(Context context, Detector<?> detector) {
            if (context == null) {
                throw new IllegalArgumentException("No context supplied.");
            }
            if (detector == null) {
                throw new IllegalArgumentException("No detector supplied.");
            }

            mDetector = detector;
            mCameraSource.mContext = context;
        }

        /**
         * Sets the requested frame rate in frames per second.  If the exact requested value is not
         * not available, the best matching available value is selected.   Default: 30.
         */
        public FocusedCameraSource.Builder setRequestedFps(float fps) {
            if (fps <= 0) {
                throw new IllegalArgumentException("Invalid fps: " + fps);
            }
            mCameraSource.mRequestedFps = fps;
            return this;
        }

        /**
         * Sets the desired width and height of the camera frames in pixels.  If the exact desired
         * values are not available options, the best matching available options are selected.
         * Also, we try to select a preview size which corresponds to the aspect ratio of an
         * associated full picture size, if applicable.  Default: 1024x768.
         */
        public FocusedCameraSource.Builder setRequestedPreviewSize(int width, int height) {
            // Restrict the requested range to something within the realm of possibility.  The
            // choice of 1000000 is a bit arbitrary -- intended to be well beyond resolutions that
            // devices can support.  We bound this to avoid int overflow in the code later.
            final int MAX = 1000000;
            if ((width <= 0) || (width > MAX) || (height <= 0) || (height > MAX)) {
                throw new IllegalArgumentException("Invalid preview size: " + width + "x" + height);
            }
            mCameraSource.mRequestedPreviewWidth = width;
            mCameraSource.mRequestedPreviewHeight = height;
            return this;
        }

        /**
         * Creates an instance of the camera source.
         */
        public FocusedCameraSource build() {
            mCameraSource.mFocusMode = Camera.Parameters.FOCUS_MODE_AUTO;
            mCameraSource.mFrameProcessor = mCameraSource.new FrameProcessingRunnable(mDetector);
            return mCameraSource;
        }
    }
}
