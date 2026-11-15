package com.wakeup.alarm;
import android.app.*; import android.content.*; import org.json.*; import java.util.*;
public class Scheduler {
  static final String PREF="wk", KEY="alarms";
  static void save(Context c,String json){ c.getSharedPreferences(PREF,0).edit().putString(KEY,json).apply(); }
  static void scheduleNext(Context c){
    AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
    long best=Long.MAX_VALUE; String bl="", bt="";
    try{
      JSONArray a=new JSONObject(c.getSharedPreferences(PREF,0).getString(KEY,"{\"alarms\":[]}")).getJSONArray("alarms");
      long now=System.currentTimeMillis();
      for(int n=0;n<a.length();n++){
        JSONObject o=a.getJSONObject(n);
        if(!o.optBoolean("on")) continue;
        String[] hm=o.getString("time").split(":");
        JSONArray d=o.optJSONArray("days");
        for(int k=0;k<8;k++){
          Calendar cal=Calendar.getInstance();
          cal.add(Calendar.DAY_OF_YEAR,k);
          cal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(hm[0]));
          cal.set(Calendar.MINUTE,Integer.parseInt(hm[1]));
          cal.set(Calendar.SECOND,0); cal.set(Calendar.MILLISECOND,0);
          if(cal.getTimeInMillis()<=now) continue;
          int dow=cal.get(Calendar.DAY_OF_WEEK)-1;
          if(d!=null && d.length()>0){ boolean m=false; for(int x=0;x<d.length();x++) if(d.getInt(x)==dow) m=true; if(!m) continue; }
          if(cal.getTimeInMillis()<best){ best=cal.getTimeInMillis(); bl=o.optString("label"); bt=o.getString("time"); }
          break;
        }
      }
    }catch(Exception e){}
    Intent i=new Intent(c,AlarmReceiver.class);
    if(best==Long.MAX_VALUE){
      PendingIntent old=PendingIntent.getBroadcast(c,1,i,PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);
      if(old!=null) am.cancel(old);
      return;
    }
    i.putExtra("label",bl); i.putExtra("time",bt);
    PendingIntent pi=PendingIntent.getBroadcast(c,1,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    PendingIntent show=PendingIntent.getActivity(c,2,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    am.setAlarmClock(new AlarmManager.AlarmClockInfo(best,show),pi);
  }
}
