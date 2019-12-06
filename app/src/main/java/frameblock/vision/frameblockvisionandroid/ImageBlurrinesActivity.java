package frameblock.vision.frameblockvisionandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.Frame;

import frameblock.vision.image.YuvUtil;

public class ImageBlurrinesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_blurrines);

        TextView tvHigh = findViewById(R.id.tvHigh);
        TextView tvBlur = findViewById(R.id.tvBlur);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bmHigh = BitmapFactory.decodeResource(getResources(), R.drawable.hd_vision, options);
        Bitmap bmBlur = BitmapFactory.decodeResource(getResources(), R.drawable.blurry_vision, options);
        Log.d("Bitmap", " w/h " + bmHigh.getWidth() + ", " + bmHigh.getHeight());

//        Frame fh = new Frame.Builder().setBitmap(bmHigh).build();
//        FIXME: getGrayscaleImageData es solo el canal gris, getFormat -1, desconocido
//        Log.d("yuv", ""+fh.getGrayscaleImageData().array().length + "f:" + fh.getMetadata().getFormat() + " w/h " + fh.getMetadata().getWidth() + ", " + fh.getMetadata().getHeight());


        YuvImage frameHigh = YuvUtil.bitmapToNV21(bmHigh);
        YuvImage frameBlur = YuvUtil.bitmapToNV21(bmBlur);

        Rect roiHigh = new Rect(450, 130, 600, 220);
        float focus1 = YuvUtil.nativeFocusScore(frameHigh.getYuvData(),
                frameHigh.getWidth(), frameHigh.getHeight(), roiHigh);
        float blur1 = YuvUtil.nativeMotionBlur(frameHigh.getYuvData(),
                frameHigh.getWidth(), frameHigh.getHeight(), roiHigh);
        tvHigh.setText(("focus: " + focus1 + "\n blur: " + blur1));

        Bitmap bmOverlay = bmHigh.copy(bmHigh.getConfig(), true);
        Canvas canvas = new Canvas(bmOverlay);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(roiHigh, paint);
        ((ImageView)findViewById(R.id.imageView)).setImageBitmap(bmOverlay);


        Rect roiBlur = new Rect(450, 130, 600, 220);
        float focus2 = YuvUtil.nativeFocusScore(frameBlur.getYuvData(),
                frameBlur.getWidth(), frameBlur.getHeight(), roiBlur);
        float blur2 = YuvUtil.nativeMotionBlur(frameBlur.getYuvData(),
                frameBlur.getWidth(), frameBlur.getHeight(), roiBlur);
        tvBlur.setText(("focus: " + focus2 + "\n blur: " + blur2));


    }
}
