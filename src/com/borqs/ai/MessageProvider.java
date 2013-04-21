package com.borqs.ai;

import android.content.Context;

public class MessageProvider {
    public static final int TYPE_NO_TYPE = -1;
    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_4S = 2;
    public static final int TYPE_STORES = 3;
    
    public static String getName(Context context, int type) {
        if (context != null) {
            switch (type) {
            case TYPE_SYSTEM:
                return context.getString(R.string.tab_system);
            case TYPE_4S:
                return context.getString(R.string.tab_4s);
            case TYPE_STORES:
                return context.getString(R.string.tab_stores);
            }
        }
        
        return "";
    }
}
