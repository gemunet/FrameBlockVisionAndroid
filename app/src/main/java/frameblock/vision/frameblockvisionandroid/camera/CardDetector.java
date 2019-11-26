package frameblock.vision.frameblockvisionandroid.camera;

import android.content.Context;
import android.graphics.Rect;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

import frameblock.vision.camera.FinderGraphicOverlay;
import frameblock.vision.camera.PreviewUtil;
import frameblock.vision.card.Card;
import frameblock.vision.image.YuvUtil;

public class CardDetector extends Detector<Card> {
    private FinderGraphicOverlay mOverlay;
    private Context mContext;
    private Frame frame;

    public CardDetector(Context context, FinderGraphicOverlay overlay) {
        this.mContext = context;
        this.mOverlay = overlay;
    }

    @Override
    public SparseArray<Card> detect(Frame frame) {
        this.frame = frame;

        SparseArray<Card> foos = new SparseArray<>();

        byte[] yuvImage = frame.getGrayscaleImageData().array();
        Rect yuvRect = new Rect(0, 0, frame.getMetadata().getWidth(), frame.getMetadata().getHeight());

        Rect roi = mOverlay.getScaledFinder();
        if(PreviewUtil.isPortraitMode(mContext)) {
            yuvImage = YuvUtil.yuvPortraitToLandscape(yuvImage, yuvRect.width(), yuvRect.height());
            yuvRect = new Rect(0, 0, yuvRect.height(), yuvRect.width());
        }

        Card card = frameblock.vision.card.CardDetector.detectLargestCard(yuvImage, yuvRect.width(), yuvRect.height(), roi, null);
        if(card != null) {
            foos.append(0, card);
        }
        return foos;
    }

    public Frame getFrame() {
        return frame;
    }
}
