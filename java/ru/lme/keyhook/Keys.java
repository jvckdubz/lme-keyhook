package ru.lme.keyhook;

import android.view.KeyEvent;

public final class Keys {
    public static final class Btn {
        public final int scan;

        public final String label;

        public final String shortLabel;
        public final int color;

        Btn(int scan, String label, String shortLabel, int color) {
            this.scan = scan;
            this.label = label;
            this.shortLabel = shortLabel;
            this.color = color;
        }
    }

    public static final Btn[] BRANDED = {
            new Btn(433, "иви", "иви", 0xFFE0304A),
            new Btn(152, "Okko", "Okko", 0xFF3A2E8F),
            new Btn(423, "VK Видео", "VK", 0xFF2787F5),
            new Btn(426, "RUTUBE", "RUTUBE", 0xFF98A4B6),
            new Btn(362, "KION", "KION", 0xFFD11E4C),
            new Btn(379, "Wink", "Wink", 0xFFEE7203),
    };

    private Keys() {
    }

    public static Btn byScan(int scan) {
        for (Btn b : BRANDED) if (b.scan == scan) return b;
        return null;
    }

    public static boolean isNavigation(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_POWER:
                return true;
            default:
                return false;
        }
    }
}
