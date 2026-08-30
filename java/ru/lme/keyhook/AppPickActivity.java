package ru.lme.keyhook;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppPickActivity extends Activity {
    public static final String EXTRA_SCAN = "scan";

    private final List<Targets.Entry> items = new ArrayList<Targets.Entry>();
    private int scan;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        scan = getIntent().getIntExtra(EXTRA_SCAN, -1);

        LinearLayout root = Ui.screen(this);
        root.addView(Ui.title(this, "Что открывать"));
        root.addView(Ui.subtitle(this, "Кнопка с кодом " + scan));
        ListView list = Ui.list(this);
        root.addView(list);
        setContentView(root);

        load();

        list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return items.size();
            }

            @Override
            public Object getItem(int position) {
                return items.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convert, ViewGroup parent) {
                Targets.Entry e = items.get(position);
                return Ui.row(AppPickActivity.this, e.label, e.note);
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (scan <= 0) {
                    finish();
                    return;
                }
                Targets.Entry e = items.get(position);
                Mappings.put(AppPickActivity.this, scan, e.target);
                Toast.makeText(AppPickActivity.this, "Назначено: " + e.label,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void load() {
        items.addAll(Targets.inputs(this));

        PackageManager pm = getPackageManager();
        Set<String> seen = new HashSet<String>();
        seen.add(getPackageName());

        List<ResolveInfo> found = new ArrayList<ResolveInfo>();
        found.addAll(pm.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), 0));
        found.addAll(pm.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0));

        List<Targets.Entry> apps = new ArrayList<Targets.Entry>();
        for (ResolveInfo ri : found) {
            String pkg = ri.activityInfo.packageName;
            if (!seen.add(pkg)) continue;
            String label;
            try {
                label = ri.loadLabel(pm).toString();
            } catch (Exception e) {
                label = pkg;
            }
            apps.add(new Targets.Entry(pkg, label, pkg));
        }
        Collections.sort(apps, new Comparator<Targets.Entry>() {
            @Override
            public int compare(Targets.Entry a, Targets.Entry b) {
                return a.label.compareToIgnoreCase(b.label);
            }
        });
        items.addAll(apps);
    }
}
