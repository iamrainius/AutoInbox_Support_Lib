package com.borqs.ai.provider;

import com.borqs.ai.AutoInbox;
import com.borqs.ai.provider.AutoInboxContent.Report;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public abstract class AutoInboxContent {
    public static final String AUTHORITY = AutoInboxProvider.AUTOINBOX_AUTHORITY;
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    // All sub types share this
    public static final String RECORD_ID = "_id";
    
    // ID for newly created instances
    private static final int NOT_SAVED = -1;
    
    public long mId = NOT_SAVED;
    
    public Uri mBaseUri;
    private Uri mUri = null;
    
    // Write an intance's content to a ContentValues
    public abstract ContentValues toContentValues();
    // Read the content from a cursor
    public abstract <T extends AutoInboxContent> T restore(Cursor cursor);
    
    // The T class must have a non-param constructor
    @SuppressWarnings("unchecked")
    public static <T extends AutoInboxContent> T getContent(Cursor cursor, Class<T> klass) {
        try {
            T content = klass.newInstance();
            content.mId = cursor.getLong(0);
            return (T) content.restore(cursor);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Lazy initialize for mUri: content://com.borqs.ai.provider/<subType>/<ID>
    public Uri getUri() {
        if (mUri == null) {
            mUri = ContentUris.withAppendedId(mBaseUri, mId);
        }
        return mUri;
    }
    
    public boolean isSaved() {
        return mId != NOT_SAVED;
    }
    
    public Uri save(Context context) {
        if (isSaved()) {
            throw new UnsupportedOperationException();
        }
        
        Uri result = context.getContentResolver().insert(mBaseUri, toContentValues());
        mId = Long.parseLong(result.getPathSegments().get(1));
        return result;
    }
    
    public interface MessageColumns {
        public static final String ID = RECORD_ID;
        public static final String SUBJECT = "subject";
        public static final String BODY = "body";
        public static final String SENDER_ID = "senderId";
        public static final String SENDER_NAME = "senderName";
        public static final String TYPE = "type";
        public static final String SERVER_ID = "serverId";
        public static final String FLAG_READ = "flagRead";
        public static final String TIMESTAMP = "timeStamp";
        
    }
    
    public static final class Message extends AutoInboxContent implements MessageColumns {
        public static final String TABLE_NAME = "Message";
        
        public static final Uri CONTENT_URI = 
                Uri.parse(AutoInboxContent.CONTENT_URI + "/message");
        
        public String mSubject;
        public String mBody;
        public String mSenderId;
        public String mSenderName;
        public int mType;
        public long mServerId;
        public int mFlagRead = 0;
        public long mTimeStamp;
        
        public static final int UNREAD = 0;
        public static final int READ = 1;

        public static final int CONTENT_ID_COLUMN = 0;
        public static final int CONTENT_SUBJECT_COLUMN = 1;
        public static final int CONTENT_BODY_COLUMN = 2;
        public static final int CONTENT_SENDER_ID_COLUMN = 3;
        public static final int CONTENT_SENDER_NAME_COLUMN = 4;
        public static final int CONTENT_TYPE_COLUMN = 5;
        public static final int CONTENT_SERVER_ID_COLUMN = 6;
        public static final int CONTENT_FLAG_READ_COLUMN = 7;
        public static final int CONTENT_TIMESTAMP_COLUMN = 8;
        
        public static final int TYPE_MESSAGE_UNSUPPORTED = -1;
        
        public static final int TYPE_MESSAGE_SYSTEM = 1;
        public static final int TYPE_MESSAGE_4S = 2;
        public static final int TYPE_MESSAGE_STORES = 3;
        
        private static final String[] CONTENT_PROJECTION = {
            RECORD_ID,
            MessageColumns.SUBJECT, MessageColumns.BODY, MessageColumns.SENDER_ID, MessageColumns.SENDER_NAME,
            MessageColumns.TYPE, MessageColumns.SERVER_ID, MessageColumns.FLAG_READ, MessageColumns.TIMESTAMP
        };

        public static final long NO_MESSAGE = -1;

        public Message() {
            mBaseUri = CONTENT_URI;
        }
        
        @Override
        public ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            
            values.put(MessageColumns.SUBJECT, mSubject);
            values.put(MessageColumns.BODY, mBody);
            values.put(MessageColumns.SENDER_ID, mSenderId);
            values.put(MessageColumns.SENDER_NAME, mSenderName);
            values.put(MessageColumns.TYPE, mType);
            values.put(MessageColumns.SERVER_ID, mServerId);
            values.put(MessageColumns.FLAG_READ, mFlagRead);
            values.put(MessageColumns.TIMESTAMP, mTimeStamp);
            return values;
        }
        
        public static Message restoreMessageWithId(Context context, long id) {
            Uri u = ContentUris.withAppendedId(Message.CONTENT_URI, id);
            if (AutoInbox.DEBUG) {
                Log.d(AutoInbox.TAG, "restoreMessageWithId: " + u + ", " + id);
            }
            Cursor c = context.getContentResolver().query(u, Message.CONTENT_PROJECTION,
                null, null, null);
            
            try {
                if (c.moveToFirst()) {
                    return getContent(c, Message.class);
                } else {
                    return null;
                }
            } finally {
                c.close();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public AutoInboxContent.Message restore(Cursor c) {
            mBaseUri = CONTENT_URI;
            mId = c.getLong(CONTENT_ID_COLUMN);
            mSubject = c.getString(CONTENT_SUBJECT_COLUMN);
            mBody = c.getString(CONTENT_BODY_COLUMN);
            mSenderId = c.getString(CONTENT_SENDER_ID_COLUMN);
            mSenderName = c.getString(CONTENT_SENDER_NAME_COLUMN);
            mType = c.getInt(CONTENT_TYPE_COLUMN);
            mServerId = c.getLong(CONTENT_SERVER_ID_COLUMN);
            mFlagRead = c.getInt(CONTENT_FLAG_READ_COLUMN);
            mTimeStamp = c.getLong(CONTENT_TIMESTAMP_COLUMN);
            return this;
        }
    }
    
    public interface _4SStoreColumns {
        public static final String ID = RECORD_ID;
        public static final String NAME = "name";
        public static final String _4S_ID = "_4sId";
        public static final String TELEPHONE = "telephone";
        public static final String ADDRESS = "address";
        public static final String LNG = "lng";
        public static final String LAT = "lat";

    }
    
    
    public static final class _4SStore extends AutoInboxContent implements _4SStoreColumns {
        public static final String TABLE_NAME = "_4SStore";
        public static final Uri CONTENT_URI = Uri
                .parse(AutoInboxContent.CONTENT_URI + "/4s_store");
        public String mName;
        public String m4SId;
        public String mTelephone;
        public String mAddress;
        public double mLng;
        public double mLat;

        public static final int CONTENT_ID_COLUMN = 0;
        public static final int CONTENT_NAME_COLUMN = 1;
        public static final int CONTENT_ID4S_COLUMN = 2;
        public static final int CONTENT_TELEPHONE_COLUMN = 3;
        public static final int CONTENT_ADDRESS_COLUMN = 4;
        public static final int CONTENT_LNG_COLUMN = 5;
        public static final int CONTENT_LAT_COLUMN = 6;
        private static final String[] CONTENT_PROJECTION = { RECORD_ID,
                _4SStore.NAME, _4SStore._4S_ID, _4SStore.TELEPHONE,
                _4SStore.ADDRESS, _4SStore.LNG, _4SStore.LAT };

        public _4SStore() {
            mBaseUri = CONTENT_URI;
        }

        @Override
        public ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            values.put(_4SStoreColumns.NAME, mName);
            values.put(_4SStoreColumns._4S_ID, m4SId);
            values.put(_4SStoreColumns.TELEPHONE, mTelephone);
            values.put(_4SStoreColumns.ADDRESS, mAddress);
            values.put(_4SStoreColumns.LNG, mLng);
            values.put(_4SStoreColumns.LAT, mLat);
            return values;
        }

        public static _4SStore restore4SStoreWithId(Context context, long id) {
            Uri u = ContentUris.withAppendedId(_4SStore.CONTENT_URI, id);
            Cursor c = context.getContentResolver().query(u,
                    _4SStore.CONTENT_PROJECTION, null, null, null);

            try {
                if (c.moveToFirst()) {
                    return getContent(c, _4SStore.class);
                } else {
                    return null;
                }
            } finally {
                c.close();
            }
        }
        
        public static _4SStore restore4SStoreWithMessage(Context context, Message message) {
          String selection = _4SStore._4S_ID + "= '" +message.mSenderId +"'";
          Cursor c = context.getContentResolver().query(_4SStore.CONTENT_URI, _4SStore.CONTENT_PROJECTION, selection, null, null);
          try {
              if (c.moveToFirst()) {
                  return getContent(c, _4SStore.class);
              } else {
                  return null;
              }
          } finally {
              c.close();
          }
            
        }

        @SuppressWarnings("unchecked")
        @Override
        public AutoInboxContent._4SStore restore(Cursor c) {
            mBaseUri = CONTENT_URI;
            mId = c.getLong(CONTENT_ID_COLUMN);
            mName = c.getString(CONTENT_NAME_COLUMN);
            m4SId = c.getString(CONTENT_ID4S_COLUMN);
            mTelephone = c.getString(CONTENT_TELEPHONE_COLUMN);
            mAddress = c.getString(CONTENT_ADDRESS_COLUMN);
            mLng = c.getDouble(CONTENT_LNG_COLUMN);
            mLat = c.getDouble(CONTENT_LAT_COLUMN);
            return this;
        }
     }
    
    public interface ReportColumns {
        public static final String ID = RECORD_ID;
        public static final String MESSAGE_KEY = "messageKey";
        public static final String REPORT_TYPE = "reportType";
    }
    
    public static final class Report extends AutoInboxContent implements ReportColumns {
        public static final String TABLE_NAME = "Report";
        public static final Uri CONTENT_URI = Uri
                .parse(AutoInboxContent.CONTENT_URI + "/report");

        public static final int CONTENT_ID_COLUMN = 0;
        public static final int CONTENT_MESSAGE_KEY_COLUMN = 1;
        public static final int CONTENT_REPORT_TYPE_COLUMN = 2;
        
        public static final int TYPE_FETCHED = 0;
        public static final int TYPE_READ = 1;
        
        public static final String[] CONTENT_PROJECTION = { RECORD_ID,
            Report.MESSAGE_KEY, Report.REPORT_TYPE };
        
        public long mMessageKey;
        public int mReportType;
        
        public Report() {
            mBaseUri = CONTENT_URI;
        }
        
        @Override
        public ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            values.put(ReportColumns.MESSAGE_KEY, mMessageKey);
            values.put(ReportColumns.REPORT_TYPE, mReportType);
            return values;
        }
        
        public static Report restoreReportWithId(Context context, long id) {
            Uri u = ContentUris.withAppendedId(Report.CONTENT_URI, id);
            Cursor c = context.getContentResolver().query(u,
                    Report.CONTENT_PROJECTION, null, null, null);

            try {
                if (c.moveToFirst()) {
                    return getContent(c, Report.class);
                } else {
                    return null;
                }
            } finally {
                c.close();
            }
        }
        
        public static Report restoreReportWithServerId(Context context,
                long serverId, int type) {
            String selection = Report.MESSAGE_KEY + "=" + serverId + " AND " + 
                    Report.REPORT_TYPE + "=" + type;
            Cursor c = context.getContentResolver().query(Report.CONTENT_URI,
                    Report.CONTENT_PROJECTION, selection, null, null);
            
            try {
                if (c.moveToFirst()) {
                    return getContent(c, Report.class);
                } else {
                    return null;
                }
            } finally {
                c.close();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public AutoInboxContent.Report restore(Cursor c) {
            mBaseUri = CONTENT_URI;
            mId = c.getLong(CONTENT_ID_COLUMN);
            mMessageKey = c.getLong(CONTENT_MESSAGE_KEY_COLUMN);
            mReportType = c.getInt(CONTENT_REPORT_TYPE_COLUMN);
            return this;
        }
    }
}
