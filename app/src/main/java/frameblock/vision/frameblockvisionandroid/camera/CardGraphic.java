package frameblock.vision.frameblockvisionandroid.camera;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import frameblock.vision.camera.GraphicOverlay;
import frameblock.vision.geometric.Polygon;

public class CardGraphic extends GraphicOverlay.Graphic {
    private Polygon mPolygon;
    private Paint mPaint;

    public CardGraphic(GraphicOverlay overlay, Polygon polygon) {
        super(overlay);
        this.mPolygon = polygon;

        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5.0f);
    }

    @Override
    public void draw(Canvas canvas) {
        Point[] pts = mPolygon.getCornerPoints();
        float[] points = {
                translateX(pts[0].x), translateY(pts[0].y), translateX(pts[1].x), translateY(pts[1].y),
                translateX(pts[1].x), translateY(pts[1].y), translateX(pts[2].x), translateY(pts[2].y),
                translateX(pts[2].x), translateY(pts[2].y), translateX(pts[3].x), translateY(pts[3].y),
                translateX(pts[3].x), translateY(pts[3].y), translateX(pts[0].x), translateY(pts[0].y)
        };
        canvas.drawLines(points, mPaint);
    }
}
