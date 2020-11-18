package frameblock.vision.camera;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraResult implements Parcelable {
    private Uri mJpegUri;
    private Uri mJpegUriCropped;
    transient private byte[] mJpegImage;
    transient private byte[] mJpegImageCropped;
    private Rect roi;

    public CameraResult(Bitmap fullImage, Bitmap croppedImage) throws IOException {
        mJpegUri = createUriFromBitmap(fullImage);
        mJpegUriCropped = createUriFromBitmap(croppedImage);
    }

    protected CameraResult(Parcel in) {
        mJpegUri = in.readParcelable(Uri.class.getClassLoader());
        mJpegUriCropped = in.readParcelable(Uri.class.getClassLoader());
        roi = in.readParcelable(Rect.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mJpegUri, flags);
        dest.writeParcelable(mJpegUriCropped, flags);
        dest.writeParcelable(roi, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CameraResult> CREATOR = new Creator<CameraResult>() {
        @Override
        public CameraResult createFromParcel(Parcel in) {
            return new CameraResult(in);
        }

        @Override
        public CameraResult[] newArray(int size) {
            return new CameraResult[size];
        }
    };

    private Uri createUriFromBitmap(Bitmap image) throws IOException {
        File outputFile = File.createTempFile("image", "jpeg");
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
        image.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();

        return Uri.fromFile(outputFile);
    }

    public byte[] getJpegImage() {
        if(mJpegImage == null) {
            mJpegImage = readBinaryFile(mJpegUri.getPath());
        }
        return mJpegImage;
    }

    public byte[] getJpegImageCropped() {
        if(mJpegImageCropped == null) {
            mJpegImageCropped = readBinaryFile(mJpegUriCropped.getPath());
        }
        return mJpegImageCropped;
    }

    private byte[] readBinaryFile(String path) {
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
