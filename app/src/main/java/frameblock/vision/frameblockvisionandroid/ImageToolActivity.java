package frameblock.vision.frameblockvisionandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.Frame;

import frameblock.vision.image.YuvUtil;

public class ImageToolActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_tool);

        TextView tvHigh = findViewById(R.id.tvHigh);
        TextView tvBlur = findViewById(R.id.tvBlur);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bmHigh = BitmapFactory.decodeResource(getResources(), R.drawable.hd_vision, options);
        Bitmap bmBlur = BitmapFactory.decodeResource(getResources(), R.drawable.blurry_vision, options);
        Log.d("Bitmap", " w/h " + bmHigh.getWidth() + ", " + bmHigh.getHeight());

        Frame frameHigh = new Frame.Builder().setBitmap(bmHigh).build();
        Log.d("yuv", ""+frameHigh.getGrayscaleImageData().array().length + " w/h " + frameHigh.getMetadata().getWidth() + ", " + frameHigh.getMetadata().getHeight());
        Frame frameBlur = new Frame.Builder().setBitmap(bmBlur).build();



        Rect roiHigh = new Rect(450, 130, 600, 220);
        float focus1 = YuvUtil.nativeFocusScore(frameHigh.getGrayscaleImageData().array(),
                frameHigh.getMetadata().getWidth(), frameHigh.getMetadata().getHeight(), roiHigh);
        float blur1 = YuvUtil.nativeMotionBlur(frameHigh.getGrayscaleImageData().array(),
                frameHigh.getMetadata().getWidth(), frameHigh.getMetadata().getHeight(), roiHigh);
        tvHigh.setText(("focus: " + focus1 + "\n blur: " + blur1));

        Bitmap bmOverlay = bmHigh.copy(bmHigh.getConfig(), true);
        Canvas canvas = new Canvas(bmOverlay);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(roiHigh, paint);
        ((ImageView)findViewById(R.id.imageView)).setImageBitmap(bmOverlay);


        Rect roiBlur = new Rect(450, 130, 600, 220);
        float focus2 = YuvUtil.nativeFocusScore(frameBlur.getGrayscaleImageData().array(),
                frameHigh.getMetadata().getWidth(), frameBlur.getMetadata().getHeight(), roiBlur);
        float blur2 = YuvUtil.nativeMotionBlur(frameBlur.getGrayscaleImageData().array(),
                frameHigh.getMetadata().getWidth(), frameBlur.getMetadata().getHeight(), roiBlur);
        tvBlur.setText(("focus: " + focus2 + "\n blur: " + blur2));


    }
}
