package com.wakeup.alarm;
import android.content.*;
public class BootReceiver extends BroadcastReceiver {
  @Override public void onReceive(Context c,Intent i){ Scheduler.scheduleNext(c); }
}
