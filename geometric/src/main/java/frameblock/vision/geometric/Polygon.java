package frameblock.vision.geometric;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * representa una pligono encontrada en una imagen
 */
public class Polygon {
    /**
     * retorna 4 puntos de la tarjeta en sentido antihorario ((x,y),(x,y),(x,y),(x,y))
     */
    private Point[] cornerPoints;

    /**
     * relación de diferencia entre las dos lineas verticales
     * abs(1-left_h/(float)right_h)
     */
    private float verticalDiff;

    /**
     * relación de diferencia entre las dos lineas horizontales
     * abs(1-top_w/(float)bottom_w);
     */
    private float horizontalDiff;

    /**
     * Ratio de aspecto de la tarjeta
     * (top_w/(float)left_h);
     */
    private float aspectRatio;

    /**
     * BoundingBox
     */
    private Rect boundingBox ;

    public Polygon(Point[] cornerPoints, Rect boundingBox, float verticalDiff, float horizontalDiff, float aspectRatio) {
        this.cornerPoints = cornerPoints;
        this.boundingBox = boundingBox;
        this.verticalDiff = verticalDiff;
        this.horizontalDiff = horizontalDiff;
        this.aspectRatio = aspectRatio;
    }

    public float getAspectRatioDiff(float expectedRatio) {
        return Math.abs(expectedRatio-aspectRatio);
    }

    public boolean checkParallel(float maxDiff) {
        return (verticalDiff <= maxDiff || horizontalDiff <= maxDiff);
    }

    public boolean checkAspectRatio(float expectedRatio, float maxDiff) {
        return (getAspectRatioDiff(expectedRatio) <= maxDiff);
    }

    public Point[] getCornerPoints() {
        return cornerPoints;
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public float getVerticalDiff() {
        return verticalDiff;
    }

    public float getHorizontalDiff() {
        return horizontalDiff;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }
}
