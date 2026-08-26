package com.minis.purplenotifytheme;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayDeque;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * ColorOS 15 / AOSP 通知行的保守主题 Hook。
 * 只在通知视图挂载后设置外层背景；找不到类时直接跳过，避免影响 SystemUI。
 */
public class PurpleNotifyHook implements IXposedHookLoadPackage {
    private static final int TAG_KEY = 0x7f0f5a19;
    private static final String[] ROW_CLASSES = {
        "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow",
        "com.oplus.systemui.statusbar.notification.row.ExpandableNotificationRow"
    };

    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) throws Throwable {
        if (!"com.android.systemui".equals(p.packageName) && !"com.oplus.systemui".equals(p.packageName)) return;
        for (String cn : ROW_CLASSES) {
            try {
                Class<?> c = XposedHelpers.findClass(cn, p.classLoader);
                XposedHelpers.findAndHookMethod(c, "onAttachedToWindow", new XC_MethodHook() {
                    @Override protected void afterHookedMethod(MethodHookParam param) {
                        if (param.thisObject instanceof View) apply((View)param.thisObject);
                    }
                });
                return; // 找到一个兼容类即可
            } catch (Throwable ignored) { }
        }
    }

    private void apply(View row) {
        try {
            if (row.getTag(TAG_KEY) != null) return;
            row.setTag(TAG_KEY, Boolean.TRUE);
            GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(70,45,125), Color.rgb(41,38,92), Color.rgb(27,49,105)});
            bg.setCornerRadius(dp(row, 26));
            bg.setStroke(dp(row, 1), Color.rgb(184,159,255));
            row.setBackground(bg);
            // 仅提高正文层次，不修改点击/展开逻辑。
            ArrayDeque<View> q = new ArrayDeque<>(); q.add(row);
            int count = 0;
            while (!q.isEmpty() && count++ < 80) {
                View v = q.remove();
                if (v instanceof TextView) {
                    TextView t = (TextView)v;
                    if (t.getTextSize() > 13) t.setTextColor(Color.WHITE);
                    else t.setTextColor(Color.rgb(210,202,245));
                }
                if (v instanceof android.view.ViewGroup) {
                    android.view.ViewGroup g=(android.view.ViewGroup)v;
                    for(int i=0;i<g.getChildCount();i++) q.add(g.getChildAt(i));
                }
            }
        } catch (Throwable ignored) { }
    }
    private int dp(View v, int x) { return (int)(x * v.getResources().getDisplayMetrics().density + .5f); }
}
