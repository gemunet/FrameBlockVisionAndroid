package frameblock.vision.image;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;

public class YuvUtil {
    static {
        System.loadLibrary("yuv_util");
    }

    /**
     * Rota una imagen 90 grados en sentido antihorario (90_counterclockwise)
     * @param data
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    public static native byte[] yuvPortraitToLandscape(byte[] data, int imageWidth, int imageHeight);

    /**
     * Retorna la distancia laplaciana para detectar borrosidad
     * @param bytes
     * @param width
     * @param height
     * @param roi
     * @return
     */
    public static native float nativeMotionBlur(byte[] bytes, int width, int height, Rect roi);

    /**
     * Retorna un score que define el nivel de enfoque
     * @param bytes
     * @param width
     * @param height
     * @param roi
     * @return
     */
    public static native float nativeFocusScore(byte[] bytes, int width, int height, Rect roi);


    public static native byte[] cropAndWarp(byte[] image, int imageWidth, int imageHeight, Point[] srcPoints, Point[] dstPoints, int dstWidth, int dstHeight);

    public static byte[] yuvToJpeg(byte[] data, int width, int height, Rect roi, int quality) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        yuvImage.compressToJpeg(roi, quality, out);
        return out.toByteArray();
    }

    public static YuvImage bitmapToNV21(Bitmap bmImage) {
        int width = bmImage.getWidth();
        int height = bmImage.getHeight();
        int[] intArray = new int[width * height];
        bmImage.getPixels(intArray, 0, width, 0, 0, width, height);
        byte[] yuv = new byte[width*height*3/2];
        YuvUtil.encodeYUV420SP(yuv, intArray, width, height);

        return new YuvImage(yuv, ImageFormat.NV21, width, height, null);
    }

    //NV21
    public static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        if(BuildConfig.DEBUG && !(width % 2 == 0 && height % 2 == 0)) {
            throw new AssertionError("width % 2 == 0 && height % 2 == 0");
        }

        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
                }

                index ++;
            }
        }
    }
}
