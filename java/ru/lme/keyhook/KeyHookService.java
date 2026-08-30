package ru.lme.keyhook;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class KeyHookService extends AccessibilityService {
    public static final String TAG = "LmeKeyHook";

    @Override
    protected void onServiceConnected() {
        Mappings.reload(this);
        Log.i(TAG, "сервис подключён, сопоставлений: " + Mappings.all(this).size());
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        final int code = event.getKeyCode();

        if (Learn.active()) {
            if (Keys.isNavigation(code)) return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                Log.i(TAG, "экран настройки: scan=" + event.getScanCode() + " keyCode=" + code);
                Learn.deliver(event.getScanCode(), code);
            }
            return true;
        }

        final String target = Mappings.packageFor(this, event.getScanCode());
        if (target == null) return false;

        if (event.getAction() != KeyEvent.ACTION_DOWN) return true;
        if (event.getRepeatCount() > 0) return true;

        open(target);
        return true;
    }

    private void open(String target) {
        Intent i = Targets.intentFor(this, target);
        if (i == null) {
            Log.w(TAG, "нечем открывать " + target);
            return;
        }
        try {
            startActivity(i);
            Log.i(TAG, "открыто " + target);
        } catch (Exception e) {
            Log.e(TAG, "не удалось открыть " + target, e);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
}
