package com.cfreesespuffs.github.giftswapper.Activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cfreesespuffs.github.giftswapper.R;

public class BadgeDrawable extends Drawable {

    private final Paint mBadgePaint;
    private final Paint mTextPaint;
    private final Rect mTxtRect = new Rect();
    private String mCount = "";
    private boolean mWillDraw = false;

    public BadgeDrawable(Context paramContext) { // https://codkiller0911.medium.com/adding-notification-badges-to-the-icons-in-android-b29ab4e15625

        float mTextSize = paramContext.getResources().getDimension(R.dimen.browser_actions_context_menu_min_padding) * .8f;
        this.mBadgePaint = new Paint();
        this.mBadgePaint.setColor(paramContext.getResources().getColor(R.color.black));
        this.mBadgePaint.setAntiAlias(true);
        this.mBadgePaint.setStyle(Paint.Style.FILL);
        this.mTextPaint = new Paint();
        this.mTextPaint.setColor(Color.WHITE);
        this.mTextPaint.setTypeface(Typeface.DEFAULT);
        this.mTextPaint.setTextSize(mTextSize);
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void draw(Canvas paramCanvas) {
        if (!this.mWillDraw) {
            return;
        }

        Rect localRect = getBounds();
        float width = (localRect.right - localRect.left); // changes location of where badge will show.
        float height = localRect.bottom - localRect.top;
        float circleRadius;

        if (Integer.parseInt(this.mCount) < 10) {
            circleRadius = Math.min(width, height) / 4.0f + 2.5F;
        } else {
            circleRadius = Math.min(width, height) / 4.0f + 4.5F;
        }

        float circleX = width - circleRadius + 6.2F;
        float circleY = circleRadius - 9.5f;

        paramCanvas.drawCircle(circleX, circleY, circleRadius, this.mBadgePaint);

        this.mTextPaint.getTextBounds(this.mCount, 0, this.mCount.length(), this.mTxtRect);

        float textY = circleY + (this.mTxtRect.bottom - this.mTxtRect.top) / 2.0F;
        float textX = circleX;

        if (Integer.parseInt(this.mCount) >= 10) {
            textX = textX - 1.0F;
            textY = textY - 1.0F;
        }

        paramCanvas.drawText(this.mCount, textX, textY, this.mTextPaint);
    }

    @Override
    public void setAlpha(int paramInt) {
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    public void setCount(int paramInt) {
        this.mCount = Integer.toString(paramInt);
        if (paramInt > 0) {
            this.mWillDraw = true;
            invalidateSelf();
        }
    }
}
