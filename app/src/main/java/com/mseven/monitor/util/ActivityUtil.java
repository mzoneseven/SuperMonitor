package com.mseven.monitor.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.mseven.monitor.ActivityController;
import com.mseven.monitor.AppInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mseven on 3/24/17.
 */

public class ActivityUtil {

    public static HashMap<String, String> mLockMap = new HashMap<>();

    public static boolean isTopActivy(String cmdName, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(Integer.MAX_VALUE);
        String cmpNameTemp = null;
        if (null != runningTaskInfos) {
            cmpNameTemp = (runningTaskInfos.get(0).topActivity).toString();
        }
        if (null == cmpNameTemp) {
            return false;
        }
        return cmpNameTemp.equals(cmdName);

    }

    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getPackageName();
        List<ActivityManager.RecentTaskInfo> appTask = activityManager.getRecentTasks(Integer.MAX_VALUE, 1);
        if (appTask == null) {
            return false;
        }
        if (appTask.get(0).baseIntent.toString().contains(packageName)) {
            return true;
        }
        return false;
    }

    public static List<AppInfo> getPackageMainActivity(Context context) {
        mLockMap.clear();
        List<AppInfo> appinfos = new ArrayList<>();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> infoList = context.getPackageManager().queryIntentActivities(
                mainIntent, PackageManager.MATCH_ALL);
        Collections.sort(infoList, new ResolveInfo.DisplayNameComparator(
                context.getPackageManager()));
        if (infoList != null) {
            for (ResolveInfo info : infoList) {
                AppInfo myApp = new AppInfo();
                String packname = info.activityInfo.packageName;
                if (context.getPackageName().equals(packname)) {
                    continue;
                }
                String className = info.activityInfo.name;
                //mainIntent.setPackage(packname);
                //ComponentName ac = mainIntent.resolveActivity(context.getPackageManager());
                //if (ac != null) {
                //String classname = ac.getClassName();
                Log.d("book", "packagename:" + packname);
                Log.d("book", "classname:" + className);
                myApp.setPackname(packname);
                Drawable icon = info.loadIcon(context.getPackageManager());
                myApp.setIcon(icon);
                String appname = info.loadLabel(context.getPackageManager()).toString();
                myApp.setAppname(appname);
                myApp.setClassname(className);
                mLockMap.put(packname,className);
                appinfos.add(myApp);
            }
            //mPackageHashSet.add(packname);
            //}
        }
        return appinfos;
    }

    public static String mainClassName(Context context, String packname) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packname);
        ComponentName ac = mainIntent.resolveActivity(context.getPackageManager());
        if (ac != null) {
            return ac.getClassName();
        } else {
            return "";
        }
    }


    public static void setActivityController() {
        try {
            Log.d("book", "setActivityController");
            Class<?> cActivityManagerNative = Class
                    .forName("android.app.ActivityManagerNative");
            Method mGetDefault = cActivityManagerNative.getMethod("getDefault", null);
            Object oActivityManagerNative = mGetDefault.invoke(null, null);
            Method mSetActivityController = cActivityManagerNative.getMethod(
                    "setActivityController", Class.forName("android.app.IActivityController"));
            ActivityController activityController = new ActivityController();
            mSetActivityController.invoke(oActivityManagerNative, activityController);
            Log.d("book", "setActivityController");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
