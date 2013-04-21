package com.borqs.ai.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.AsyncTask;

public class Utilities {

    /**
     * Convert the timestamp in text to millisencond.
     * For unconvertable String, simply set to 0 firstly
     * @param timeStamp
     * @return ms for the timestamp
     */
    public static long parseTimeStamp(String timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
        Date date = null;
        try {
            date = sdf.parse(timeStamp);
        } catch (ParseException e) {
        }
        return date.getTime();
    }
    
    /**
     * Cancel an {@link AsyncTask}.  If it's already running, it'll be interrupted.
     */
    public static void cancelTaskInterrupt(AsyncTask<?, ?, ?> task) {
        cancelTask(task, true);
    }

    /**
     * Cancel an {@link AsyncTask}.
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
     *        task should be interrupted; otherwise, in-progress tasks are allowed
     *        to complete.
     */
    public static void cancelTask(AsyncTask<?, ?, ?> task, boolean mayInterruptIfRunning) {
        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(mayInterruptIfRunning);
        }
    }

}
