package frameblock.vision.frameblockvisionandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;

import frameblock.vision.camera.CameraActivity;
import frameblock.vision.camera.CameraResult;

public class CameraSnapshotActivity extends AppCompatActivity {
    private final static int CAMERA_REQUEST = 1;

    private ImageView ivPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_snapshot);

        ivPhoto = findViewById(R.id.ivPhoto);

        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraSnapshotActivity.this, CameraActivity.class);

                CameraActivity.Setting setting = new CameraActivity.Setting();
                setting.message("hola mundo");
                setting.waitingMessage("esperaaaaa");
                setting.showProgress(true);
                setting.toggleButtonVisible(true);
                setting.snapshotButtonVisible(true);
                setting.cameraFacing(CameraActivity.CAMERA_FACING_BACK);
                setting.forceAutoFocus(true);
                setting.finderVisible(true);
                setting.finderShape(CameraActivity.FINDER_SHAPE_RECTANGLE);
                setting.finderAspectRatio(1.57f);

                intent.putExtra(CameraActivity.SETTING, setting);
                startActivityForResult(intent, CAMERA_REQUEST);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("requestCode", ""+requestCode);
        Log.d("resultCode", ""+resultCode);

        if(requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {

                CameraResult result = data.getParcelableExtra(CameraActivity.CAMERA_RESULT);
//                ByteArrayInputStream inputResult = new ByteArrayInputStream(result.getJpegImage());
                ByteArrayInputStream inputResult = new ByteArrayInputStream(result.getJpegImageCropped());
                Bitmap bitmap = BitmapFactory.decodeStream(inputResult);
                ivPhoto.setImageBitmap(bitmap);

            } else {
                Toast.makeText(this,"Resultado failed", Toast.LENGTH_LONG).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
