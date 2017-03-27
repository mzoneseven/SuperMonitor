package com.mseven.monitor;

import android.app.IActivityController;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by mseven on 3/24/17.
 */

public class ActivityController implements IActivityController {
    @Override
    public boolean activityStarting(Intent intent, String pkg) throws RemoteException {
        Log.d("book", "activityStarting:" + pkg);
        return true;
    }

    @Override
    public boolean activityResuming(String pkg) throws RemoteException {
        Log.d("book", "activityStarting:" + pkg);
        return true;
    }

    @Override
    public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) throws RemoteException {
        return false;
    }

    @Override
    public int appEarlyNotResponding(String processName, int pid, String annotation) throws RemoteException {
        return 0;
    }

    @Override
    public int appNotResponding(String processName, int pid, String processStats) throws RemoteException {
        return 0;
    }

    @Override
    public int systemNotResponding(String msg) throws RemoteException {
        return 0;
    }

    @Override
    public IBinder asBinder() {
        return null;
    }
}
