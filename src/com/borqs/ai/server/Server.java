package com.borqs.ai.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.ai.AutoInbox;
import com.borqs.ai.R;
import com.borqs.ai.provider.AutoInboxContent.Message;
import com.borqs.ai.provider.AutoInboxContent._4SStore;
import com.borqs.ai.utility.Utilities;
import com.borqs.ai.xml.MessageXmlParser;

public class Server {
    
    private static Server sServer = null;
    
    private static final String SAMPLE_HOST = "http://ucs.chinatsp.com/api/1.0/tu/";
    private static final String SAMPLE_TUID = "12345678123456781234567812345678";
    private static final String SAMPLE_4S_HOST = "http://ucs.chinatsp.com/api/1.0/4s/";
    private static final String SAMPLE_4SID = "001";
    
    private static final String OPERATION_CATEGORY_MESSAGE = "msg";
    private static final String OPERATION_TYPE_RECEIVE = "receive";
    private static final String OPERATION_CATEGORY_STORE_4S = "info";
    private static final String PARAM_KEY_MESSAGE_LIST = "msg_list";
    private static final String OPERATION_TYPE_READ = "read";
    
    private Context mContext;
    private String mHost;
    private String mTUid;
    private String mHost_4S;
    private String m4SID;
    
    private Server(Context context) {
        mContext = context;
        mHost = SAMPLE_HOST;
        mTUid = SAMPLE_TUID;
        mHost_4S = SAMPLE_4S_HOST;
        m4SID = SAMPLE_4SID;
    }
    
    public static Server getInstance(Context context) {
        if (sServer == null) {
            sServer = new Server(context);
        }
        return sServer;
    }
    
    public boolean isConnectingToInternet(){  
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);  
        if (connectivity != null)  
          {  
            NetworkInfo[] info = connectivity.getAllNetworkInfo();  
            if (info != null) {  
                for (int i = 0; i < info.length; i++) {  
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {  
                        return true;  
                    }  
                }  
            }  
        }  
        return false;  
    }  
    
    public List<Message> fetchMessages() {
        List<Message> messages = new ArrayList<Message>();
        
        // Build a message request URL
        StringBuilder sb = new StringBuilder();
        sb.append(mHost)
          .append(mTUid)
          .append("/")
          .append(OPERATION_CATEGORY_MESSAGE)
          .append("/");
        
        try {
            URL url = new URL(sb.toString());
            if (AutoInbox.DEBUG) {
                Log.d(AutoInbox.TAG, "Fetch messages, url=" + url);
            }
            if (isConnectingToInternet()) {
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                InputStream is = conn.getInputStream();

//                XmlPullParserFactory xppFactory = XmlPullParserFactory
//                        .newInstance();
//                XmlPullParser xpp = xppFactory.newPullParser();
//                xpp.setInput(is, "utf-8");
//
//                messages = parseMessages(xpp);
                messages = parseMessages(is);

                is.close();
                conn.disconnect();
            }
            
        } catch (MalformedURLException e) {
            Log.e(AutoInbox.TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(AutoInbox.TAG, e.getMessage());
        }
        
        return messages;
    }
    
    private List<Message> parseMessages(InputStream is) {
        MessageXmlParser parser = new MessageXmlParser();
        try {
            return parser.parse(is);
        } catch (XmlPullParserException e) {
        } catch (IOException e) {
        }
        
        return new ArrayList<Message>();
    }

public _4SStore fetch4SStore(String _4sId) {
        if (TextUtils.isEmpty(_4sId)) {
            return null;
        }
        _4SStore _4sStore = new _4SStore();
        // Build a store_4S request URL
        StringBuilder sb = new StringBuilder();
        sb.append(mHost_4S)
          .append(m4SID)
          .append("/")
          .append(OPERATION_CATEGORY_STORE_4S)
          .append("/");
        
        try {
            URL url = new URL(sb.toString());
            if (AutoInbox.DEBUG) {
                Log.d(AutoInbox.TAG, "Fetch 4s store, url=" + url);
            }
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream is = conn.getInputStream();
            
            XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = xppFactory.newPullParser();
            xpp.setInput(is, "utf-8");
            
            _4sStore = parse4SStore(xpp);
            _4sStore.m4SId = _4sId;
            is.close();
            conn.disconnect();
            
        } catch (MalformedURLException e) {
            Log.e(AutoInbox.TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(AutoInbox.TAG, e.getMessage());
            throw new RuntimeException(e);
        } catch (XmlPullParserException e) {
            Log.e(AutoInbox.TAG, e.getMessage());
        }
        
        return _4sStore;
    }
    
    /**
     * Report the fetched new messages to the server
     * This is to announce the server not to send these messages repeatedly
     * 
     * return: a failed message server ID list
     */
    public List<Long> reportFetched(List<Long> serverIds) {
        StringBuilder sb = new StringBuilder();
        sb.append(mHost)
          .append(mTUid)
          .append('/')
          .append(OPERATION_CATEGORY_MESSAGE)
          .append('/')
          .append(OPERATION_TYPE_RECEIVE)
          .append('/');
        
        StringBuilder param = new StringBuilder();
        param.append(PARAM_KEY_MESSAGE_LIST)
             .append('=');
        for (int i=0; i<serverIds.size(); i++) {
            param.append(serverIds.get(i));
            if (i < serverIds.size() - 1) {
                param.append(',');
            }
        }
        
        List<Long> failedList = serverIds;
        
        try {
            URL url = new URL(sb.toString());
            if (AutoInbox.DEBUG) {
                Log.d(AutoInbox.TAG, "report received messages, url=" + url);
            }
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            
            OutputStream os = conn.getOutputStream();
            os.write(param.toString().getBytes());
            os.flush();
            os.close();
            
            // Todo: handle the response
            int code = conn.getResponseCode();
            
            
            if (code == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = xppFactory.newPullParser();
                xpp.setInput(is, "utf-8");
                
                ReceivedReportResponse response = parseReportResponse(xpp);
                if (ReceivedReportResponse.STATUS_OK.equals(response.status)) {
                    failedList = response.failedList;
                }
            }
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } catch (XmlPullParserException e) {
        }
        
        return failedList;
    }
    
    /**
     * Report the message has just been read
     * return: failed list
     */
    public List<Long> reportRead(List<Long> serverIds) {
        StringBuilder sb = new StringBuilder();
        sb.append(mHost)
          .append(mTUid)
          .append('/')
          .append(OPERATION_CATEGORY_MESSAGE)
          .append('/')
          .append(OPERATION_TYPE_READ)
          .append('/');
        
        StringBuilder param = new StringBuilder();
        param.append(PARAM_KEY_MESSAGE_LIST)
             .append('=');
        for (int i=0; i<serverIds.size(); i++) {
            param.append(serverIds.get(i));
            if (i < serverIds.size() - 1) {
                param.append(',');
            }
        }
        
        List<Long> failedList = serverIds;
        
        try {
            URL url = new URL(sb.toString());
            if (AutoInbox.DEBUG) {
                Log.d(AutoInbox.TAG, "report received messages, url=" + url);
            }
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            
            OutputStream os = conn.getOutputStream();
            os.write(param.toString().getBytes());
            os.flush();
            os.close();
            
            // Todo: handle the response
            int code = conn.getResponseCode();
            
            
            if (code == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = xppFactory.newPullParser();
                xpp.setInput(is, "utf-8");
                
                ReceivedReportResponse response = parseReportResponse(xpp);
                if (ReceivedReportResponse.STATUS_OK.equals(response.status)) {
                    failedList = response.failedList;
                }
            }
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } catch (XmlPullParserException e) {
        }
        
        return failedList;
    }
    
    /**
     * Only for test
     */
    List<Message> fetchMessagesFromMock() {
        XmlResourceParser xpp = mContext.getResources().getXml(R.xml.test_resp_messages);
        List<Message> messages = null;
        try {
            messages = this.parseMessages(xpp);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return messages;
    }
    public _4SStore fetchStores_4SFromMock() {
        XmlResourceParser xpp = mContext.getResources().getXml(R.xml.test_resp_stores);
        _4SStore store = null;
        try {
            store = this.parse4SStore(xpp);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return store;
    }

    private double getLngFromXpp(String lng) {
        if (lng == null || lng.trim().length() == 0) {
            return 0;
        }
        return Double.parseDouble(lng.trim());

    }

    private double getLatFromXpp(String lat) {
        if (lat == null || lat.trim().length() == 0) {
            return 0;
        }
        return Double.parseDouble(lat.trim());

    }
    /*package*/ List<Message> parseMessages(XmlPullParser xpp) throws NumberFormatException, 
            XmlPullParserException, IOException {
        List<Message> messages = new ArrayList<Message>();
        
        if (xpp == null) {
            return messages;
        }
        
        String status;
        Message msg = null;
        
        int eventType;
        while ((eventType = xpp.next()) != XmlPullParser.END_DOCUMENT) {
            if ((eventType == XmlPullParser.START_TAG) 
                    && "item".equals(xpp.getName())) {
                if ("resp_status".equals(xpp.getAttributeValue(null, "key"))) {
                    xpp.next();
                    status = xpp.getText();
                    // Need any handle to status?
                    
                } else if ("msg_id".equals(xpp.getAttributeValue(null, "key"))) {
                    msg = new Message();
                    xpp.next();
                    String text = xpp.getText();
                    msg.mServerId = Long.parseLong(text);
                } else if ("title".equals(xpp.getAttributeValue(null, "key"))) {
                    xpp.next();
                    msg.mSubject = xpp.getText();
                } else if ("content".equals(xpp.getAttributeValue(null, "key"))) {
                    xpp.next();
                    msg.mBody = xpp.getText();
                } else if ("msg_type".equals(xpp.getAttributeValue(null, "key"))) {
                    xpp.next();
                    msg.mType = Integer.parseInt(xpp.getText());
                } else if ("sender_id".equals(xpp.getAttributeValue(null, "key"))) {
                    xpp.next();
                    msg.mSenderId =  xpp.getText();
                } else if ("sender_name".equals(xpp.getAttributeValue(null, "key"))) {
                    xpp.next();
                    msg.mSenderName =  xpp.getText();
                } else if ("send_timestamp".equals(xpp.getAttributeValue(null, "key"))) {
                    xpp.next();
                    msg.mTimeStamp = Utilities.parseTimeStamp(xpp.getText());
                    messages.add(msg);
                    msg = null;
                }
            }
        }
        
        return messages;
    }
    
    public _4SStore parse4SStore(XmlPullParser xpp) throws NumberFormatException, 
    XmlPullParserException, IOException {
        if (xpp == null) {
            return null;
        }

        String status;
        _4SStore store = new _4SStore();
        int eventType;
        while ((eventType = xpp.next()) != XmlPullParser.END_DOCUMENT) {
            if ((eventType == XmlPullParser.START_TAG)
                    && "item".equals(xpp.getName())) {
                if ("resp_status".equals(xpp.getAttributeValue(null, "key"))) {
                    xpp.next();
                    status = xpp.getText();
                    // Need any handle to status?
                } else if ("name".equals(xpp.getAttributeValue(null, "key"))) {
                    xpp.next();
                    store.mName = xpp.getText();
                } else if ("telephone".equals(xpp.getAttributeValue(null, "key"))) {
                    xpp.next();
                    store.mTelephone = xpp.getText();
                } else if ("address".equals(xpp.getAttributeValue(null, "key"))) {
                    xpp.next();
                    store.mAddress = xpp.getText();
                } else if ("lat".equals(xpp.getAttributeValue(null, "key"))) {
                    xpp.next();
                    store.mLat = getLatFromXpp(xpp.getText());
                } else if ("lng".equals(xpp.getAttributeValue(null, "key"))) {
                    xpp.next();
                    store.mLng = getLngFromXpp(xpp.getText());
                } 
            }
        }
        return store;
    }
    
    // We need to check the TUID before do some interaction with the server
    private void validateTuid() {
        mTUid = getDeviceTuid();
        
        if (mTUid == null) {
            //mTUid = 
        }
    }

    private String getDeviceTuid() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * @param xpp
     * @return the failed list
     */
    private ReceivedReportResponse parseReportResponse(XmlPullParser xpp) {
        ReceivedReportResponse rrr = new ReceivedReportResponse();
        
        // Todo: 
        
        return rrr;
    }
    
    private class ReceivedReportResponse {
        public static final String STATUS_OK = "OK";
        
        public String status = STATUS_OK;
        public List<Long> failedList = new ArrayList<Long>();
        
        @Override
        public String toString() {
            return "received report: status=" + status;
        }
    }
}
