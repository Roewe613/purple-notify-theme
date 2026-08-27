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
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * ColorOS 15.001.028 定向紫霞通知主题。
 * Hook 真实 NotificationBackgroundView 的 onDraw，在系统白色背景绘制后叠加紫霞层；
 * 不改通知点击、展开、滑动、内容布局。
 */
public class PurpleNotifyHook implements IXposedHookLoadPackage {
    private static final String BG = "com.android.systemui.statusbar.notification.row.NotificationBackgroundView";
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) {
        if (!"com.android.systemui".equals(p.packageName)) return;
        try {
            Class<?> c = XposedHelpers.findClass(BG, p.classLoader);
            XposedHelpers.findAndHookMethod(c, "onDraw", Canvas.class, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    if (!(param.thisObject instanceof View) || param.args == null || param.args.length == 0 || !(param.args[0] instanceof Canvas)) return;
                    drawPurple((View)param.thisObject, (Canvas)param.args[0]);
                }
            });
        } catch (Throwable ignored) { }
    }
    private void drawPurple(View v, Canvas c) {
        try {
            int w=v.getWidth(), h=v.getHeight(); if(w<10||h<10)return;
            float r=dp(v,28); Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setShader(new LinearGradient(0,0,w,h,new int[]{Color.rgb(84,51,151),Color.rgb(55,40,111),Color.rgb(27,54,118)},null,Shader.TileMode.CLAMP));
            RectF rect=new RectF(1,1,w-1,h-1); c.drawRoundRect(rect,r,r,p);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(v,1));p.setColor(Color.rgb(200,178,255));
            c.drawRoundRect(rect,r,r,p);
        } catch (Throwable ignored) { }
    }
    private float dp(View v,int n){return n*v.getResources().getDisplayMetrics().density;}
}
