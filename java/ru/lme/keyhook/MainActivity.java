package ru.lme.keyhook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Learn.Listener, RemoteView.Listener {
    private RemoteView remote;
    private TextView panelTitle;
    private TextView panelTarget;
    private TextView panelHint;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Ui.BG);

        root.setPadding(Ui.dp(this, 96), Ui.dp(this, 36), Ui.dp(this, 64), Ui.dp(this, 36));

        remote = new RemoteView(this);
        remote.setListener(this);
        root.addView(remote, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER_VERTICAL);
        panel.setPadding(Ui.dp(this, 48), 0, 0, 0);

        TextView head = new TextView(this);
        head.setText("Кнопки пульта");
        head.setTextColor(Ui.DIM);
        head.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        head.setAllCaps(true);
        head.setLetterSpacing(0.14f);
        panel.addView(head);

        panelTitle = new TextView(this);
        panelTitle.setTextColor(Ui.TEXT);
        panelTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
        panelTitle.setPadding(0, Ui.dp(this, 6), 0, 0);
        panel.addView(panelTitle);

        panelTarget = new TextView(this);
        panelTarget.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
        panelTarget.setPadding(0, Ui.dp(this, 10), 0, 0);
        panel.addView(panelTarget);

        panelHint = new TextView(this);
        panelHint.setTextColor(Ui.DIM);
        panelHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        panelHint.setLineSpacing(0, 1.25f);
        panelHint.setPadding(0, Ui.dp(this, 28), 0, 0);
        panel.addView(panelHint);

        root.addView(panel, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        setContentView(root);
        remote.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Learn.set(this);
        if (!serviceEnabled()) offerToEnable();
        refresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Learn.set(null);
    }

    private void refresh() {
        boolean[] flags = new boolean[Keys.BRANDED.length];
        for (int i = 0; i < Keys.BRANDED.length; i++) {
            flags[i] = Mappings.packageFor(this, Keys.BRANDED[i].scan) != null;
        }
        remote.setAssigned(flags);
        describe(remote.getSelected());
    }

    private void describe(int index) {
        if (index == RemoteView.OTHER) {
            panelTitle.setText("Другая кнопка");
            panelTarget.setTextColor(Ui.DIM);
            panelTarget.setText("для кнопок, которых нет на схеме");
            panelHint.setText("«ОК» — и нажми любую кнопку на пульте.\n"
                    + "Стрелки, «назад», «домой» и громкость назначить нельзя: пульт стал бы "
                    + "неуправляемым.");
            return;
        }
        Keys.Btn b = Keys.BRANDED[index];
        String target = Mappings.packageFor(this, b.scan);
        panelTitle.setText(b.label);
        if (target == null) {
            panelTarget.setTextColor(Ui.DIM);
            panelTarget.setText("не назначена — откроет Google Play");
            panelHint.setText("«ОК» — выбрать приложение или вход телевизора.\n"
                    + "Код кнопки " + b.scan + ".");
        } else {
            panelTarget.setTextColor(b.color);
            panelTarget.setText(Targets.label(this, target));
            panelHint.setText("«ОК» — изменить или убрать.\n"
                    + "Код кнопки " + b.scan + ".");
        }
    }

    @Override
    public void onSelected(int index) {
        describe(index);
    }

    @Override
    public void onActivate(int index) {
        if (index == RemoteView.OTHER) {
            startActivity(new Intent(this, LearnActivity.class));
            return;
        }
        final Keys.Btn b = Keys.BRANDED[index];
        final boolean assigned = Mappings.packageFor(this, b.scan) != null;
        CharSequence[] items = assigned
                ? new CharSequence[]{"Выбрать другое", "Убрать назначение"}
                : new CharSequence[]{"Выбрать приложение или вход"};
        new AlertDialog.Builder(this)
                .setTitle(b.label)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        if (assigned && which == 1) {
                            Mappings.remove(MainActivity.this, b.scan);
                            refresh();
                        } else {
                            Intent i = new Intent(MainActivity.this, AppPickActivity.class);
                            i.putExtra(AppPickActivity.EXTRA_SCAN, b.scan);
                            startActivity(i);
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onKey(final int scan, final int keyCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!remote.selectByScan(scan)) {
                    Toast.makeText(MainActivity.this,
                            "Эта кнопка не на схеме, код " + scan + " — назначь через «другая кнопка»",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean serviceEnabled() {
        String on = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return on != null && on.contains(getPackageName() + "/");
    }

    private void offerToEnable() {
        new AlertDialog.Builder(this)
                .setTitle("Перехват выключен")

                .setMessage("Приложение не сможет ловить кнопки, пока в настройках телевизора "
                        + "не включён его сервис специальных возможностей.\n\n"
                        + "Настройки → Настройки устройства → Специальные возможности → "
                        + "Кнопки пульта")
                .setPositiveButton("Открыть настройки", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        openSettings();
                    }
                })
                .setNegativeButton("Потом", null)
                .show();
    }

    private void openSettings() {
        Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        ResolveInfo r = getPackageManager().resolveActivity(i, 0);
        if (r == null || r.activityInfo == null
                || r.activityInfo.packageName.contains("frameworkpackagestubs")) {
            i = new Intent(Settings.ACTION_SETTINGS);
        }
        try {
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Настройки не открылись, зайди в них с главного экрана",
                    Toast.LENGTH_LONG).show();
        }
    }
}
