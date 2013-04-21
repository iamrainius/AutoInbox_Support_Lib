package com.borqs.ai.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.borqs.ai.AutoInbox;

public class AutoInboxService extends Service {
    private static final String ACTION_CHECK_MESSAGE =
            "com.borqs.ai.intent.action.AI_SERVICE_WAKEUP";
    private static final String ACTION_RESCHEDULE =
            "com.borqs.ai.intent.action.AI_SERVICE_RESCHEDULE";
    private static final String ACTION_CANCEL =
            "com.borqs.ai.intent.action.AI_SERVICE_CANCEL";
    private static final String ACTION_NOTIFY =
            "com.borqs.ai.intent.action.AI_SERVICE_NOTIFY";
    private static final long WATCHDOG_DELAY = 20000;
    
    private static CheckReport mCheckReport = new CheckReport();
    
    public static void actionReschedule(Context context) {
        Intent i = new Intent();
        i.setClass(context, AutoInbox.class);
        i.setAction(AutoInboxService.ACTION_RESCHEDULE);
        context.startService(i);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        
        String action = intent.getAction();
        if (ACTION_CHECK_MESSAGE.equals(action)) {
            // Renew the check report
            
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            
            if (AutoInbox.DEBUG) {
                Log.d(AutoInbox.TAG, "action: check messages");
            }
            
            // Set a watch dog against the process is being killed
            // Normally, the work is done by reschedule()
            setWatchdog(alarmManager);
            
            // Check Messages
            
            // Reschedule
            reschedule(alarmManager);
            stopSelf(startId);
            
        } else if (ACTION_RESCHEDULE.equals(action)) {
            if (AutoInbox.DEBUG) {
                Log.d(AutoInbox.TAG, "action : reschedule");
            }
            refreshCheckReport();
                
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            reschedule(alarmManager);
            stopSelf(startId);
        }
        
        return START_NOT_STICKY;
    }

    private void setWatchdog(AlarmManager alarmManager) {
        PendingIntent pi = createAlarmIntent();
        long timeNow = SystemClock.elapsedRealtime();
        long nextCheckTime = timeNow + WATCHDOG_DELAY;
    }

    /**
     * Update the report for next check
     */
    private void refreshCheckReport() {
        synchronized (mCheckReport) {
            if (mCheckReport.prevCheckTime == 0) {
                mCheckReport.prevCheckTime = SystemClock.elapsedRealtime();
            }
            
            mCheckReport.nextCheckTime = mCheckReport.prevCheckTime + mCheckReport.checkInterval;
        }
    }

    /**
     * This operation firstly calculates the nextTime to alarm,
     * and then set an alarm to execute checking message when timeout
     */
    private void reschedule(AlarmManager alarmManager) {
        long nextTime = Long.MAX_VALUE;
        long timeNow = SystemClock.elapsedRealtime();
        
        long prevCheckTime = mCheckReport.prevCheckTime;
        long nextCheckTime = mCheckReport.nextCheckTime;
        
        if ((prevCheckTime == 0) || (nextCheckTime < timeNow)) {
            // The alarm will be triggered immediately when nextCheckTime is earlier then now
            nextTime = 0;
        } else if (nextCheckTime < nextTime) {
            nextTime = nextCheckTime;
        }
        
        PendingIntent pi = createAlarmIntent();
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTime, pi);
        if (AutoInbox.DEBUG) {
            Log.d(AutoInbox.TAG, "reschedule: alarm set at " + nextTime);
        }
    }

    /**
     * Create a PendingIntent to start service to check messages
     */
    private PendingIntent createAlarmIntent() {
        Intent i = new Intent();
        i.setClass(this, AutoInbox.class);
        i.setAction(ACTION_CHECK_MESSAGE);
        // Watchdog ?
        
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class CheckReport {

        long prevCheckTime;
        long nextCheckTime;
        int numNewMessages;
        long checkInterval;
        
        public CheckReport() {
            prevCheckTime = 0;
            numNewMessages = 0;
            checkInterval = AutoInbox.MESSAGE_CHECK_INTERVAL_SECONDS;
            nextCheckTime = prevCheckTime + checkInterval;
        }

        public CheckReport(CheckReport report) {
            if (report == null) {
                throw new IllegalStateException();
            }
            prevCheckTime = report.prevCheckTime;
            nextCheckTime = report.nextCheckTime;
            numNewMessages = report.numNewMessages;
            checkInterval = report.checkInterval;
        }

        @Override
        public String toString() {
            return "Check report: prevCheck=" + prevCheckTime + " nextCheck=" 
                + nextCheckTime + " numNew=" + numNewMessages + " checkInterval=" + checkInterval;
        }
    }
}
