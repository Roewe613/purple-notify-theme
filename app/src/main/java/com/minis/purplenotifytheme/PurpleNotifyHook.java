package com.minis.purplenotifytheme;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import android.view.ViewGroup;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** ColorOS 15 紫霞全局通知外框：Hook 继承自 ViewGroup 的最终绘制方法。 */
public class PurpleNotifyHook implements IXposedHookLoadPackage {
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) {
        if (!"com.android.systemui".equals(p.packageName)) return;
        try {
            // dispatchDraw 在 ViewGroup 中声明，不能只从 ExpandableNotificationRow 子类查找。
            XposedHelpers.findAndHookMethod(ViewGroup.class, "dispatchDraw", Canvas.class, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    if (!(param.thisObject instanceof View) || param.args == null || param.args.length == 0 || !(param.args[0] instanceof Canvas)) return;
                    View v = (View)param.thisObject;
                    String name = v.getClass().getName();
                    if (name.contains("ExpandableNotificationRow")) overlay(v, (Canvas)param.args[0]);
                }
            });
        } catch (Throwable ignored) { }
    }
    private void overlay(View v, Canvas c) {
        try {
            int w=v.getWidth(), h=v.getHeight(); if(w<20||h<20)return;
            float r=dp(v,28); Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
            // 半透明紫霞层覆盖ColorOS白色通知容器，同时保留文字和图标。
            p.setAlpha(105);
            p.setShader(new LinearGradient(0,0,w,h,new int[]{Color.rgb(135,75,220),Color.rgb(88,57,178),Color.rgb(41,87,180)},null,Shader.TileMode.CLAMP));
            RectF rect=new RectF(1,1,w-1,h-1); c.drawRoundRect(rect,r,r,p);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(v,1));p.setAlpha(235);p.setColor(Color.rgb(225,205,255));
            c.drawRoundRect(rect,r,r,p);
        } catch (Throwable ignored) { }
    }
    private float dp(View v,int n){return n*v.getResources().getDisplayMetrics().density;}
}
