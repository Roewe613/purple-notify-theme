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
 * ColorOS 15 紫霞全局通知蒙层。
 * 直接在 ExpandableNotificationRow 所有子内容绘制完成后叠加半透明色层，
 * 因此可覆盖 OPlus 最外层白色玻璃容器；不拦截任何触摸/点击/展开逻辑。
 */
public class PurpleNotifyHook implements IXposedHookLoadPackage {
    private static final String ROW = "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow";
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) {
        if (!"com.android.systemui".equals(p.packageName)) return;
        try {
            Class<?> c = XposedHelpers.findClass(ROW, p.classLoader);
            XposedHelpers.findAndHookMethod(c, "dispatchDraw", Canvas.class, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    if (param.thisObject instanceof View && param.args != null && param.args.length > 0 && param.args[0] instanceof Canvas)
                        overlay((View)param.thisObject, (Canvas)param.args[0]);
                }
            });
        } catch (Throwable ignored) { }
    }
    private void overlay(View v, Canvas c) {
        try {
            int w=v.getWidth(), h=v.getHeight(); if(w<20||h<20)return;
            float r=dp(v,28); Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setAlpha(125); // 保留原始文字与图标可读性，同时覆盖白色外框。
            p.setShader(new LinearGradient(0,0,w,h,new int[]{Color.rgb(123,72,210),Color.rgb(84,55,164),Color.rgb(54,93,177)},null,Shader.TileMode.CLAMP));
            c.drawRoundRect(new RectF(1,1,w-1,h-1),r,r,p);
            p.setAlpha(220);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(v,1));p.setColor(Color.rgb(220,198,255));
            c.drawRoundRect(new RectF(1,1,w-1,h-1),r,r,p);
        } catch (Throwable ignored) { }
    }
    private float dp(View v,int n){return n*v.getResources().getDisplayMetrics().density;}
}
