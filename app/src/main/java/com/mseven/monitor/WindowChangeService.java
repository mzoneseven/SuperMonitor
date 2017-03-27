package com.mseven.monitor;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import com.mseven.monitor.util.SharedPreUtil;

import static com.mseven.monitor.util.ActivityUtil.mLockMap;

/**
 * Created by mseven on 3/24/17.
 */

public class WindowChangeService extends AccessibilityService {
    private View mShowView;
    private WindowManager mWindowManager;
    WindowManager.LayoutParams mParams;
    private boolean isOnShow = false;
    Handler mHandler = new Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            String packageName = event.getPackageName().toString();
            String className = event.getClassName().toString();
            Log.d("book", "className:" + className);
            Log.d("book", "packageName:" + event.getPackageName().toString());
            Log.i("book", "className:" + event.getClassName().toString());
            if (!TextUtils.isEmpty(className) && !TextUtils.isEmpty(packageName)) {
                boolean isShow = SharedPreUtil.getInstance(this).getBoolean(event.getPackageName().toString(), false);
                Log.d("book", "isShow:" + isShow);
                if (isShow && className.equals(mLockMap.get(packageName))) {
                    if (!isOnShow) {
                        showWindow();
                    }
                } else {
                    if (isOnShow) {
                        removeWindow();
                    }
                }
            } else {
                if (isOnShow) {
                    removeWindow();
                }
            }
            /*Log.i("book", "packageName:" + event.getPackageName().toString());
            Log.i("book", "className:" + event.getClassName().toString());
            ActivityInfo activityInfo = tryGetActivity(componentName);
            boolean isActivity = activityInfo != null;
            if (isActivity) {
                Log.i("book", componentName.flattenToShortString());
                boolean isShow = SharedPreUtil.getInstance(this).getBoolean(event.getPackageName().toString(), false);
                if () {

                }
            } else {showRunnable
                removeWindow();
            }*/
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        createWindow();
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16)
            //Just in case this helps
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        setServiceInfo(config);
    }


    private void createWindow() {
        mWindowManager = (WindowManager) getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams();
        mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.gravity = Gravity.CENTER;
    }


    private Runnable showRunnable = new Runnable() {
        @Override
        public void run() {
            if (isOnShow) {
                removeWindow();
            }
        }
    };

    private void showWindow() {
        mWindowManager.addView(getView(), mParams);
        isOnShow = true;
        mHandler.removeCallbacks(showRunnable);
        mHandler.postDelayed(showRunnable, 3000);
    }

    private void removeWindow() {
        mWindowManager.removeView(getView());
        isOnShow = false;
    }

    private View getView() {
        if (mShowView != null) {
            return mShowView;
        }
        View view = LayoutInflater.from(this).inflate(
                R.layout.show_layout, null);
        mShowView = view;
        return mShowView;
    }
}
