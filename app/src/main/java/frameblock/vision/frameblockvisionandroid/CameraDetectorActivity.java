package frameblock.vision.frameblockvisionandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import frameblock.vision.camera.CameraActivity;
import frameblock.vision.camera.CameraResult;

public class CameraDetectorActivity extends CameraActivity {
    public static final String DETECTOR_RESULT_RUT = "DETECTOR_RESULT_RUT";
    public static final String DETECTOR_RESULT_SERIAL = "DETECTOR_RESULT_SERIAL";

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_camera_detector);
//    }


    @Override
    protected Detector<?> createDetector() {

        BarcodeDetector detector = new BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.QR_CODE | Barcode.PDF417)
            .build();

        CustomProcessor mProcessor = new CustomProcessor(new CustomProcessor.BarcodeListener() {
            @Override
            public void onBarcodeCapture(Map<String, String> mapData) {
                Log.d("mapData", ""+mapData);
                final Intent resultData = new Intent();
                resultData.putExtra(DETECTOR_RESULT_RUT, mapData.get("rut"));
                resultData.putExtra(DETECTOR_RESULT_SERIAL, mapData.get("serial"));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setResult(RESULT_OK, resultData);
                        finish();
                    }
                });
            }
        });
        detector.setProcessor(mProcessor);
        return detector;
    }

    public static class CustomProcessor implements Detector.Processor<Barcode> {
        private BarcodeListener mListener;
        public interface BarcodeListener {
            void onBarcodeCapture(Map<String, String> mapData);
        }

        public CustomProcessor(BarcodeListener listener) {
            mListener = listener;
        }

        @Override
        public void release() {

        }

        @Override
        public void receiveDetections(Detector.Detections<Barcode> detections) {

            try {

                SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() > 0) {
                    Map<String, String> mapData = new HashMap<>();
                    Barcode barcode = barcodes.valueAt(0);
                    Log.d("barcode.rawValue", barcode.rawValue);

                    if (barcode.format == Barcode.QR_CODE) { //QR
                        //https://portal.sidiv.registrocivil.cl/docstatus?RUN=15988779-0&type=CEDULA&serial=110515489&mrz=110515489485092882509286
                        Uri data = Uri.parse("?" + barcode.rawValue);
                        mapData.put("serial", data.getQueryParameter("serial"));
                        mapData.put("rut", data.getQueryParameter("RUN").toUpperCase());
                    } else { //PDF417
                        String rut = (barcode.rawValue.substring(0, 8) + "-" + barcode.rawValue.substring(8, 9)).toUpperCase();

                        Matcher m = Pattern.compile("(A[0-9]{9})").matcher(barcode.rawValue);
                        String serial = m.find() ? m.group(1) : "";
                        mapData.put("serial", serial);
                        mapData.put("rut", rut);
                    }
                    if (!validarRut(mapData.get("rut"))) {
                        throw new Exception("barcode invalid!");
                    }

                    mListener.onBarcodeCapture(mapData);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean validarRut(String rutdv) {
        String[] data = rutdv.toUpperCase().split("-");
        int rut = Integer.parseInt(data[0]);
        char dv = data[1].charAt(0);

        int m = 0, s = 1;
        for (; rut != 0; rut /= 10) {
            s = (s + rut % 10 * (9 - m++ % 6)) % 11;
        }
        return dv == (char) (s != 0 ? s + 47 : 75);
    }
}
