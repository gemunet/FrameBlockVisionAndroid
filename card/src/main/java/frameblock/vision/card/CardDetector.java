package frameblock.vision.card;

import android.graphics.Rect;

import java.io.OutputStream;

public class CardDetector {

    static {
        System.loadLibrary("card_detector");
    }

    /**
     * Busca una tarjeta dentro de una imagen en el ROI indicado, una vez encontrada retorna los
     * puntos del cuadrilatero y el status indicando si es una tarjeta o no.
     *
     * @param image Imagen yuv/jpeg/bitmap
     * @param width width imagen
     * @param height height imagen
     * @param roi area a escanear de la imagen
     * @param outEdges (opcional, puede ser NULL) OutputStream donde se retornara la imagen de edges en jpg
     * @return Card si encuentra tarjeta, de lo contrario NULL
     */
    public static Card detectLargestCard(byte[] image, int width, int height, Rect roi, OutputStream outEdges) {
        return detectLargestCard(image, width, height, roi, outEdges, 0.13f, 1.25f, 0.20f, 0.3f, 0.6f);
    }


    private static native Card detectLargestCard(byte[] image, int width, int height, Rect roi, OutputStream outEdge, float cannyThr1Factor, float cannyThr2Factor, float margin, float minLineLength, float maxGap);

}
