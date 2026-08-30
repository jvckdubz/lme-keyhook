package ru.lme.keyhook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.KeyEvent;
import android.view.View;

public class RemoteView extends View {
    public interface Listener {
        void onSelected(int index);

        void onActivate(int index);
    }

    public static final int OTHER = Keys.BRANDED.length;

    private static final float BODY_RATIO = 3.0f;

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF[] slots = new RectF[Keys.BRANDED.length + 1];
    private final RectF body = new RectF();
    private final RectF vol = new RectF();

    private int selected = 0;
    private int lastCol = 0;
    private Listener listener;
    private boolean[] assigned = new boolean[Keys.BRANDED.length];

    public RemoteView(Context c) {
        super(c);
        setFocusable(true);
        setFocusableInTouchMode(true);
        for (int i = 0; i < slots.length; i++) slots[i] = new RectF();
        stroke.setStyle(Paint.Style.STROKE);
        text.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        text.setTextAlign(Paint.Align.CENTER);
    }

    public void setListener(Listener l) {
        listener = l;
    }

    public void setAssigned(boolean[] flags) {
        assigned = flags;
        invalidate();
    }

    public int getSelected() {
        return selected;
    }

    public void select(int index) {
        if (index < 0 || index >= slots.length || index == selected) return;
        selected = index;
        if (index < Keys.BRANDED.length) lastCol = index % 2;
        invalidate();
        if (listener != null) listener.onSelected(selected);
    }

    public boolean selectByScan(int scan) {
        for (int i = 0; i < Keys.BRANDED.length; i++) {
            if (Keys.BRANDED[i].scan == scan) {
                select(i);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        int h = MeasureSpec.getSize(hSpec);

        int w = (int) (h * 0.84f / BODY_RATIO * 1.3f);
        setMeasuredDimension(Math.min(w, MeasureSpec.getSize(wSpec)), h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        layoutSlots(w, h);
    }

    private void layoutSlots(int w, int h) {
        float bodyH = h * 0.84f;
        float bodyW = bodyH / BODY_RATIO;
        float cx = w / 2f;
        body.set(cx - bodyW / 2f, 0, cx + bodyW / 2f, bodyH);

        vol.set(cx - bodyW * 0.085f, bodyH * 0.430f, cx + bodyW * 0.085f, bodyH * 0.570f);

        float pillW = bodyW * 0.38f;
        float pillH = bodyH * 0.055f;
        float gapX = bodyW * 0.08f;
        float gapY = pillH * 0.42f;
        float startY = bodyH * 0.60f;
        for (int i = 0; i < Keys.BRANDED.length; i++) {
            int row = i / 2, col = i % 2;
            float left = col == 0 ? cx - gapX / 2f - pillW : cx + gapX / 2f;
            float top = startY + row * (pillH + gapY);
            slots[i].set(left, top, left + pillW, top + pillH);
        }

        float otherW = bodyW * 0.85f;
        float otherH = pillH * 0.9f;
        float otherTop = bodyH + h * 0.035f;
        slots[OTHER].set(cx - otherW / 2f, otherTop, cx + otherW / 2f, otherTop + otherH);
    }

    @Override
    protected void onDraw(Canvas cv) {
        float bw = body.width();
        float bh = body.height();
        float cx = body.centerX();

        fill.setColor(0xFF1A1F26);
        cv.drawRoundRect(body, bw * 0.22f, bw * 0.22f, fill);
        stroke.setColor(0xFF2A323C);
        stroke.setStrokeWidth(Math.max(1f, bw * 0.012f));
        cv.drawRoundRect(body, bw * 0.22f, bw * 0.22f, stroke);

        int dim = 0xFF39424E;
        fill.setColor(dim);
        cv.drawCircle(cx, bh * 0.058f, bw * 0.105f, fill);
        cv.drawCircle(cx - bw * 0.235f, bh * 0.132f, bw * 0.095f, fill);
        cv.drawCircle(cx + bw * 0.235f, bh * 0.132f, bw * 0.095f, fill);
        stroke.setColor(dim);
        stroke.setStrokeWidth(bw * 0.075f);
        cv.drawCircle(cx, bh * 0.275f, bw * 0.315f, stroke);
        fill.setColor(0xFF454F5C);
        cv.drawCircle(cx, bh * 0.275f, bw * 0.135f, fill);
        fill.setColor(dim);
        cv.drawCircle(cx - bw * 0.245f, bh * 0.455f, bw * 0.09f, fill);
        cv.drawCircle(cx + bw * 0.245f, bh * 0.455f, bw * 0.09f, fill);
        cv.drawRoundRect(vol, bw * 0.085f, bw * 0.085f, fill);

        for (int i = 0; i < Keys.BRANDED.length; i++) {
            RectF r = slots[i];
            float radius = r.height() / 2f;
            boolean on = i < assigned.length && assigned[i];
            if (on) {
                fill.setColor(Keys.BRANDED[i].color);
                cv.drawRoundRect(r, radius, radius, fill);
            } else {
                fill.setColor(0xFF11151A);
                cv.drawRoundRect(r, radius, radius, fill);
                stroke.setColor(withAlpha(Keys.BRANDED[i].color, 0x99));
                stroke.setStrokeWidth(Math.max(1.5f, r.height() * 0.08f));
                cv.drawRoundRect(r, radius, radius, stroke);
            }

            text.setColor(on ? (isLight(Keys.BRANDED[i].color) ? 0xFF10151B : 0xFFFFFFFF)
                    : withAlpha(Keys.BRANDED[i].color, 0xDD));
            text.setTextSize(r.height() * 0.44f);
            cv.drawText(Keys.BRANDED[i].shortLabel, r.centerX(),
                    r.centerY() + r.height() * 0.16f, text);
            if (selected == i) drawFocus(cv, r, radius);
        }

        RectF o = slots[OTHER];
        float orad = o.height() * 0.32f;
        fill.setColor(0xFF171C22);
        cv.drawRoundRect(o, orad, orad, fill);
        stroke.setColor(0xFF333D49);
        stroke.setStrokeWidth(1.5f);
        cv.drawRoundRect(o, orad, orad, stroke);
        text.setColor(0xFFAEB8C2);
        text.setTextSize(o.height() * 0.40f);
        cv.drawText("другая кнопка", o.centerX(), o.centerY() + o.height() * 0.14f, text);
        if (selected == OTHER) drawFocus(cv, o, orad);
    }

    private void drawFocus(Canvas cv, RectF r, float radius) {
        float pad = r.height() * 0.34f;
        RectF f = new RectF(r.left - pad, r.top - pad, r.right + pad, r.bottom + pad);
        stroke.setColor(hasFocus() ? 0xFF7AA7FF : 0x66445060);
        stroke.setStrokeWidth(Math.max(2f, r.height() * 0.10f));
        cv.drawRoundRect(f, radius + pad, radius + pad, stroke);
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private static boolean isLight(int color) {
        int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
        return (r * 299 + g * 587 + b * 114) / 1000 > 150;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (selected < Keys.BRANDED.length && selected % 2 == 1) select(selected - 1);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (selected < Keys.BRANDED.length && selected % 2 == 0) select(selected + 1);
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (selected == OTHER) select(4 + lastCol);
                else if (selected >= 2) select(selected - 2);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (selected < Keys.BRANDED.length - 2) select(selected + 2);
                else if (selected < Keys.BRANDED.length) select(OTHER);
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (listener != null) listener.onActivate(selected);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onFocusChanged(boolean gained, int direction, android.graphics.Rect prev) {
        super.onFocusChanged(gained, direction, prev);
        invalidate();
    }
}
