package frameblock.vision.frameblockvisionandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCameraTools = findViewById(R.id.btnCameraTools);
        btnCameraTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CameraToolActivity.class);
                startActivity(intent);
            }
        });

        Button btnCardTools = findViewById(R.id.btnCardTools);
        btnCardTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CardToolActivity.class);
                startActivity(intent);
            }
        });

        Button btnImageTools = findViewById(R.id.btnImageTools);
        btnImageTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ImageToolActivity.class);
                startActivity(intent);
            }
        });
    }
}
