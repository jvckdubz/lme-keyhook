package ru.lme.keyhook;

public final class Learn {
    public interface Listener {
        void onKey(int scan, int keyCode);
    }

    private static volatile Listener listener;

    private Learn() {
    }

    public static void set(Listener l) {
        listener = l;
    }

    public static boolean active() {
        return listener != null;
    }

    static void deliver(int scan, int keyCode) {
        Listener l = listener;
        if (l != null) l.onKey(scan, keyCode);
    }
}
