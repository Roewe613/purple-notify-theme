package com.minis.purplenotifytheme;

import android.view.View;
import android.view.ViewGroup;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** 只读分组布局诊断：定位 ColorOS 分组头部、留白、分割线的真实 View 边界。 */
public class PurpleNotifyHook implements IXposedHookLoadPackage {
    private static final int TAG=0x7f0f7442;
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p){
        if(!"com.android.systemui".equals(p.packageName))return;
        try{
            Class<?> row=XposedHelpers.findClass("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow",p.classLoader);
            XposedHelpers.findAndHookMethod(row,"onAttachedToWindow",new XC_MethodHook(){
                @Override protected void afterHookedMethod(MethodHookParam q){if(q.thisObject instanceof View)report((View)q.thisObject);}
            });
            XposedBridge.log("PurpleGroupDiag active");
        }catch(Throwable e){XposedBridge.log("PurpleGroupDiag failed "+e);}
    }
    private void report(View root){
        try{
            if(root.getTag(TAG)!=null)return;root.setTag(TAG,Boolean.TRUE);
            StringBuilder s=new StringBuilder("PurpleGroupDiag ROOT ");walk(root,s,0,0);XposedBridge.log(s.toString());
        }catch(Throwable e){XposedBridge.log("PurpleGroupDiag error "+e);}
    }
    private void walk(View v,StringBuilder s,int depth,int index){
        if(depth>5)return;
        String n=v.getClass().getName();
        boolean important=n.contains("Notification")||n.contains("Header")||n.contains("Divider")||n.contains("Row")||depth<2;
        if(important)s.append("\n d").append(depth).append("[").append(index).append("] ").append(n)
          .append(" x=").append(v.getLeft()).append(" y=").append(v.getTop()).append(" w=").append(v.getWidth()).append(" h=").append(v.getHeight())
          .append(" bg=").append(v.getBackground()==null?"null":v.getBackground().getClass().getSimpleName());
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)walk(g.getChildAt(i),s,depth+1,i);}
    }
}
