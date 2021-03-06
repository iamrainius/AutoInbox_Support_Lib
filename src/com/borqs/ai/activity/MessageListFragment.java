package com.borqs.ai.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.borqs.ai.AutoInbox;
import com.borqs.ai.Controller;
import com.borqs.ai.MessageProvider;
import com.borqs.ai.R;
import com.borqs.ai.provider.AutoInboxContent.Message;

public class MessageListFragment extends ListFragment implements OnClickListener{
    
    private static final int LOADER_ID_MESSAGE_LIST_LOADER = 0;
    public static final int SNIPPET_MAX_LENGTH = 30;
    
    private Callback mCallback;
    private MessageListAdapter mAdapter;
    private int mProviderType = MessageProvider.TYPE_NO_TYPE;
    private long mMessageId = Message.NO_MESSAGE;
    private ImageButton mSelectedDelete;
    private HashSet<Long> mSelectedSet = new HashSet<Long>();
    private boolean mSelectMode = false;
    
    
    public ImageButton getSelectedDelete() {
        return mSelectedDelete;
    }


    public MessageListAdapter getAdapter() {
        return mAdapter;
    }

    public HashSet<Long> getSelectedSet() {
        return mSelectedSet;
    }

    public void setSelectedSet(HashSet<Long> mSelectedSet) {
        this.mSelectedSet = mSelectedSet;
    }

    public boolean isSelectMode() {
        return mSelectMode;
    }

    public void setSelectMode(boolean mSelectMode) {
        this.mSelectMode = mSelectMode;
    }

    public interface Callback {
        public void onMessageSelected(long messageId);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (AutoInbox.DEBUG) {
            Log.d(AutoInbox.TAG, this + " onCreate");
        }
        
        super.onCreate(savedInstanceState);
        mAdapter = new MessageListAdapter(getActivity());
        this.setListAdapter(mAdapter);
    }

    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        return inflater.inflate(R.layout.message_list_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (AutoInbox.DEBUG) {
            Log.d(AutoInbox.TAG, this + " onActivityCreated");
        }
        super.onActivityCreated(savedInstanceState);
        
        mSelectedDelete = (ImageButton) getActivity().findViewById(R.id.action_select_delete);
        mSelectedDelete.setOnClickListener(this);
        
        LoaderManager lm = this.getLoaderManager();
        lm.initLoader(LOADER_ID_MESSAGE_LIST_LOADER, null, LOADER_CALLBACKS);
        
        getListView().setSelector(R.drawable.list_item_bg);
        getListView().setDivider(getActivity().getResources().getDrawable(R.drawable.divider));
        
        TextView emptyView= (TextView) getActivity().findViewById(R.id.empty);;
        getListView().setEmptyView(emptyView);
    }
    
    @Override
    public void onAttach(Activity activity) {
        if (AutoInbox.DEBUG) {
            Log.d(AutoInbox.TAG, this + " onAttach");
        }
        
        super.onAttach(activity);
        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement MessageListFragment.Callback");
        }
    }
    
    @Override
    public void onStart() {
        if (AutoInbox.DEBUG) {
            Log.d(AutoInbox.TAG, this + " onStart");
        }
        
        super.onStart();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        
        MessageListItem itemView = (MessageListItem) v;
        if (itemView != null) {
            View tagView = (View) l.getTag();
            if (tagView != null) {
                tagView.setBackgroundDrawable(null);
            }
            l.setTag(v);
            mAdapter.setSelected(position);
            mAdapter.notifyDataSetInvalidated();
            mCallback.onMessageSelected(itemView.mMessageId);
        }
        
        if (mSelectMode) {
            if (mSelectedSet.contains(id)) {
                mSelectedSet.remove(id);
            } else {
                mSelectedSet.add(id);
            }
            mAdapter.setSelected(mSelectedSet);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void updateMessageList(int providerType, long messageId) {
        mProviderType = providerType;
        mMessageId  = messageId;
        getLoaderManager().restartLoader(LOADER_ID_MESSAGE_LIST_LOADER, null, LOADER_CALLBACKS);
    }
    
    private static final String[] MESSAGE_LIST_PROJECTION = {
        Message.ID, Message.SUBJECT, Message.BODY, 
        Message.FLAG_READ, Message.TIMESTAMP
    };
    
    private final LoaderCallbacks<Cursor> LOADER_CALLBACKS = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String selection = Message.TYPE + "=" + mProviderType + " order by "
                    + Message.RECORD_ID + " desc ";
            
            CursorLoader loader = new CursorLoader(getActivity(), Message.CONTENT_URI, 
                    MESSAGE_LIST_PROJECTION, selection, null, null);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.swapCursor(data);

            long messageId = mMessageId;
            if (data.moveToFirst()) {
                if (messageId == Message.NO_MESSAGE) {
                    messageId = data.getLong(MessageListAdapter.COLUMN_ID);
                } else {

                }
                mAdapter.setSelected(data.getPosition());
            }
           
            mCallback.onMessageSelected(messageId);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
        
    };
    
    public class MessageListAdapter extends CursorAdapter {
        public static final int COLUMN_ID = 0;
        public static final int COLUMN_SUBJECT = 1;
        public static final int COLUMN_BODY = 2;
        public static final int COLUMN_FLAG_READ = 3;
        public static final int COLUMN_TIMESTAMP = 4;
        
        private Context mContext;
        private java.text.DateFormat mDateFormat;
        private LayoutInflater mInflater;
        private int mSelected = -1;
        private HashSet<Long> mChecked = new HashSet<Long>();
        
        private ViewHolder mViewHolder;
        
        public MessageListAdapter(Context context) {
            super(context, null, true);
            mContext = context;
            mDateFormat = android.text.format.DateFormat.getDateFormat(context);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            MessageListItem itemView = (MessageListItem) view;
            
            itemView.mMessageId = cursor.getLong(COLUMN_ID);
            
            itemView.checkedView = (CheckBox) view.findViewById(R.id.checkBox);
            itemView.checkedView.setChecked(mChecked.contains(itemView.mMessageId));
            if(mSelectMode){
                itemView.checkedView.setVisibility(View.VISIBLE);
            }else{
                itemView.checkedView.setVisibility(View.GONE);
            }
            
            TextView subjectView = (TextView) view.findViewById(R.id.subject);
            String subjectText = cursor.getString(COLUMN_SUBJECT);
            subjectView.setText(subjectText);
            
            TextView snippetView = (TextView) view.findViewById(R.id.snippet);
            String body = cursor.getString(COLUMN_BODY);
            if (!TextUtils.isEmpty(body) && body.length() > SNIPPET_MAX_LENGTH) {
                body = body.substring(0, SNIPPET_MAX_LENGTH);
            }
            snippetView.setText(body);
            
            ImageView readView = (ImageView) view.findViewById(R.id.read_flag);
            int readFlag = cursor.getInt(COLUMN_FLAG_READ);
            if (readFlag == Message.UNREAD) {
                readView.setBackgroundResource(R.drawable.unread);
            } else {
                readView.setBackgroundResource(R.drawable.read);
            }
            
            TextView dateView = (TextView) view.findViewById(R.id.datetime);
            long timestamp = cursor.getLong(COLUMN_TIMESTAMP);
            setTimeText(mDateFormat, dateView, timestamp);
            int position = cursor.getPosition();
            if (position == mSelected) {
                view.setBackgroundResource(R.drawable.list_item_hl);
            } else {
                view.setBackgroundDrawable(null);
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

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.message_list_item, parent, false);
        }
        
        public void setSelected(int selected) {
            mSelected = selected;
        }
        
        public void setSelected(HashSet<Long> selected) {
            mChecked = selected;
        }
        
        public HashSet<Long> getSelected() {
            return mChecked;
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        onSelectedDelete();
    }

    
    
    private void onSelectedDelete() {
        // TODO Auto-generated method stub
        Controller.getInstance(getActivity()).deleteSelectedMessage(mAdapter.mChecked);
        mSelectedDelete.setVisibility(View.GONE);
        this.setSelectMode(false);
        
    }
    
    private static class ViewHolder {

    }
}
