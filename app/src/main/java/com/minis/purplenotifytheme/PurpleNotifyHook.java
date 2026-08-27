package com.minis.purplenotifytheme;

import android.view.View;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** 只读诊断版：记录 ColorOS 通知行及其父容器链，不修改任何系统绘制。 */
public class PurpleNotifyHook implements IXposedHookLoadPackage {
    private static final int TAG = 0x7f0f7331;
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) {
        if (!"com.android.systemui".equals(p.packageName)) return;
        try {
            Class<?> row = XposedHelpers.findClass("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", p.classLoader);
            XposedHelpers.findAndHookMethod(row, "onAttachedToWindow", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam q) {
                    if (q.thisObject instanceof View) report((View) q.thisObject);
                }
            });
            XposedBridge.log("PurpleNotifyDiag: hook installed for ExpandableNotificationRow");
        } catch (Throwable e) { XposedBridge.log("PurpleNotifyDiag: hook failed " + e); }
    }
    private void report(View row) {
        try {
            if (row.getTag(TAG) != null) return;
            row.setTag(TAG, Boolean.TRUE);
            StringBuilder s = new StringBuilder("PurpleNotifyDiag ROW=").append(row.getClass().getName())
                .append(" bg=").append(row.getBackground() == null ? "null" : row.getBackground().getClass().getName());
            Object p = row.getParent(); int n = 0;
            while (p instanceof View && n++ < 8) {
                View v = (View) p;
                s.append(" <- ").append(v.getClass().getName())
                 .append("[bg=").append(v.getBackground() == null ? "null" : v.getBackground().getClass().getName()).append("]");
                p = v.getParent();
            }
            XposedBridge.log(s.toString());
        } catch (Throwable e) { XposedBridge.log("PurpleNotifyDiag: report failed " + e); }
    }
}
