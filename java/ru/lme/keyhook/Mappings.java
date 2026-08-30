package ru.lme.keyhook;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.SparseArray;

import java.util.Map;
import java.util.TreeMap;

public final class Mappings {
    public static final String PREFS = "keyhook";
    private static final String PREFIX = "map_";

    private static final SparseArray<String> cache = new SparseArray<String>();
    private static volatile boolean loaded;

    private Mappings() {
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String packageFor(Context c, int scan) {
        if (!loaded) reload(c);
        synchronized (cache) {
            return cache.get(scan);
        }
    }

    public static void reload(Context c) {
        synchronized (cache) {
            cache.clear();
            for (Map.Entry<String, ?> e : prefs(c).getAll().entrySet()) {
                String k = e.getKey();
                if (!k.startsWith(PREFIX)) continue;
                if (!(e.getValue() instanceof String)) continue;
                try {
                    cache.put(Integer.parseInt(k.substring(PREFIX.length())), (String) e.getValue());
                } catch (NumberFormatException ignored) {
                }
            }
            loaded = true;
        }
    }

    public static void put(Context c, int scan, String target) {
        prefs(c).edit().putString(PREFIX + scan, target).apply();
        synchronized (cache) {
            cache.put(scan, target);
        }
    }

    public static void remove(Context c, int scan) {
        prefs(c).edit().remove(PREFIX + scan).apply();
        synchronized (cache) {
            cache.remove(scan);
        }
    }

    public static Map<Integer, String> all(Context c) {
        if (!loaded) reload(c);
        Map<Integer, String> out = new TreeMap<Integer, String>();
        synchronized (cache) {
            for (int i = 0; i < cache.size(); i++) {
                out.put(cache.keyAt(i), cache.valueAt(i));
            }
        }
        return out;
    }
}
