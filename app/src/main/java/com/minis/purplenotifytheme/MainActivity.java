package com.minis.purplenotifytheme;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.graphics.Color;

public class MainActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b);TextView t=new TextView(this);t.setPadding(48,64,48,48);t.setTextColor(Color.WHITE);t.setTextSize(17);t.setText("紫霞全局通知主题\n\n这是 LSPosed 模块。\n\n请到 LSPosed → 模块 → 紫霞全局通知主题 → 仅勾选 系统界面 / com.android.systemui，然后重启 SystemUI 或重启手机。\n\n如状态栏反复崩溃：进入 LSPosed 禁用模块后重启。");setContentView(t);}
}
