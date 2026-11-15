package com.wakeup.alarm;
import android.app.*; import android.content.*; import android.content.pm.PackageManager; import android.media.*;
import android.net.Uri; import android.os.*; import android.provider.Settings; import android.view.WindowManager; import android.webkit.*;
public class MainActivity extends Activity {
  WebView wv; MediaPlayer mp; Vibrator vib; boolean ringing=false, ready=false; String pend;
  class Bridge {
    @JavascriptInterface public void schedule(String json){ Scheduler.save(MainActivity.this,json); Scheduler.scheduleNext(MainActivity.this); }
    @JavascriptInterface public void sound(final boolean on){ runOnUiThread(new Runnable(){ public void run(){ if(on) startSound(); else stopSound(); }}); }
    @JavascriptInterface public void ring(final boolean r){ ringing=r; runOnUiThread(new Runnable(){ public void run(){
      if(r){ ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(1); getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); }
      else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); }}); }
    @JavascriptInterface public String pending(){ String s=pend==null?"":pend; pend=null; return s; }
    @JavascriptInterface public void setup(){ runOnUiThread(new Runnable(){ public void run(){ askSetup(); }}); }
  }
  void askSetup(){
    try{
      PowerManager pm=(PowerManager)getSystemService(POWER_SERVICE);
      NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
      if(!pm.isIgnoringBatteryOptimizations(getPackageName())){
        startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,Uri.parse("package:"+getPackageName())));
      } else if(Build.VERSION.SDK_INT>=34 && !nm.canUseFullScreenIntent()){
        startActivity(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,Uri.parse("package:"+getPackageName())));
      } else {
        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));
      }
    }catch(Exception e){}
  }
  void startSound(){
    stopSound();
    try{
      AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE);
      am.setStreamVolume(AudioManager.STREAM_ALARM,am.getStreamMaxVolume(AudioManager.STREAM_ALARM),0);
      Uri u=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
      if(u==null) u=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
      mp=new MediaPlayer();
      mp.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
      mp.setDataSource(this,u); mp.setLooping(true); mp.prepare(); mp.start();
    }catch(Exception e){}
    try{ vib=(Vibrator)getSystemService(VIBRATOR_SERVICE); vib.vibrate(VibrationEffect.createWaveform(new long[]{0,500,300},0)); }catch(Exception e){}
  }
  void stopSound(){
    if(mp!=null){ try{ mp.stop(); }catch(Exception e){} mp.release(); mp=null; }
    if(vib!=null){ vib.cancel(); }
  }
  void take(Intent i){
    if(i!=null && i.getBooleanExtra("ring",false)){
      String l=i.getStringExtra("label"), t=i.getStringExtra("time");
      pend=(l==null?"":l)+"|"+(t==null?"":t);
      i.removeExtra("ring");
    }
  }
  @Override protected void onCreate(Bundle b){
    super.onCreate(b);
    if(Build.VERSION.SDK_INT>=27){ setShowWhenLocked(true); setTurnScreenOn(true); }
    if(Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED)
      requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},1);
    wv=new WebView(this); setContentView(wv);
    WebSettings s=wv.getSettings();
    s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setMediaPlaybackRequiresUserGesture(false);
    wv.addJavascriptInterface(new Bridge(),"Android");
    wv.setWebViewClient(new WebViewClient(){ @Override public void onPageFinished(WebView v,String u){
      ready=true; if(pend!=null) v.evaluateJavascript("window.nativeRing&&nativeRing()",null); }});
    take(getIntent());
    wv.loadUrl("file:///android_asset/index.html");
  }
  @Override protected void onNewIntent(Intent i){
    super.onNewIntent(i); setIntent(i); take(i);
    if(ready && pend!=null) wv.evaluateJavascript("window.nativeRing&&nativeRing()",null);
  }
  @Override public void onBackPressed(){ if(!ringing) super.onBackPressed(); }
  @Override protected void onDestroy(){ stopSound(); super.onDestroy(); }
}
