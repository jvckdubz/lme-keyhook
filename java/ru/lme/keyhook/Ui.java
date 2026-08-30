package ru.lme.keyhook;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

final class Ui {
    static final int BG = 0xFF101215;
    static final int TEXT = 0xFFF2F4F5;
    static final int DIM = 0xFF9AA3AA;

    static final int FOCUS = 0xFF1F4FA8;
    static final int SUB_ON_FOCUS = 0xFFD3DEEA;

    private Ui() {
    }

    static int dp(Context c, int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                c.getResources().getDisplayMetrics());
    }

    static LinearLayout screen(Context c) {
        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        int pad = dp(c, 32);
        root.setPadding(pad, dp(c, 28), pad, pad);
        return root;
    }

    static TextView title(Context c, String text) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextColor(TEXT);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        t.setPadding(dp(c, 12), 0, 0, dp(c, 4));
        return t;
    }

    static TextView subtitle(Context c, String text) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextColor(DIM);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        t.setPadding(dp(c, 12), 0, 0, dp(c, 16));
        return t;
    }

    static ListView list(Context c) {
        ListView l = new ListView(c);
        StateListDrawable sel = new StateListDrawable();
        sel.addState(new int[]{android.R.attr.state_focused}, new ColorDrawable(FOCUS));
        sel.addState(new int[0], new ColorDrawable(Color.TRANSPARENT));
        l.setSelector(sel);
        l.setDrawSelectorOnTop(false);
        l.setDivider(new ColorDrawable(0xFF1E2329));
        l.setDividerHeight(1);
        l.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        return l;
    }

    static LinearLayout row(Context c, String main, String sub) {
        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(c, 12), dp(c, 14), dp(c, 12), dp(c, 14));
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setDuplicateParentStateEnabled(true);

        TextView m = new TextView(c);
        m.setText(main);
        m.setTextColor(TEXT);
        m.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        m.setDuplicateParentStateEnabled(true);
        box.addView(m);

        if (sub != null && sub.length() > 0) {
            TextView s = new TextView(c);
            s.setText(sub);
            s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            s.setDuplicateParentStateEnabled(true);
            s.setTextColor(new android.content.res.ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_selected},
                            new int[]{android.R.attr.state_focused},
                            new int[]{android.R.attr.state_pressed},
                            new int[0]
                    },
                    new int[]{SUB_ON_FOCUS, SUB_ON_FOCUS, SUB_ON_FOCUS, DIM}));
            box.addView(s);
        }
        return box;
    }
}
