package com.borqs.ai.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.borqs.ai.Controller;
import com.borqs.ai.R;
import com.borqs.ai.provider.AutoInboxContent.Message;
import com.borqs.ai.provider.AutoInboxContent._4SStore;
import com.borqs.ai.utility.Utilities;

public class MessageViewFragment extends Fragment implements OnClickListener {

    private Button mCall;
    private Button mNavigate;
    private Button mDelete;
    private TextView mSubject;
    private TextView mSender;
    private TextView mMessageContent;
    private TextView mDatetime;
    public  _4SStore m4SStore;
    public DateFormat mMessageDateFormat;
    private LoadMessageTask mLoadMessageTask;

    private Message mMessage;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_view_view, container, false);
        mCall = (Button) view.findViewById(R.id.action_call);
        mCall.setVisibility(View.VISIBLE);
        mCall.setOnClickListener(this);
        
        mNavigate = (Button) view.findViewById(R.id.action_navi);
        mNavigate.setVisibility(View.VISIBLE);
        mNavigate.setOnClickListener(this);
        
        mDelete = (Button) view.findViewById(R.id.action_delete);
        mDelete.setOnClickListener(this);
        
        mSubject = (TextView) view.findViewById(R.id.message_subject);
        mSender = (TextView) view.findViewById(R.id.message_sender);
        mDatetime = (TextView) view.findViewById(R.id.message_datetime);
        mMessageContent = (TextView) view.findViewById(R.id.message_content);
        
        mMessageDateFormat = android.text.format.DateFormat.getDateFormat(getActivity().getApplicationContext());
        
        return view;
    }

    public void updateContent(long messageId) {
        rescheduleLoadMessageTask(messageId);
    }
    
    @SuppressWarnings("unchecked")
    private void rescheduleLoadMessageTask(long messageId) {
        Utilities.cancelTaskInterrupt(mLoadMessageTask);
        mLoadMessageTask = new LoadMessageTask(messageId);
        mLoadMessageTask.execute();
    }
    
    private class LoadMessageTask extends AsyncTask<Void, Void, Void> {
        private long mMessageId = Message.NO_MESSAGE;
        public LoadMessageTask(long messageId) {
            mMessageId = messageId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mMessageId == Message.NO_MESSAGE) {
                mMessage = null;
                return null;
            }
            
            Context context = getActivity();
            mMessage = Message.restoreMessageWithId(context, mMessageId);
            if(mMessage == null){
                   return null;
            }
            if(mMessage.mType == Message.TYPE_MESSAGE_4S){
                m4SStore = _4SStore.restore4SStoreWithMessage(context, mMessage);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            clearMessageView();
            if (mMessage == null) {
                return;
            }
            
            int messageType = mMessage.mType;
            if (messageType == Message.TYPE_MESSAGE_4S) {
                mCall.setVisibility(View.VISIBLE);
                mNavigate.setVisibility(View.VISIBLE);
            } else {
                mCall.setVisibility(View.GONE);
                mNavigate.setVisibility(View.GONE);
            }
            
            long timestamp = mMessage.mTimeStamp;
            
            mSubject.setText(mMessage.mSubject);
            mSender.setText(mMessage.mSenderName);
            setTimeText(mMessageDateFormat, mDatetime, timestamp);
            mMessageContent.setText(mMessage.mBody);
            
            if (mMessage.mFlagRead == Message.UNREAD) {
                setRead(mMessage);
            }
        }
    }
    
    private void setTimeText(DateFormat dateformat, TextView textView ,long timestamp){
        Date date = new Date(timestamp);
        String dateSend = dateformat.format(date);
        String currentDate = dateformat.format(System.currentTimeMillis());
        if(!dateSend.equals(currentDate)){
            textView.setText(dateSend);
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            String currentTime = formatter.format(timestamp);
            textView.setText(currentTime);
        }
    }
    
    private void setRead(Message message) {
        Controller.getInstance(getActivity()).setMessageRead(message);
    }
    
    private void clearMessageView() {
        mSender.setText("");
        mSubject.setText("");
        mDatetime.setText("");
        mMessageContent.setText("");
        mCall.setVisibility(View.GONE);
        mNavigate.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.action_call:
            if(mMessage != null && m4SStore != null){
                onCall4SStore(m4SStore.mTelephone);
            }
            break;
        case R.id.action_navi:
            if(mMessage != null && m4SStore != null){
                onNagi4SStore(m4SStore.mLat,m4SStore.mLng);
            }
            break;
        case R.id.action_delete:
            if (mMessage != null) {
                onDeleteMessage(mMessage.mId);
                clearMessageView();
            }
            
            break;
        }
    }
    
    private void onDeleteMessage(long messageId) {
        Controller.getInstance(getActivity()).deleteMessage(messageId);
    }
    private void onCall4SStore(String telephone) {
        Controller.getInstance(getActivity()).call4sStore(telephone);
    }
    private void onNagi4SStore(double _4SLat , double _4SLng) {
        Controller.getInstance(getActivity()).nagi4SStore(_4SLng, _4SLat);
    }
}
