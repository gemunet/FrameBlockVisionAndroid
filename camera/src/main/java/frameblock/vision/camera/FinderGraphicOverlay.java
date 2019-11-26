package frameblock.vision.camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;

public class FinderGraphicOverlay<T extends GraphicOverlay.Graphic> extends GraphicOverlay<T> {
    private static final int GRAVITY_TOP = 1;
    private static final int GRAVITY_LEFT = 2;
    private static final int GRAVITY_BOTTOM = 4;
    private static final int GRAVITY_RIGHT = 8;

    private static final int FINDER_SHAPE_RECTANGLE = 0;
    private static final int FINDER_SHAPE_CIRCLE = 1;

    private static final float DEFAULT_ASPECT_RATIO = 0f;
    private static final int DEFAULT_MARGIN = 0;
    private static final int DEFAULT_GRAVITY = GRAVITY_TOP | GRAVITY_RIGHT | GRAVITY_BOTTOM | GRAVITY_LEFT;
    private static final int DEFAULT_CORNER_RADIUS = 35;

    private float mAspectRatio = DEFAULT_ASPECT_RATIO;
    private int mMargin;
    private int mGravity;
    private int mFinderColor;
    private int mFinderMaskColor;
    private int mFinderCornerRadius;
    private Paint mDrawFinderPaint;
    private Paint mDrawMaskPaint;
    private Paint mDrawHolePaint;
    private int mFinderShape;

    private Rect mFinderRoi = new Rect();
    private Rect mScreenSize = new Rect();

    public FinderGraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FinderGraphicOverlay);
        mMargin = a.getDimensionPixelSize(R.styleable.FinderGraphicOverlay_finder_margin, DEFAULT_MARGIN);
        mGravity = a.getInt(R.styleable.FinderGraphicOverlay_finder_gravity, DEFAULT_GRAVITY);
        mFinderColor = a.getColor(R.styleable.FinderGraphicOverlay_finder_color, getResources().getColor(R.color.finder_color));
        mFinderMaskColor = a.getColor(R.styleable.FinderGraphicOverlay_finder_maskColor, -1);
        mFinderCornerRadius = a.getDimensionPixelSize(R.styleable.FinderGraphicOverlay_finder_cornerRadius, DEFAULT_CORNER_RADIUS);
        mFinderShape = a.getInt(R.styleable.FinderGraphicOverlay_finder_shape, FINDER_SHAPE_RECTANGLE);

        String ratio = a.getString(R.styleable.FinderGraphicOverlay_finder_aspectRatio);
        if (ratio != null) {
            int colonIndex = ratio.indexOf(':');
            if (colonIndex >= 0 && colonIndex < ratio.length() - 1) {
                String nominator = ratio.substring(0, colonIndex);
                String denominator = ratio.substring(colonIndex + 1);
                try {
                    float nominatorValue = Float.parseFloat(nominator);
                    float denominatorValue = Float.parseFloat(denominator);
                    if (nominatorValue > 0 && denominatorValue > 0) {
                        mAspectRatio = Math.abs(nominatorValue / denominatorValue);
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            } else {
                if (ratio.length() > 0) {
                    try {
                        mAspectRatio = Float.parseFloat(ratio);
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }
        }


        a.recycle();

        mDrawFinderPaint = new Paint();
        mDrawFinderPaint.setColor(mFinderColor);
        mDrawFinderPaint.setStyle(Paint.Style.STROKE);
        mDrawFinderPaint.setStrokeWidth(5.0f);

        mDrawHolePaint = new Paint();
        mDrawHolePaint.setColor(getResources().getColor(android.R.color.transparent));
        mDrawHolePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        if(mFinderMaskColor != -1) {
            mDrawMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mDrawMaskPaint.setStyle(Paint.Style.FILL);
            mDrawMaskPaint.setColor(mFinderMaskColor);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        getLocalVisibleRect(mScreenSize);

        // portraint
        if(mScreenSize.width() < mScreenSize.height()) {
            mFinderRoi.left = mScreenSize.left+mMargin;
            mFinderRoi.right = mScreenSize.right-mMargin;
            mFinderRoi.top = mScreenSize.top+mMargin;

            if(mAspectRatio > 0) {
                mFinderRoi.bottom = (int)(mFinderRoi.width() / mAspectRatio)+mMargin;
            } else {
                mFinderRoi.bottom = mScreenSize.bottom-mMargin;
            }

            if(mGravity == GRAVITY_TOP) {
                mFinderRoi.offsetTo(mFinderRoi.left, mScreenSize.top+mMargin);
            }
            else if(mGravity == GRAVITY_BOTTOM) {
                mFinderRoi.offsetTo(mFinderRoi.left, mScreenSize.bottom-mFinderRoi.height()-mMargin);
            }
            else {
                mFinderRoi.offsetTo(mFinderRoi.left, mScreenSize.centerY()-mFinderRoi.height()/2);
            }

        }
        // landscape
        else {
            mFinderRoi.top = mScreenSize.top+mMargin;
            mFinderRoi.bottom = mScreenSize.bottom-mMargin;
            mFinderRoi.left = mScreenSize.left+mMargin;

            if(mAspectRatio > 0) {
                mFinderRoi.right = (int)(mFinderRoi.height() * mAspectRatio)+mMargin;
            } else {
                mFinderRoi.right = mScreenSize.right-mMargin;
            }

            if(mGravity == GRAVITY_LEFT) {
                mFinderRoi.offsetTo(mScreenSize.left+mMargin, mFinderRoi.top);
            }
            else if(mGravity == GRAVITY_RIGHT) {
                mFinderRoi.offsetTo(mScreenSize.right-mFinderRoi.width()-mMargin, mFinderRoi.top);
            } else {
                mFinderRoi.offsetTo(mScreenSize.centerX()-mFinderRoi.width()/2, mFinderRoi.top);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        RectF finder = new RectF(mFinderRoi);

        if(mDrawMaskPaint != null) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();

            canvas.drawRect(0,0, width, height, mDrawMaskPaint);
        }

        if(mFinderShape == FINDER_SHAPE_RECTANGLE) {
            canvas.drawRoundRect(finder, mFinderCornerRadius, mFinderCornerRadius, mDrawHolePaint);
            canvas.drawRoundRect(finder, mFinderCornerRadius, mFinderCornerRadius, mDrawFinderPaint);
        } else {
            float radius = finder.width() < finder.height() ? finder.width() / 2 : finder.height() / 2;
            canvas.drawCircle(finder.centerX(), finder.centerY(), radius, mDrawHolePaint);
            canvas.drawCircle(finder.centerX(), finder.centerY(), radius, mDrawFinderPaint);
        }

        super.onDraw(canvas);
    }

    /**
     * get finder area in image preview coordinates
     * @return
     */
    public Rect getScaledFinder() {
        float sw = getWidthScaleFactor();
        float sh = getHeightScaleFactor();

        return new Rect((int)(mFinderRoi.left/sw), (int)(mFinderRoi.top/sh),
                (int)(mFinderRoi.right/sw), (int)(mFinderRoi.bottom/sh));
    }
}
