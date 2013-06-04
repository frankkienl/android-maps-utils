package com.google.maps.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.maps.android.R;

/**
 * BubbleIconFactory generates icons that contain text (or custom content) within a bubble.
 * <p/>
 * The icon {@link Bitmap}s generated by the factory should be used in conjunction with a {@link
 * com.google.android.gms.maps.model.BitmapDescriptorFactory}.
 * <p/>
 * This class is not thread safe.
 */
public class BubbleIconFactory {
    private final Context mContext;

    private ViewGroup mContainer;
    private RotationLayout mRotationLayout;
    private TextView mTextView;
    private View mContentView;

    private int mRotation;

    /**
     * Creates a new BubbleIconFactory with the default style.
     */
    public BubbleIconFactory(Context context) {
        mContext = context;
    }

    /**
     * Sets the text content, then creates an icon with the current style.
     *
     * @param text the text content to display inside the bubble.
     */
    public Bitmap makeIcon(String text) {
        ensureViewsSetUp();

        if (mTextView != null) {
            mTextView.setText(text);
        }

        return makeIcon();
    }

    /**
     * Creates an icon with the current content and style.
     * <p/>
     * This method is useful if a custom view has previously been set, or if text content is not
     * applicable.
     */
    public Bitmap makeIcon() {
        ViewGroup container = getContainer();

        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        container.measure(measureSpec, measureSpec);

        int measuredWidth = container.getMeasuredWidth();
        int measuredHeight = container.getMeasuredHeight();

        container.layout(0, 0, measuredWidth, measuredHeight);

        if (mRotation == 1 || mRotation == 3) {
            measuredHeight = container.getMeasuredWidth();
            measuredWidth = container.getMeasuredHeight();
        }

        Bitmap r = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        r.eraseColor(Color.TRANSPARENT);

        Canvas canvas = new Canvas(r);

        if (mRotation == 0) {
            // do nothing
        } else if (mRotation == 1) {
            canvas.translate(measuredWidth, 0);
            canvas.rotate(90);
        } else if (mRotation == 2) {
            canvas.rotate(180, measuredWidth / 2, measuredHeight / 2);
        } else {
            canvas.translate(0, measuredHeight);
            canvas.rotate(270);
        }
        container.draw(canvas);
        return r;
    }

    /**
     * Sets the child view for the bubble.
     * <p/>
     * If the view contains a {@link TextView} with the id "text", operations such as {@link
     * #setTextAppearance} and {@link #makeIcon(String)} will operate upon that {@link TextView}.
     */
    public void setContentView(View contentView) {
        ensureViewsSetUp();
        mRotationLayout.removeAllViews();
        mRotationLayout.addView(contentView);
        mContentView = contentView;
        try {
            mTextView = (TextView) mRotationLayout.findViewById(R.id.text);
        } catch (Exception e) {
            mTextView = null;
        }
    }

    /**
     * Rotates the contents of the bubble.
     *
     * @param degrees the amount the contents should be rotated, as a multiple of 90 degrees.
     */
    public void setContentRotation(int degrees) {
        ensureViewsSetUp();
        mRotationLayout.setViewRotation(degrees);
    }

    /**
     * Rotates the bubble.
     *
     * @param degrees the amount the bubble should be rotated, as a multiple of 90 degrees.
     */
    public void setRotation(int degrees) {
        mRotation = ((degrees + 360) % 360) / 90;
    }

    /**
     * Sets the text color, size, style, hint color, and highlight color from the specified
     * <code>TextAppearance</code> resource.
     *
     * @param resid the identifier of the resource.
     */
    public void setTextAppearance(Context context, int resid) {
        ensureViewsSetUp();
        if (mTextView != null) {
            mTextView.setTextAppearance(context, resid);
        }
    }

    /**
     * Sets the style of the bubble. The style consists of a background and text appearance.
     */
    public void setStyle(Style style) {
        setBackground(mContext.getResources().getDrawable(style.mResid));
        setTextAppearance(mContext, style.mTextResid);
    }

    /**
     * Set the background to a given Drawable, or remove the background.
     *
     * @param background the Drawable to use as the background, or null to remove the background.
     */
    @SuppressWarnings("deprecation")
    // View#setBackgroundDrawable is compatible with pre-API level 16 (Jelly Bean).
    public void setBackground(Drawable background) {
        getContainer().setBackgroundDrawable(background);
    }

    /**
     * Not thread safe.
     */
    private ViewGroup getContainer() {
        ensureViewsSetUp();
        return mContainer;
    }

    /**
     * Ensure views are ready. This allows us to lazily inflate the main layout.
     */
    private void ensureViewsSetUp() {
        if (mContainer == null) {
            final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContainer = (ViewGroup) inflater.inflate(R.layout.text_bubble, null);
            mRotationLayout = (RotationLayout) mContainer.getChildAt(0);
            mContentView = mTextView = (TextView) mRotationLayout.findViewById(R.id.text);
        }
    }

    /**
     * Sets the padding of the content view. The default padding of the content view (i.e. text
     * view) is 5dp top/bottom and 10dp left/right.
     *
     * @param left   the left padding in pixels.
     * @param top    the top padding in pixels.
     * @param right  the right padding in pixels.
     * @param bottom the bottom padding in pixels.
     */
    public void setContentPadding(int left, int top, int right, int bottom) {
        ensureViewsSetUp();
        mContentView.setPadding(left, top, right, bottom);
    }

    /**
     * Style represents a bubble background drawable and a suitable text appearance.
     */
    public enum Style {
        DEFAULT(R.drawable.bubble_white, R.style.Bubble_TextAppearance_Dark),
        WHITE(R.drawable.bubble_white, R.style.Bubble_TextAppearance_Dark),
        RED(R.drawable.bubble_red, R.style.Bubble_TextAppearance_Light),
        BLUE(R.drawable.bubble_blue, R.style.Bubble_TextAppearance_Light),
        GREEN(R.drawable.bubble_green, R.style.Bubble_TextAppearance_Light),
        PURPLE(R.drawable.bubble_purple, R.style.Bubble_TextAppearance_Light),
        ORANGE(R.drawable.bubble_orange, R.style.Bubble_TextAppearance_Light);

        private final int mResid;
        private final int mTextResid;

        private Style(int resid, int textResid) {
            mResid = resid;
            mTextResid = textResid;
        }
    }
}