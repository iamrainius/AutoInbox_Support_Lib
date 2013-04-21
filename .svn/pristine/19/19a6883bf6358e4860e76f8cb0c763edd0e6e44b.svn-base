package com.borqs.ai.widget;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.widget.Toast;

public class WidgetDatabaseObserver extends ContentObserver {
    private Context mContext;
    public String ACTION_UPDATE_WIDGET = "intent.action.UPDATE_WIDGET";

    public WidgetDatabaseObserver(Context context) {
        super(new Handler());
        mContext = context;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onChange(boolean selfChange) {
        // TODO Auto-generated method stub
        super.onChange(selfChange);
        updateWidget();
    }

    private void updateWidget() {
        // TODO Auto-generated method stub
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_WIDGET);
        mContext.sendBroadcast(intent);
    }
}
