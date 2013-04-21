package com.borqs.ai.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.borqs.ai.Controller;
import com.borqs.ai.R;
import com.borqs.ai.provider.AutoInboxContent.Message;
import com.borqs.ai.widget.WidgetDatabaseObserver;

public class AutoInboxActivity extends FragmentActivity
        implements SelectionFragment.Callback, MessageListFragment.Callback {

    private int mProviderType ;
    private int mSelectionId;
    private WidgetDatabaseObserver mObserver;
    private Controller mController = Controller.getInstance(this);
    public final static String SELECTED_MESSAGE_TYPE = "selectedMessageType";
    
    
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        mProviderType = intent.getIntExtra(SELECTED_MESSAGE_TYPE, Message.TYPE_MESSAGE_4S);
        onTabSelected(mProviderType);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_inbox_activity);
        mObserver = new WidgetDatabaseObserver(getApplicationContext());
        getApplicationContext().getContentResolver().registerContentObserver(Message.CONTENT_URI, true,mObserver);
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        Intent intent = getIntent();
        mProviderType = intent.getIntExtra(SELECTED_MESSAGE_TYPE, Message.TYPE_MESSAGE_4S);
        super.onResume();
        onTabSelected(mProviderType);
        SelectionFragment selectionFragment = (SelectionFragment) 
                getSupportFragmentManager().findFragmentById(R.id.selection_fragment);
        selectionFragment.resetButtons();
        View view = onSetCurrentButton();
        selectionFragment.updateSelection(view, mSelectionId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.auto_inbox_activity, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_refresh:
            onRefreshMessage();
            return true;
        case R.id.menu_select:
            onSeleteMessage();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void onSeleteMessage() {
        MessageListFragment messageList = (MessageListFragment) 
                getSupportFragmentManager().findFragmentById(R.id.message_list_fragment);
        
        if(messageList.isSelectMode()){
            messageList.setSelectMode(false);
            messageList.getSelectedDelete().setVisibility(View.GONE);
        }else {
            messageList.setSelectMode(true);
            messageList.getSelectedDelete().setVisibility(View.VISIBLE);

        }
        messageList.getAdapter().notifyDataSetChanged();

    }
    private void onRefreshMessage() {
        mController.checkMessages();
        onOpenMessageList(mProviderType, Message.NO_MESSAGE);
    }
    
    private View onSetCurrentButton() {
         View view = null;
         switch(mProviderType){
         case Message.TYPE_MESSAGE_4S:
            mSelectionId = R.id.sel_4s;
            break;
         case Message.TYPE_MESSAGE_STORES:
            mSelectionId = R.id.sel_stores;
            break;
         case Message.TYPE_MESSAGE_SYSTEM:
            mSelectionId = R.id.sel_system;
            break;
         }
         view = findViewById(mSelectionId);
         return view;
    }
    
    private void onOpenMessageList(int providerType, long messageId) {
        mProviderType = providerType;
        
        MessageListFragment messageList = (MessageListFragment) 
                getSupportFragmentManager().findFragmentById(R.id.message_list_fragment);
        
        if (messageList != null) {
            messageList.updateMessageList(mProviderType, messageId);
        }
    }
    
    // Implement SelectionFragment.Callback
    @Override
    public void onTabSelected(int providerType) {
        onOpenMessageList(providerType, Message.NO_MESSAGE);
    }
    
    // Implement MessageListFragment.Callback
    @Override
    public void onMessageSelected(long messageId) {
        onOpenMessage(messageId);
    }

    private void onOpenMessage(long messageId) {
        MessageViewFragment messageView = (MessageViewFragment) 
                getSupportFragmentManager().findFragmentById(R.id.message_view_fragment);
        if (messageView != null) {
            messageView.updateContent(messageId);
        }
    }
}
