package com.minis.purplenotifytheme;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** ColorOS 15 紫霞通知背景主题：对真实 NotificationBackgroundView 最终绘制上色。 */
public class PurpleNotifyHook implements IXposedHookLoadPackage {
    private static final String BG = "com.android.systemui.statusbar.notification.row.NotificationBackgroundView";
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) {
        if (!"com.android.systemui".equals(p.packageName)) return;
        try {
            Class<?> c = XposedHelpers.findClass(BG, p.classLoader);
            XposedHelpers.findAndHookMethod(c, "onDraw", Canvas.class, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam q) {
                    if (q.thisObject instanceof View && q.args != null && q.args.length > 0 && q.args[0] instanceof Canvas)
                        draw((View)q.thisObject, (Canvas)q.args[0]);
                }
            });
            XposedBridge.log("PurpleNotify: NotificationBackgroundView hook active");
        } catch (Throwable e) { XposedBridge.log("PurpleNotify: background hook failed " + e); }
    }
    private void draw(View v, Canvas c) {
        try {
            int w=v.getWidth(), h=v.getHeight(); if(w<20||h<20)return;
            float r=dp(v,28); Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setShader(new LinearGradient(0,0,w,h,new int[]{Color.rgb(100,58,190),Color.rgb(65,48,142),Color.rgb(36,78,160)},null,Shader.TileMode.CLAMP));
            RectF box=new RectF(1,1,w-1,h-1);c.drawRoundRect(box,r,r,p);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(v,1));p.setColor(Color.rgb(214,193,255));
            c.drawRoundRect(box,r,r,p);
        } catch (Throwable ignored) { }
    }
    private float dp(View v,int x){return x*v.getResources().getDisplayMetrics().density;}
}
