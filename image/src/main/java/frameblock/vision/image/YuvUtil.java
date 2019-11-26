package frameblock.vision.image;

import android.graphics.Rect;

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
}
