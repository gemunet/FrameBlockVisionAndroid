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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import frameblock.vision.geometric.Polygon;
import frameblock.vision.geometric.QuadrilateralDetector;
import frameblock.vision.image.YuvUtil;

public class QuadToolActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quad_tool);

        ImageView ivImage = findViewById(R.id.ivImage);
        ImageView ivEdge = findViewById(R.id.ivEdge);

        // frame_cedulaarea1.png - Card[status: 0, bbox: [(3, 9), (4, 76), (111, 72), (107, 4)]],
        // canny 49.435510541546094 15.325008267879289 111.22989871847871, 9676

        // f1573153063665971.jpg - Card[status: 0, bbox: [(4, 8), (6, 77), (114, 71), (111, 3)]]
        // canny 129.341325616646 40.09581094116026 291.0179826374535, 9676

        InputStream input = getResources().openRawResource(R.raw.f1573153063665971r);
        Bitmap bmImage = BitmapFactory.decodeStream(input);
        YuvImage yuv = YuvUtil.bitmapToNV21(bmImage);

        Rect roi = new Rect(110, 165, 110+118, 165+82); //118x82 1,43902439024 (300, 400, 3)

        ByteArrayOutputStream edges = new ByteArrayOutputStream();
        Polygon quad = QuadrilateralDetector.detectLargestQuad(yuv.getYuvData(), yuv.getWidth(), yuv.getHeight(), roi, edges);

        Log.d("quad", ""+quad);
        Log.d("edges", ""+edges.size());

        if(quad != null) {
            // info
            float expectedRatio = 1.59f;
            float ratioMaxDiff = 0.05f;
            float parallelMaxDiff = 0.05f;

            TextView tvInfo = findViewById(R.id.tvInfo);
            tvInfo.setText(("expectedRatio: " + expectedRatio + ", Ratio: " + quad.getAspectRatio() +
                    "\nratioDiff: " + quad.getAspectRatioDiff(expectedRatio) +
                    "\nVerticalDiff: " + quad.getVerticalDiff() +
                    "\nHorizontalDiff: " + quad.getHorizontalDiff()));
            TextView tvCheck = findViewById(R.id.tvCheck);
            tvCheck.setText(("IsValid: " + (quad.checkAspectRatio(expectedRatio, ratioMaxDiff) && quad.checkParallel(parallelMaxDiff))));


            // dibuja los bordes de card en la imagen original
            Bitmap bmOverlay = bmImage.copy(bmImage.getConfig(), true);
            Point[] pts = quad.getCornerPoints();
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
            ivImage.setImageBitmap(bmOverlay);
        }

        // dibuja la imagen roi transformada a bordes
        if(edges.size() > 0) {
            Bitmap bmEdge = BitmapFactory.decodeByteArray(edges.toByteArray(), 0, edges.size());
            ivEdge.setImageBitmap(bmEdge);
        }
    }
}
