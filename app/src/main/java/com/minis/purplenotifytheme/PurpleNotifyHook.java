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

/** ColorOS 15 紫霞通知外层容器 Hook（实验性但保守：不改点击/展开逻辑）。 */
public class PurpleNotifyHook implements IXposedHookLoadPackage {
    private static final int TAG_KEY = 0x7f0f5a19;
    // 同时命中通知行、可激活外框、实际背景图层；ColorOS 版本间类名不同。
    private static final String[] TARGETS = {
        "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow",
        "com.android.systemui.statusbar.notification.row.ActivatableNotificationView",
        "com.android.systemui.statusbar.notification.row.NotificationBackgroundView",
        "com.oplus.systemui.statusbar.notification.row.ExpandableNotificationRow",
        "com.oplus.systemui.statusbar.notification.row.ActivatableNotificationView",
        "com.oplus.systemui.statusbar.notification.row.NotificationBackgroundView"
    };

    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) {
        if (!"com.android.systemui".equals(p.packageName) && !"com.oplus.systemui".equals(p.packageName)) return;
        for (String name : TARGETS) hookTarget(name, p.classLoader);
    }

    private void hookTarget(String name, ClassLoader loader) {
        try {
            Class<?> c = XposedHelpers.findClass(name, loader);
            XposedHelpers.findAndHookMethod(c, "onAttachedToWindow", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    if (param.thisObject instanceof View) apply((View)param.thisObject);
                }
            });
        } catch (Throwable ignored) { /* 当前系统没有该类，安全跳过 */ }
    }

    private void apply(View v) {
        try {
            if (v.getTag(TAG_KEY) != null) return;
            v.setTag(TAG_KEY, Boolean.TRUE);
            GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(77,47,139), Color.rgb(52,38,105), Color.rgb(26,54,115)});
            bg.setCornerRadius(dp(v, 28));
            bg.setStroke(dp(v, 1), Color.rgb(196,171,255));
            v.setBackground(bg);
            // 仅对当前容器子树做轻量文字对比修正，限制遍历数避免拖慢 SystemUI。
            ArrayDeque<View> q = new ArrayDeque<>(); q.add(v); int seen = 0;
            while (!q.isEmpty() && seen++ < 45) {
                View child = q.remove();
                if (child instanceof TextView) ((TextView) child).setTextColor(Color.rgb(242,238,255));
                if (child instanceof android.view.ViewGroup) {
                    android.view.ViewGroup g=(android.view.ViewGroup)child;
                    for(int i=0;i<g.getChildCount();i++) q.add(g.getChildAt(i));
                }
            }
        } catch (Throwable ignored) { }
    }
    private int dp(View v, int x) { return (int)(x * v.getResources().getDisplayMetrics().density + .5f); }
}
