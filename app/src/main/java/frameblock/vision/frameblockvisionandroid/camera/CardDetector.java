package frameblock.vision.frameblockvisionandroid.camera;

import android.content.Context;
import android.graphics.Rect;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

import frameblock.vision.camera.FinderGraphicOverlay;
import frameblock.vision.camera.PreviewUtil;
import frameblock.vision.geometric.Polygon;
import frameblock.vision.geometric.QuadrilateralDetector;
import frameblock.vision.image.YuvUtil;

public class CardDetector extends Detector<Polygon> {
    private FinderGraphicOverlay mOverlay;
    private Context mContext;
    private Frame frame;

    public CardDetector(Context context, FinderGraphicOverlay overlay) {
        this.mContext = context;
        this.mOverlay = overlay;
    }

    @Override
    public SparseArray<Polygon> detect(Frame frame) {
        //System.out.println("getFormat:" + frame.getMetadata().getFormat());
        this.frame = frame;

        SparseArray<Polygon> foos = new SparseArray<>();

        byte[] yuvImage = frame.getGrayscaleImageData().array();
        Rect yuvRect = new Rect(0, 0, frame.getMetadata().getWidth(), frame.getMetadata().getHeight());

        Rect roi = mOverlay.getScaledFinder();
        if(PreviewUtil.isPortraitMode(mContext)) {
            yuvImage = YuvUtil.yuvPortraitToLandscape(yuvImage, yuvRect.width(), yuvRect.height());
            yuvRect = new Rect(0, 0, yuvRect.height(), yuvRect.width());
        }

        //System.out.println("yuvImage Len:" + yuvImage.length + ", w:" + yuvRect.width() + ", h:" + yuvRect.height());

        Polygon polygon = QuadrilateralDetector.detectLargestQuad(yuvImage, yuvRect.width(), yuvRect.height(), roi, null);
        if(polygon != null) {
            foos.append(0, polygon);
        }
        return foos;
    }

    public Frame getFrame() {
        return frame;
    }
}
