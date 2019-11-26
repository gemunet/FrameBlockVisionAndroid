package frameblock.vision.frameblockvisionandroid.camera;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;

import frameblock.vision.camera.FinderGraphicOverlay;
import frameblock.vision.card.Card;

public class CardProcessor implements Detector.Processor<Card> {
    private static final float EXPECTED_RATIO = 1.56f;
    private static final float RATIO_MAXDIFF = 0.05f;
    private static final float PARALLEL_MAXDIFF = 0.05f;

    private FinderGraphicOverlay<CardGraphic> mOverlay;

    public CardProcessor(FinderGraphicOverlay overlay) {
        this.mOverlay = overlay;
    }

    @Override
    public void release() {
        mOverlay.clear();
    }

    @Override
    public void receiveDetections(Detector.Detections<Card> detections) {
        SparseArray<Card> items = detections.getDetectedItems();

        mOverlay.clear();

        if(items.size() > 0) {
            Card card = items.get(0);
            boolean isvalid = (card.checkAspectRatio(EXPECTED_RATIO, RATIO_MAXDIFF) && card.checkParallel(PARALLEL_MAXDIFF));

            Log.d("card", ""+card);
            Log.d("isvalid", ""+isvalid);

            if(isvalid) {
                mOverlay.add(new CardGraphic(mOverlay, card));
            }
        }
    }
}
