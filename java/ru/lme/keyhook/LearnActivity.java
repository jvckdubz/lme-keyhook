package ru.lme.keyhook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LearnActivity extends Activity implements Learn.Listener {
    private TextView hint;
    private boolean handled;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        LinearLayout root = Ui.screen(this);
        root.addView(Ui.title(this, "Нажми кнопку на пульте"));
        root.addView(Ui.subtitle(this, "Ту, которую хочешь назначить. «Назад» — отмена."));

        hint = new TextView(this);
        hint.setTextColor(Ui.DIM);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        hint.setPadding(Ui.dp(this, 12), Ui.dp(this, 24), 0, 0);
        hint.setText("Жду нажатия…");
        root.addView(hint);
        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handled = false;
        Learn.set(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Learn.set(null);
    }

    @Override
    public void onKey(final int scan, final int keyCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (handled) return;

                if (scan <= 0) {
                    hint.setText("Эта кнопка приходит без кода устройства, назначить её нельзя.\n"
                            + "Попробуй другую.");
                    return;
                }
                handled = true;
                Learn.set(null);

                Intent i = new Intent(LearnActivity.this, AppPickActivity.class);
                i.putExtra(AppPickActivity.EXTRA_SCAN, scan);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
