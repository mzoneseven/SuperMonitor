package com.mseven.monitor;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by mseven on 3/24/17.
 */

public class ActivityMonitorService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        //ActivityUtil.setActivityController();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public ActivityMonitorService() {
        super();
    }
}
