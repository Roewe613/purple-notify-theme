package com.minis.purplenotifytheme;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** ColorOS 15 淡粉紫玻璃通知主题：子卡片与分组头部统一背景。 */
public class PurpleNotifyHook implements IXposedHookLoadPackage {
    // 通知枢纽同款浅薰衣草玻璃蓝：暖白紫 → 淡紫 → 雾蓝。
    private static final int[] COLORS={Color.rgb(245,241,255),Color.rgb(229,224,255),Color.rgb(203,217,246)};
    private static final int EDGE=Color.rgb(255,255,255);
    private static final int TAG=0x7f0f7a55;
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p){
        if(!"com.android.systemui".equals(p.packageName))return;
        try{
            Class<?> bg=XposedHelpers.findClass("com.android.systemui.statusbar.notification.row.NotificationBackgroundView",p.classLoader);
            XposedHelpers.findAndHookMethod(bg,"onDraw",Canvas.class,new XC_MethodHook(){
                @Override protected void afterHookedMethod(MethodHookParam q){if(q.thisObject instanceof View&&q.args!=null&&q.args.length>0&&q.args[0] instanceof Canvas)drawCard((View)q.thisObject,(Canvas)q.args[0]);}
            });
            Class<?> row=XposedHelpers.findClass("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow",p.classLoader);
            XposedHelpers.findAndHookMethod(row,"onLayout",boolean.class,int.class,int.class,int.class,int.class,new XC_MethodHook(){
                @Override protected void afterHookedMethod(MethodHookParam q){if(q.thisObject instanceof ViewGroup)styleGroup((ViewGroup)q.thisObject);}
            });
            XposedBridge.log("PastelNotify v6.7 active");
        }catch(Throwable e){XposedBridge.log("PastelNotify hook failed "+e);}
    }
    private void styleGroup(ViewGroup row){
        try{
            if(!hasChildrenGroup(row))return;
            // 父行背景只在分组中可见的头部及留白区域出现，不覆盖子通知文字。
            GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,COLORS);
            bg.setCornerRadius(dp(row,30));bg.setStroke((int)dp(row,1),EDGE);
            row.setBackground(bg);
            hideShadow(row);
        }catch(Throwable ignored){}
    }
    private boolean hasChildrenGroup(View v){
        if(v.getClass().getName().contains("NotificationChildrenContainer"))return true;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(hasChildrenGroup(g.getChildAt(i)))return true;}
        return false;
    }
    private void hideShadow(View v){
        if(v.getClass().getName().contains("FakeShadowView")){v.setAlpha(0f);return;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)hideShadow(g.getChildAt(i));}
    }
    private void drawCard(View v,Canvas c){
        try{int w=v.getWidth(),h=v.getHeight();if(w<20||h<20)return;Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setShader(new LinearGradient(0,0,w,h,COLORS,null,Shader.TileMode.CLAMP));RectF b=new RectF(0,0,w,h);c.drawRoundRect(b,dp(v,27),dp(v,27),p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(v,1.2f));p.setColor(EDGE);c.drawRoundRect(new RectF(1,1,w-1,h-1),dp(v,27),dp(v,27),p);}catch(Throwable ignored){}}
    private float dp(View v,float x){return x*v.getResources().getDisplayMetrics().density;}
}
