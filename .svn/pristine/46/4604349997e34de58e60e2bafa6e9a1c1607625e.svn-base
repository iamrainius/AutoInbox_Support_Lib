package com.borqs.ai.service;

import com.borqs.ai.AutoInbox;
import com.borqs.ai.activity.MessageList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoInboxBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            onBootCompleted(context);
        }
    }

    private void onBootCompleted(Context context) {
        if (AutoInbox.DEBUG) {
            Log.d(AutoInbox.TAG, "BOOT_COMPLETED");
        }
        
        AutoInboxService.actionReschedule(context);
    }

}
