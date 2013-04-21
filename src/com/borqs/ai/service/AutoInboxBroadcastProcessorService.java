package com.borqs.ai.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/**
 * Out of design, and will be removed later
 */
public class AutoInboxBroadcastProcessorService extends IntentService {

    private static final String ACTION_BROADCAST = "ai_broadcast_receiver";

    public AutoInboxBroadcastProcessorService() {
        super(AutoInboxBroadcastProcessorService.class.getName());
        // The intent should be redelivered when the process get killed
        setIntentRedelivery(true);
    }
    
    public static void processBroadcastIntent(Context context, Intent broadcastIntent) {
        Intent i = new Intent(context, AutoInboxBroadcastProcessorService.class);
        i.setAction(ACTION_BROADCAST);
        i.putExtra(Intent.EXTRA_INTENT, broadcastIntent);
        context.startService(i);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Called on a worker thread
        
        final String action = intent.getAction();
        if (ACTION_BROADCAST.equals(action)) {
            final Intent broadcastIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
            final String broadcastAction = broadcastIntent.getAction();
            
            if (Intent.ACTION_BOOT_COMPLETED.equals(broadcastAction)) {
                onBootCompleted();
            }
        }
    }

    /**
     * Handle ACTION_BOOT_COMPLETED. Called on a worker thread
     */
    private void onBootCompleted() {
        
    }

}
