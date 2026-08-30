package ru.lme.keyhook;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public final class Targets {
    private static final String INPUT = "input:";

    private Targets() {
    }

    public static boolean isInput(String target) {
        return target != null && target.startsWith(INPUT);
    }

    public static String forInput(String inputId) {
        return INPUT + inputId;
    }

    public static String inputId(String target) {
        return isInput(target) ? target.substring(INPUT.length()) : null;
    }

    public static final class Entry {
        public final String target;
        public final String label;
        public final String note;

        Entry(String target, String label, String note) {
            this.target = target;
            this.label = label;
            this.note = note;
        }
    }

    public static List<Entry> inputs(Context c) {
        List<Entry> out = new ArrayList<Entry>();
        TvInputManager tv = (TvInputManager) c.getSystemService(Context.TV_INPUT_SERVICE);
        if (tv == null) return out;
        try {
            for (TvInputInfo info : tv.getTvInputList()) {
                if (info == null || !info.isPassthroughInput()) continue;
                String label = null;
                try {
                    CharSequence custom = info.loadCustomLabel(c);
                    if (custom != null && custom.length() > 0) label = custom.toString();
                    if (label == null) {
                        CharSequence l = info.loadLabel(c);
                        if (l != null) label = l.toString();
                    }
                } catch (Exception ignored) {
                }
                if (label == null) label = info.getId();
                out.add(new Entry(forInput(info.getId()), label, "вход телевизора"));
            }
        } catch (Exception e) {
            Log.w(KeyHookService.TAG, "не удалось перечислить входы", e);
        }
        return out;
    }

    public static String label(Context c, String target) {
        if (isInput(target)) {
            String id = inputId(target);
            for (Entry e : inputs(c)) {
                if (e.target.equals(target)) return e.label;
            }
            return id;
        }
        try {
            PackageManager pm = c.getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(target, 0)).toString();
        } catch (Exception e) {
            return target;
        }
    }

    public static Intent intentFor(Context c, String target) {
        if (isInput(target)) {
            Uri uri = TvContract.buildChannelUriForPassthroughInput(inputId(target));
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return i;
        }
        PackageManager pm = c.getPackageManager();
        Intent i = pm.getLeanbackLaunchIntentForPackage(target);
        if (i == null) i = pm.getLaunchIntentForPackage(target);
        if (i != null) i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }
}
