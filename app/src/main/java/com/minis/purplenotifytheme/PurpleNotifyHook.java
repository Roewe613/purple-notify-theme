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
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** ColorOS 15 通知主题：紫霞背景并移除 FakeShadowView 白色外描边。 */
public class PurpleNotifyHook implements IXposedHookLoadPackage {
    private static int[] colors={Color.rgb(100,58,190),Color.rgb(65,48,142),Color.rgb(36,78,160)};
    private static int edge=Color.rgb(214,193,255);
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p){
        if(!"com.android.systemui".equals(p.packageName))return;
        loadTheme();
        try{
            Class<?> bg=XposedHelpers.findClass("com.android.systemui.statusbar.notification.row.NotificationBackgroundView",p.classLoader);
            XposedHelpers.findAndHookMethod(bg,"onDraw",Canvas.class,new XC_MethodHook(){
                @Override protected void afterHookedMethod(MethodHookParam q){if(q.thisObject instanceof View&&q.args!=null&&q.args.length>0&&q.args[0] instanceof Canvas)draw((View)q.thisObject,(Canvas)q.args[0]);}
            });
            Class<?> row=XposedHelpers.findClass("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow",p.classLoader);
            XposedHelpers.findAndHookMethod(row,"onLayout",boolean.class,int.class,int.class,int.class,int.class,new XC_MethodHook(){
                @Override protected void afterHookedMethod(MethodHookParam q){if(q.thisObject instanceof ViewGroup) hideSystemShadow((ViewGroup)q.thisObject);}
            });
            XposedBridge.log("PurpleNotify v6.5 active: FakeShadow disabled");
        }catch(Throwable e){XposedBridge.log("PurpleNotify v6.5 hook failed "+e);}
    }
    private void hideSystemShadow(ViewGroup root){
        try{
            for(int i=0;i<root.getChildCount();i++){
                View v=root.getChildAt(i);
                if(v.getClass().getName().contains("FakeShadowView")) v.setVisibility(View.GONE);
            }
        }catch(Throwable ignored){}
    }
    private void loadTheme(){
        try{XSharedPreferences p=new XSharedPreferences("com.minis.purplenotifytheme","settings");p.reload();String t=p.getString("theme","purple");
            if("glass".equals(t)){colors=new int[]{Color.rgb(32,148,222),Color.rgb(26,93,168),Color.rgb(27,62,128)};edge=Color.rgb(150,230,255);}
            else if("terminal".equals(t)){colors=new int[]{Color.rgb(7,63,72),Color.rgb(10,39,52),Color.rgb(7,27,39)};edge=Color.rgb(61,235,181);}
        }catch(Throwable ignored){}
    }
    private void draw(View v,Canvas c){
        try{int w=v.getWidth(),h=v.getHeight();if(w<20||h<20)return;Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setShader(new LinearGradient(0,0,w,h,colors,null,Shader.TileMode.CLAMP));RectF b=new RectF(0,0,w,h);c.drawRoundRect(b,dp(v,27),dp(v,27),p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(v,1.2f));p.setColor(edge);c.drawRoundRect(new RectF(1,1,w-1,h-1),dp(v,27),dp(v,27),p);}catch(Throwable ignored){}}
    private float dp(View v,float x){return x*v.getResources().getDisplayMetrics().density;}
}
