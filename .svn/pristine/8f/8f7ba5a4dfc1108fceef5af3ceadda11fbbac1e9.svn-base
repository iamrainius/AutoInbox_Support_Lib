package com.borqs.ai.widget;

import com.borqs.ai.R;
import com.borqs.ai.activity.AutoInboxActivity;
import com.borqs.ai.activity.MessageList;
import com.borqs.ai.provider.AutoInboxContent.Message;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

public class AutoInboxWidgetProvider extends AppWidgetProvider {

    String ACTION_UPDATE_WIDGET = "intent.action.UPDATE_WIDGET";
    String ACTION_VIEW_MESSAGE = "intent.action.VIEW_MESSAGE";
    Message mMessage;
    static final ComponentName THIS_APPWIDGET = new ComponentName(
            "com.borqs.ai", "com.borqs.ai.widget.AutoInboxWidgetProvider");
    private static final String[] MESSAGE_LIST_PROJECTION = { "max(_id)" };

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // TODO Auto-generated method stub
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        // TODO Auto-generated method stub
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        // TODO Auto-generated method stub
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_UPDATE_WIDGET)) {
            updateWidgetUI(context);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        // TODO Auto-generated method stub
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.autoinbox_widget_init);
        Intent intent = new Intent();
        intent.setClass(context, MessageList.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, 0);
        views.setOnClickPendingIntent(R.id.auto_init_layout, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, views);
        updateWidgetUI(context);

    }

    public Message getNewMessage(Context context) {
        String selection = null;
        Message message = null;
        Cursor c = context.getContentResolver().query(Message.CONTENT_URI,
                MESSAGE_LIST_PROJECTION, selection, null, null);
        if (c != null) {
            c.moveToFirst();
            message = Message.restoreMessageWithId(context, c.getLong(0));
            c.close();
        }
        return message;

    }

    public void updateWidgetUI(Context context) {
        Message message = getNewMessage(context);
        RemoteViews widgetViews = null;
        if (message != null) {
            widgetViews = new RemoteViews(context.getPackageName(),
                    R.layout.autoinbox_widget);
            widgetViews.setTextViewText(R.id.widget_message_content,
                    message.mBody);
        } else {
            widgetViews = new RemoteViews(context.getPackageName(),
                    R.layout.autoinbox_widget_init);
        }
        Intent intent = new Intent();
        intent.setClass(context, AutoInboxActivity.class);
        if (message != null) {
            intent.putExtra(AutoInboxActivity.SELECTED_MESSAGE_TYPE, message.mType);
            intent.setAction(ACTION_VIEW_MESSAGE);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);
            widgetViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
        } else {
            intent.setAction(ACTION_VIEW_MESSAGE);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);
            widgetViews.setOnClickPendingIntent(R.id.auto_init_layout, pendingIntent);
        }

        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        gm.updateAppWidget(THIS_APPWIDGET, widgetViews);
    }

}
