package com.mseven.monitor.util;

/**
 * Created by mseven on 3/27/17.
 */


import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.os.Parcel;
import android.os.UserHandle;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static android.content.ContentValues.TAG;

public class PermissionUtil {

    public static final int FLAG_PERMISSION_USER_FIXED = 1 << 1;
    public static final int FLAG_PERMISSION_USER_SET = 1 << 0;
    public static final int OP_SYSTEM_ALERT_WINDOW = 24;
    public static final String STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String WINDOW_PERMISSION = "android.permission.SYSTEM_OVERLAY_WINDOW";
    public static final String PHONE_PERMISSION = "android.permission.READ_PHONE_STATE";
    private static final String LAUNCHER_PACKAGE = "com.lewaos.launcher";

    public static void grantLauncherPermission(Context context) {
        grantAppPermission(context, LAUNCHER_PACKAGE, STORAGE_PERMISSION);
        //grantAppPermission(context, PACKAGE, ALERT_WINDOW);
        grantAppPermission(context, LAUNCHER_PACKAGE, PHONE_PERMISSION);
    }

    public static void grantAppPermission(Context context, String packageName, String permissionName) {
        PermissionInfo permissionInfo = null;
        PackageInfo packageInfo = null;
        PackageManager packageManager = context.getPackageManager();
        try {
            permissionInfo = packageManager.getPermissionInfo(permissionName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        PackageItemInfo groupInfo = permissionInfo;
        if (groupInfo == null || permissionInfo == null) {
            return;
        }
        if (permissionInfo.group != null) {
            try {
                groupInfo = packageManager.getPermissionGroupInfo(
                        permissionInfo.group, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        List<PermissionInfo> permissionInfos = null;
        if (groupInfo instanceof PermissionGroupInfo) {
            try {
                permissionInfos = packageManager.queryPermissionsByGroup(
                        groupInfo.name, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            packageInfo = packageManager.getPackageInfo(
                    packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
        }
        if (permissionInfos == null || permissionInfos.isEmpty()) {
            return;
        }
        final int permissionCount = packageInfo.requestedPermissions.length;
        for (int i = 0; i < permissionCount; i++) {
            String requestedPermission = packageInfo.requestedPermissions[i];
            for (PermissionInfo permission : permissionInfos) {
                Log.d("book", "permission.name:" + permission.name);
                Log.d("book", "requestedPermission:" + requestedPermission);
                if (requestedPermission.equals(permission.name)) {
                    grantRuntimePermission(packageManager, packageName, permission.name, getUserHandle(context));
                }
            }
        }
    }


    private static UserHandle getUserHandle(Context context) {
        int userId = 0;
        try {
            Method userIdMethod = Context.class.getMethod("getUserId");
            try {
                userId = (int) userIdMethod.invoke(context);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Parcel parcel = Parcel.obtain();
        parcel.writeInt(userId);
        UserHandle userHandle = new UserHandle(parcel);
        return userHandle;
    }

    private static void grantRuntimePermission(PackageManager packageManager, String packageName, String permissionName, UserHandle userHandle) {
        try {
            Method grantPermission = PackageManager.class.getMethod("grantRuntimePermission", String.class, String.class, UserHandle.class);
            Method updatePermissionFlags = PackageManager.class.getMethod("updatePermissionFlags", String.class, String.class, int.class, int.class, UserHandle.class);
            try {
                grantPermission.invoke(packageManager, packageName, permissionName, userHandle);
                updatePermissionFlags.invoke(packageManager, permissionName, packageName, FLAG_PERMISSION_USER_FIXED | FLAG_PERMISSION_USER_SET, 0, userHandle);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static void setLauncherCanDrawOverlay(Context context, boolean newState) {
        setCanDrawOverlay(context, newState, LAUNCHER_PACKAGE);
    }

    private static void setCanDrawOverlay(Context context, boolean newState, String packageName) {
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_DISABLED_COMPONENTS |
                            PackageManager.GET_UNINSTALLED_PACKAGES |
                            PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Exception when retrieving package:");
        }
        if (packageInfo == null) {
            return;
        }
        setMode(appOpsManager, packageInfo, newState, packageName);
    }

    private static void setMode(AppOpsManager appOpsManager, PackageInfo packageInfo, boolean newState, String packageName) {
        try {
            Method opMethod = AppOpsManager.class.getMethod("setMode", int.class, int.class, String.class, int.class);
            try {
                opMethod.invoke(appOpsManager, OP_SYSTEM_ALERT_WINDOW, packageInfo.applicationInfo.uid, packageName, newState ? AppOpsManager.MODE_ALLOWED : AppOpsManager.MODE_ERRORED);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
