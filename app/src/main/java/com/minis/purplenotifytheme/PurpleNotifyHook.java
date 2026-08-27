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

/** ColorOS 15 全局通知主题：卡片背景 + 分组顶部/留白覆盖。 */
public class PurpleNotifyHook implements IXposedHookLoadPackage {
    private static int[] colors = {Color.rgb(100,58,190),Color.rgb(65,48,142),Color.rgb(36,78,160)};
    private static int[] groupColors = {Color.rgb(122,78,205),Color.rgb(88,64,174),Color.rgb(57,101,184)};
    private static int edge = Color.rgb(214,193,255);
    private static final String BG = "com.android.systemui.statusbar.notification.row.NotificationBackgroundView";
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) {
        if (!"com.android.systemui".equals(p.packageName)) return;
        loadTheme();
        try {
            Class<?> c=XposedHelpers.findClass(BG,p.classLoader);
            XposedHelpers.findAndHookMethod(c,"onDraw",Canvas.class,new XC_MethodHook(){
                @Override protected void afterHookedMethod(MethodHookParam q){if(q.thisObject instanceof View&&q.args!=null&&q.args.length>0&&q.args[0] instanceof Canvas) drawCard((View)q.thisObject,(Canvas)q.args[0]);}
            });
            // 分组顶部白色摘要区属于父通知行；dispatchDraw结束后轻覆盖整组。
            XposedHelpers.findAndHookMethod(ViewGroup.class,"dispatchDraw",Canvas.class,new XC_MethodHook(){
                @Override protected void afterHookedMethod(MethodHookParam q){
                    if(q.thisObject instanceof ViewGroup&&q.args!=null&&q.args.length>0&&q.args[0] instanceof Canvas){
                        ViewGroup v=(ViewGroup)q.thisObject;
                        if(v.getClass().getName().contains("ExpandableNotificationRow")&&hasGroup(v)) drawGroup(v,(Canvas)q.args[0]);
                    }
                }});
            XposedBridge.log("PurpleNotify v6 active");
        } catch(Throwable e){XposedBridge.log("PurpleNotify hook failed "+e);}
    }
    private boolean hasGroup(ViewGroup root){
        // ColorOS 会把 ChildrenContainer 包在多层 content wrapper 内，递归查找。
        return hasGroupDeep(root, 0);
    }
    private boolean hasGroupDeep(View v, int depth){
        if(depth > 8) return false;
        if(v.getClass().getName().contains("NotificationChildrenContainer")) return true;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++) if(hasGroupDeep(g.getChildAt(i),depth+1)) return true;
        }
        return false;
    }
    private void loadTheme(){
        try {
            XSharedPreferences p=new XSharedPreferences("com.minis.purplenotifytheme","settings");p.reload();String t=p.getString("theme","purple");
            if("glass".equals(t)){colors=new int[]{Color.rgb(32,148,222),Color.rgb(26,93,168),Color.rgb(27,62,128)};groupColors=new int[]{Color.rgb(74,180,238),Color.rgb(46,125,197),Color.rgb(31,77,145)};edge=Color.rgb(150,230,255);}
            else if("terminal".equals(t)){colors=new int[]{Color.rgb(7,63,72),Color.rgb(10,39,52),Color.rgb(7,27,39)};groupColors=new int[]{Color.rgb(12,86,89),Color.rgb(8,55,64),Color.rgb(5,37,48)};edge=Color.rgb(61,235,181);}
        }catch(Throwable ignored){}
    }
    private void drawCard(View v,Canvas c){
        try{int w=v.getWidth(),h=v.getHeight();if(w<20||h<20)return;Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setShader(new LinearGradient(0,0,w,h,colors,null,Shader.TileMode.CLAMP));RectF b=new RectF(0,0,w,h);c.drawRoundRect(b,dp(v,27),dp(v,27),p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(v,1.5f));p.setColor(edge);c.drawRoundRect(new RectF(1,1,w-1,h-1),dp(v,27),dp(v,27),p);}catch(Throwable ignored){}
    }
    private void drawGroup(View v,Canvas c){
        try{int w=v.getWidth(),h=v.getHeight();if(w<20||h<20)return;Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setAlpha(118);p.setShader(new LinearGradient(0,0,w,h,groupColors,null,Shader.TileMode.CLAMP));c.drawRoundRect(new RectF(0,0,w,h),dp(v,30),dp(v,30),p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setAlpha(180);p.setStrokeWidth(dp(v,1));p.setColor(edge);c.drawRoundRect(new RectF(1,1,w-1,h-1),dp(v,30),dp(v,30),p);}catch(Throwable ignored){}
    }
    private float dp(View v,float x){return x*v.getResources().getDisplayMetrics().density;}
}
