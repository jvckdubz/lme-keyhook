package ru.lme.keyhook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Map;

public class ConfigReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra("clear", false)) {
            for (Integer scan : Mappings.all(context).keySet()) Mappings.remove(context, scan);
            Log.i(KeyHookService.TAG, "настройка: все сопоставления убраны");
            return;
        }

        int scan = intent.getIntExtra("scan", -1);
        String pkg = intent.getStringExtra("pkg");
        if (scan <= 0) {
            Log.w(KeyHookService.TAG, "настройка без годного scan — ничего не менял");
            return;
        }
        if (pkg == null || pkg.length() == 0) {
            Mappings.remove(context, scan);
        } else {
            Mappings.put(context, scan, pkg);
        }

        StringBuilder sb = new StringBuilder("настройка принята, сейчас:");
        for (Map.Entry<Integer, String> e : Mappings.all(context).entrySet()) {
            sb.append(' ').append(e.getKey()).append("=").append(e.getValue());
        }
        Log.i(KeyHookService.TAG, sb.toString());
    }
}
