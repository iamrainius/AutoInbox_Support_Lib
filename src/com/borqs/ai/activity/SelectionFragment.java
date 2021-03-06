package com.borqs.ai.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.borqs.ai.AutoInbox;
import com.borqs.ai.MessageProvider;
import com.borqs.ai.R;

public class SelectionFragment extends Fragment implements Button.OnClickListener {
    private static final String KEY_SELECTION_BUTTON = "com.borqs.ai.KeySelectionButton";
    private static final int NO_SELECTION = -1;
    private Button mSystem;
    private Button m4S;
    private Button mStores;
    private int mSelection;
    
    private Callback mCallback;
    
    public interface Callback {
        public void onTabSelected(int providerType);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.selection_view, container, false);
        mSystem = (Button) view.findViewById(R.id.sel_system);
        mSystem.setOnClickListener(this);
        m4S = (Button) view.findViewById(R.id.sel_4s); 
        m4S.setOnClickListener(this);
        mStores = (Button) view.findViewById(R.id.sel_stores); 
        mStores.setOnClickListener(this);
        
        int selection = NO_SELECTION;
        if (savedInstanceState != null) {
            selection = savedInstanceState.getInt(KEY_SELECTION_BUTTON, NO_SELECTION);
        }
        if (selection == NO_SELECTION) {
            mSelection = R.id.sel_4s;
        } else {
            mSelection = selection;
        }
        updateSelection(view, mSelection);
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SELECTION_BUTTON, mSelection);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SelectionFragment.Callback");
        }
    }
    
    

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCallback.onTabSelected(MessageProvider.TYPE_4S);
    }

    @Override
    public void onClick(View v) {
        updateSelection(v, v.getId());

        int type = MessageProvider.TYPE_NO_TYPE;
        switch (v.getId()) {
        case R.id.sel_system:
            type = MessageProvider.TYPE_SYSTEM;
            break;
        case R.id.sel_4s:
            type = MessageProvider.TYPE_4S;
            break;
        case R.id.sel_stores:
            type = MessageProvider.TYPE_STORES;
            break;
        default:
            throw new IllegalStateException("Illegal view clicked");
        }
        
        mCallback.onTabSelected(type);
    }
    public void updateSelection(View parent, int id) {
        mSelection = id;
        resetButtons();
        highlight(parent, id);
    }
    
    private void highlight(View parent, int id) {
        Button button = (Button) parent.findViewById(id);
        if (button != null) {
            button.setBackgroundResource(R.drawable.tab_bg_hl);
        }
    }
    
    public void resetButtons() {
        if (mSystem != null) {
            mSystem.setBackgroundResource(R.drawable.tab_bg);
        }
        if (m4S != null) {
            m4S.setBackgroundResource(R.drawable.tab_bg);
        }
        if (mStores != null) {
            mStores.setBackgroundResource(R.drawable.tab_bg);
        }
    }
    
}
