package com.minis.purplenotifytheme;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** ColorOS 15 紫霞通知外框：通知行 + 分组容器前景蒙层。 */
public class PurpleNotifyHook implements IXposedHookLoadPackage {
    private static final int TAG_KEY = 0x7f0f6a22;
    private static final String[] TARGETS = {
        "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow",
        "com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer"
    };
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) {
        if (!"com.android.systemui".equals(p.packageName)) return;
        for (String name : TARGETS) {
            try {
                Class<?> c=XposedHelpers.findClass(name,p.classLoader);
                XposedHelpers.findAndHookMethod(c,"onAttachedToWindow",new XC_MethodHook(){
                    @Override protected void afterHookedMethod(MethodHookParam q){if(q.thisObject instanceof View)apply((View)q.thisObject);}
                });
            } catch(Throwable ignored) { }
        }
    }
    private void apply(View v){
        try {
            if(v.getTag(TAG_KEY)!=null)return;v.setTag(TAG_KEY,Boolean.TRUE);
            GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(112,72,190),Color.rgb(76,57,157),Color.rgb(48,88,165)});
            d.setAlpha(78);d.setCornerRadius(dp(v,28));d.setStroke(dp(v,1),Color.rgb(212,190,255));
            // foreground 在所有 ColorOS 白色背景及子内容之后绘制，能命中分组外框。
            v.setForeground(d);
        }catch(Throwable ignored){}
    }
    private int dp(View v,int x){return(int)(x*v.getResources().getDisplayMetrics().density+.5f);}
}
