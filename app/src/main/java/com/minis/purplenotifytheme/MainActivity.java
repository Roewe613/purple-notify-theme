package com.minis.purplenotifytheme;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.*;
import android.view.*;
import java.io.File;

/** 紫霞全局通知主题设置页。 */
public class MainActivity extends Activity {
    private LinearLayout root;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(42,64,42,42);
        root.setBackgroundColor(Color.rgb(16,20,35));
        TextView title = text("全局通知主题",24,Color.WHITE); root.addView(title);
        TextView tip = text("仅作用于 SystemUI 通知外框。选择后重启手机或重启 SystemUI 生效。",14,Color.rgb(190,200,225));
        tip.setPadding(0,18,0,24); root.addView(tip);
        addTheme("紫霞", "purple", Color.rgb(106,65,190));
        addTheme("玻璃蓝", "glass", Color.rgb(30,121,190));
        addTheme("终端深色", "terminal", Color.rgb(20,71,78));
        TextView warn = text("提示：若通知栏异常，请在 LSPosed 禁用本模块后重启。",13,Color.rgb(255,190,120));
        warn.setPadding(0,28,0,0); root.addView(warn);
        setContentView(root);
    }
    private TextView text(String s,int size,int color){ TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);return v; }
    private void addTheme(String label,String id,int color) {
        Button b = new Button(this); b.setText(label); b.setTextColor(Color.WHITE); b.setTextSize(17); b.setBackgroundColor(color);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,120);lp.setMargins(0,10,0,10);root.addView(b,lp);
        b.setOnClickListener(v -> saveTheme(id,label));
    }
    private void saveTheme(String id,String label) {
        getSharedPreferences("settings", MODE_PRIVATE).edit().putString("theme",id).commit();
        // 供 LSPosed / SystemUI 跨进程读取；失败时仍保留本机设置。
        try { new File(getApplicationInfo().dataDir+"/shared_prefs/settings.xml").setReadable(true,false); } catch(Exception e) {}
        Toast.makeText(this,"已选择 "+label+"，重启手机或SystemUI后生效",Toast.LENGTH_LONG).show();
    }
}
