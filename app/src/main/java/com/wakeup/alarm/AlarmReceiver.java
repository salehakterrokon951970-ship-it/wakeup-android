package com.wakeup.alarm;
import android.app.*; import android.content.*; import android.media.*; import android.net.Uri;
public class AlarmReceiver extends BroadcastReceiver {
  @Override public void onReceive(Context c,Intent in){
    String label=in.getStringExtra("label"), time=in.getStringExtra("time");
    Scheduler.scheduleNext(c);
    Intent a=new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
    a.putExtra("ring",true); a.putExtra("label",label==null?"":label); a.putExtra("time",time==null?"":time);
    PendingIntent pi=PendingIntent.getActivity(c,3,a,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
    Uri snd=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    NotificationChannel ch=new NotificationChannel("ring","Alarm",NotificationManager.IMPORTANCE_HIGH);
    ch.setSound(snd,new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
    ch.enableVibration(true);
    nm.createNotificationChannel(ch);
    Notification n=new Notification.Builder(c,"ring")
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle(label==null||label.isEmpty()?"Wake up!":label)
      .setContentText("Tap to solve the challenges and stop the alarm")
      .setCategory(Notification.CATEGORY_ALARM)
      .setFullScreenIntent(pi,true).setContentIntent(pi).setOngoing(true).build();
    n.flags|=Notification.FLAG_INSISTENT;
    nm.notify(1,n);
    try{ c.startActivity(a); }catch(Exception e){}
  }
}
