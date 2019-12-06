package frameblock.vision.frameblockvisionandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

import frameblock.vision.geometric.Polygon;
import frameblock.vision.geometric.QuadrilateralDetector;
import frameblock.vision.image.YuvUtil;

public class ImageWarpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_warp);

        InputStream input = getResources().openRawResource(R.raw.f1573153063665971r);
        Bitmap bmImage = BitmapFactory.decodeStream(input);
        YuvImage yuv = YuvUtil.bitmapToNV21(bmImage);
        Rect warpedSize = new Rect(0, 0, 118, 82);


        Polygon polygon = detectAndDrawQuad(yuv, bmImage);
        if(polygon != null) {

            Point[] dstPoints = {new Point(0,0), new Point(0,warpedSize.height()), new Point(warpedSize.width(),warpedSize.height()), new Point(warpedSize.width(),0)};

            byte[] warped = YuvUtil.cropAndWarp(yuv.getYuvData(),
                    yuv.getWidth(), yuv.getHeight(),
                    polygon.getCornerPoints(), dstPoints, warpedSize.width(), warpedSize.height());

            byte[] imageJpeg = YuvUtil.yuvToJpeg(warped, warpedSize.width(), warpedSize.height(), warpedSize, 100);
            Bitmap image = BitmapFactory.decodeByteArray(imageJpeg, 0, imageJpeg.length);
            ImageView ivWarp = findViewById(R.id.ivWarp);
            ivWarp.setImageBitmap(image);
        }
    }

    private Polygon detectAndDrawQuad(YuvImage yuv, Bitmap bmImage) {
        Rect roi = new Rect(110, 165, 110+118, 165+82); //118x82 1,43902439024 (300, 400, 3)
        Polygon polygon = QuadrilateralDetector.detectLargestQuad(yuv.getYuvData(), yuv.getWidth(), yuv.getHeight(), roi, null);
        Log.d("polygon", ""+polygon);

        if(polygon != null) {
            // dibuja los bordes de card en la imagen original
            Bitmap bmOverlay = bmImage.copy(bmImage.getConfig(), true);
            Point[] pts = polygon.getCornerPoints();
            if (pts.length > 0) {
                Canvas canvas = new Canvas(bmOverlay);
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                float[] points = {
                        pts[0].x, pts[0].y, pts[1].x, pts[1].y,
                        pts[1].x, pts[1].y, pts[2].x, pts[2].y,
                        pts[2].x, pts[2].y, pts[3].x, pts[3].y,
                        pts[3].x, pts[3].y, pts[0].x, pts[0].y
                };
                canvas.drawLines(points, paint);
            }
            ImageView ivImage = findViewById(R.id.ivImage);
            ivImage.setImageBitmap(bmOverlay);
        }

        return polygon;
    }
}
