package com.borqs.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.borqs.ai.provider.AutoInboxContent.Message;
import com.borqs.ai.provider.AutoInboxContent.Report;
import com.borqs.ai.provider.AutoInboxContent._4SStore;
import com.borqs.ai.server.Server;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

/**
 * Design this component to process all the business in a queue.
 * Placing all the tasks into a queue can make the business to be done 
 * one after another.
 * @author b469
 *
 */
public class MessagingController implements Runnable, LocationListener {

    private static MessagingController sInstance = null;
    private final BlockingQueue<Command> mCommands = new LinkedBlockingQueue<Command>();
    private final Thread mThread;
    private final Context mContext;
    private boolean mBusy;
    
    final static String LATITUDE_KEY = "latitude";
    final static String LONGITUDE_KEY = "longitude";
    final static String ACTION_MAP_COMMAND = "android.intent.action.map.command";
    final static String COMMAND_KEY = "command";
    final static String COMMAND_DAY_MODE = "day";
    final static String COMMAND_NIGHT_MODE = "night";
    final static String COMMAND_SATELLITE_TYPE = "satellite";
    final static String COMMAND_STREET_TYPE = "street";
    final static String COMMAND_TRAFFIC_TYPE = "traffic";
    final static String ACTION_MAP_VIEW = "android.intent.action.map.view";
    final static String POINTLIST_KEY = "pointList";
    final static String ACTION_MAP_POI = "android.intent.action.map.poi";
    final static String POI_RADIUS_KEY = "poiRadius";
    final static String POI_TYPE_KEY = "poiType";
    final static String POI_HOTEL = "hotel";
    final static String POI_PARK = "park";
    final static String ACTION_MAP_ROUTE = "android.intent.action.map.route";
    final static String ROUTE_TYPE_KEY = "routeType";
    final static String ROUTE_NEAREST = "nearest";
    final static String ROUTE_FASTEST = "fastest";
    
    private MessagingController(Context context) {
        mContext = context;
        mThread = new Thread(this);
        mThread.start();
    }
    
    public synchronized static MessagingController getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MessagingController(context);
        }
        
        return sInstance;
    }
    
    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            Command command;
            
            try {
                // The BlockingQueue will wait if no available element
                command = mCommands.take();
            } catch (InterruptedException e) {
                continue;
            }
            
            mBusy = true;
            command.runnable.run();
            mBusy = false;
        }
    }
    
    public boolean isBusy() {
        return mBusy;
    }

    private void put(String desc, Runnable runnable) {
        Command command = new Command();
        command.runnable = runnable;
        command.description = desc;
        mCommands.add(command);
    }
    
    private static class Command {
        public Runnable runnable;
        public String description;
        
        @Override
        public String toString() {
            return description;
        }
    }
    
    public void checkMessages() {
        put("checkMessages", new Runnable() {
            @Override
            public void run() {
                Server server = Server.getInstance(mContext);
                List<Message> messageList = server.fetchMessages();
                for (Message message : messageList) {
                    // When the message has one record in Report, that means
                    // the message has been ever fetched while failed to be reported.
                    // We ignore such messages
                    if (hasReport(message, Report.TYPE_FETCHED)) {
                        continue;
                    }
                    message.save(mContext);
                    // Generate a report for the message
                    Report r = new Report();
                    r.mMessageKey = message.mServerId;
                    r.mReportType = Report.TYPE_FETCHED;
                    r.save(mContext);
                    
                    // fetch 4S store info for a 4S message
                    if (message.mType == Message.TYPE_MESSAGE_4S) {
                        load4SStore(message);
                    }
                }
                
                // Report the fetched list
                Map<Long, Report> reports = getReportsByType(Report.TYPE_FETCHED);
                List<Long> serverIds = new ArrayList<Long>();
                for (Report r : reports.values()) {
                    serverIds.add(r.mMessageKey);
                }
                List<Long> failedList = server.reportFetched(serverIds);
                // Delete reports that have been reported
                Map<Long, Report> success = reconcileReportList(reports, failedList);
                Long[] messageIds = new Long[success.size()];
                deleteReports(success.keySet().toArray(messageIds));
            }
        });
    }
    
    public void setMessageRead(final Message message) {
        put("setMessageRead", new Runnable() {
            @Override
            public void run() {
                // Set the message flag to read
                if (message.isSaved()) {
                    ContentValues values = new ContentValues();
                    values.put(Message.FLAG_READ, Message.READ);
                    Uri uri = Uri.withAppendedPath(message.mBaseUri, String.valueOf(message.mId));
                    mContext.getContentResolver().update(uri, values, null, null);
                } else {
                    throw new IllegalStateException("reportMessageRead: " + "the message must have been saved before read");
                }
                
                // Generate report for the read message if not saved
                Report report = Report.restoreReportWithServerId(mContext, message.mServerId, Report.TYPE_READ);
                if (report == null) {
                    report = new Report();
                    report.mMessageKey = message.mServerId;
                    report.mReportType = Report.TYPE_READ;
                    report.save(mContext);
                }
                
                // query all messages which have not been reported
                Map<Long, Report> reports = getReportsByType(Report.TYPE_READ);
                List<Long> serverIds = new ArrayList<Long>();
                for (Report r : reports.values()) {
                    serverIds.add(r.mMessageKey);
                }
                if (serverIds.size() == 0) {
                    return;
                }
                
                Server server = Server.getInstance(mContext);
                List<Long> failed = server.reportRead(serverIds);
                Map<Long, Report> success = reconcileReportList(reports, failed);
                // Remove the succeeded reports
                Long[] messageIds = new Long[success.size()];
                deleteReports(success.keySet().toArray(messageIds));
            }
            
        });
    }
    
    private boolean hasReport(Message message, int type) {
        String selection = Report.MESSAGE_KEY + "=" + message.mServerId + " AND " + 
                Report.REPORT_TYPE + "=" + type;
        Cursor c = mContext.getContentResolver().query(Report.CONTENT_URI, new String[] {Report.ID}, selection, 
                null, null);
        if (c == null) {
            return false;
        }
        
        try {
            if (c.getCount() > 0) {
                return true;
            } else {
                return false;
            }
        } finally {
            c.close();
        }
    }
    
    private void load4SStore(Message message) {
        String _4sId = message.mSenderId;
        _4SStore store = Server.getInstance(mContext).fetch4SStore(_4sId);
        if (store != null) {
            Cursor c = mContext.getContentResolver().query(_4SStore.CONTENT_URI, new String[] { _4SStore.ID }, 
                null, null, null);
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        long id = c.getLong(_4SStore.CONTENT_ID_COLUMN);
                        // 4S store instance exists, update it
                        ContentValues cv = store.toContentValues();
                        mContext.getContentResolver().update(Uri.withAppendedPath(store.mBaseUri, String.valueOf(id)), 
                                cv, null, null);
                    } else {
                        store.save(mContext);
                    }
                } finally {
                    c.close();
                }
            }
        }
    }
    
    /**
     * Restore all reports with the specific type from the DB
     * @param type
     * @return
     */
    /*package*/ Map<Long, Report> getReportsByType(int type) {
        if (type != Report.TYPE_FETCHED && type != Report.TYPE_READ) {
            throw new IllegalStateException("controller: wrong report type: " + type);
        }
        
        Map<Long, Report> reports = new HashMap<Long, Report>();
        String selection = Report.REPORT_TYPE + "=" + type;
        Cursor c = mContext.getContentResolver().query(Report.CONTENT_URI, new String[] { Report.ID }, selection, 
                null, null);
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    long id = c.getLong(0);
                    Report r = Report.restoreReportWithId(mContext, id);
                    if (r != null) {
                        reports.put(r.mId, r);
                    }
                }
            } finally {
                c.close();
            }
        }
        return reports;
    }
    
    /*package*/ Map<Long, Report> reconcileReportList(Map<Long, Report> reports,
            List<Long> failed) {
        if (failed.size() <= 0) {
            return reports;
        }
        
        Map<Long, Report> success = new HashMap<Long, Report>();
        for (Report r : reports.values()) {
            boolean isSuccess = true;
            for (long f : failed) {
                if (r.mMessageKey == f) {
                    isSuccess = false;
                    break;
                }
            }
            
            if (isSuccess) {
                success.put(r.mId, r);
            }
        }
        return success;
    }

    /*package*/ void deleteReports(Long[] messageIds) {
        if (messageIds != null && messageIds.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<messageIds.length; i++) {
                sb.append(messageIds[i]);
                if (i < messageIds.length - 1) {
                    sb.append(',');
                }
            }
            
            String delSelection = Report.ID + " in (" + sb.toString() + ")";
            mContext.getContentResolver().delete(Report.CONTENT_URI, delSelection, null);
        }
    }

    public void deleteMessage(final long messageId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = Message.restoreMessageWithId(mContext, messageId);
                if (message == null) {
                    return;
                }
                
                ContentResolver resolver = mContext.getContentResolver();
                resolver.delete(Uri.withAppendedPath(message.mBaseUri, String.valueOf(messageId)), null, null);
            }
        }).start();
//        this.put("deleteMessage", new Runnable() {
//            @Override
//            public void run() {
//                Message message = Message.restoreMessageWithId(mContext, messageId);
//                if (message == null) {
//                    return;
//                }
//                
//                ContentResolver resolver = mContext.getContentResolver();
//                resolver.delete(Uri.withAppendedPath(message.mBaseUri, String.valueOf(messageId)), null, null);
//            }
//        });
    }
    
    public void call4sStore(final String telephone){
        new Thread() {
            @Override
            public void run() {
                if (AutoInbox.DEBUG) {
                    Log.d(AutoInbox.TAG, "controller: call 4SStore, 4sTelephone=" + telephone);
                }
                if (telephone.trim().length() != 0) {
                    Intent phoneIntent = new Intent("android.intent.action.CALL",
                            Uri.parse("tel:" + telephone));
                    mContext.startActivity(phoneIntent);
                } else {
                    Toast.makeText(mContext,
                            R.string.call_number_nonexistent, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }.start();
    }
    
    public void nagi4SStore(final double _4SLng,final double _4SLat) {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                if (AutoInbox.DEBUG) {
                    Log.d(AutoInbox.TAG, "controller: nagi 4SStore, 4SLng:"
                            + _4SLng + " ; 4SLat:" + _4SLat);
                }

                try {
                    final Intent intent = new Intent(ACTION_MAP_ROUTE);
                    intent.putParcelableArrayListExtra(POINTLIST_KEY,
                            setPointList(_4SLng, _4SLat));
                    intent.putExtra(ROUTE_TYPE_KEY, ROUTE_NEAREST);
                    mContext.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {
                    Toast.makeText(mContext, R.string.no_navigate_application,
                            Toast.LENGTH_SHORT).show();
                }
                Looper.loop();
            }
        }.start();
    }
    
    private ArrayList<Bundle> setPointList(double _4SLng,double _4SLat) {
        Bundle pointCar = locationCar();
        final Bundle point4S = new Bundle();
        point4S.putFloat(LATITUDE_KEY, (float) _4SLng);
        point4S.putFloat(LONGITUDE_KEY, (float) _4SLat);
        final ArrayList<Bundle> pointList = new ArrayList<Bundle>();
        pointList.add(pointCar);
        pointList.add(point4S);
        return pointList;
    }
    
    private Bundle locationCar() {
        LocationManager  locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        final Bundle pointCar = new Bundle();
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        if(provider == null){
            Toast.makeText(mContext,
                    R.string.location_services_notopened, Toast.LENGTH_SHORT)
                    .show();
        }
        locationManager.requestLocationUpdates(provider, 60000, 1, this);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            double latCar = location.getLatitude();
            double lngCar = location.getLongitude();

            pointCar.putFloat(LATITUDE_KEY, (float) latCar);
            pointCar.putFloat(LONGITUDE_KEY, (float) lngCar);
            Toast.makeText(mContext, latCar + ";" + lngCar,
                    Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(mContext,
                    R.string.getlocation_null, Toast.LENGTH_SHORT)
                    .show();
        }
        return pointCar;
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
        
    }
    
}
