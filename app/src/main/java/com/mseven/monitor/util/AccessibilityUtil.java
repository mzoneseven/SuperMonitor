package com.mseven.monitor.util;

/**
 * Created by mseven on 3/27/17.
 */

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccessibilityUtil {

    static final String APP_LOCK_SERVICE = "com.mseven.monitor/com.mseven.monitor.WindowChangeService";
    private static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
    final static TextUtils.SimpleStringSplitter sStringColonSplitter =
            new TextUtils.SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
    private static final Set<ComponentName> sInstalledServices = new HashSet<>();

    private static Set<ComponentName> getEnabledServicesFromSettings(Context context) {
        final String enabledServicesSetting = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null) {
            return Collections.emptySet();
        }
        final Set<ComponentName> enabledServices = new HashSet<>();
        final TextUtils.SimpleStringSplitter colonSplitter = sStringColonSplitter;
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            final String componentNameString = colonSplitter.next();
            final ComponentName enabledService = ComponentName.unflattenFromString(
                    componentNameString);
            if (enabledService != null) {
                enabledServices.add(enabledService);
            }
        }
        return enabledServices;
    }

    private static void loadInstalledServices(Context context) {
        Set<ComponentName> installedServices = sInstalledServices;
        installedServices.clear();
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> installedServiceInfos = accessibilityManager.getInstalledAccessibilityServiceList();
        if (installedServiceInfos == null) {
            return;
        }
        final int installedServiceInfoCount = installedServiceInfos.size();
        for (int i = 0; i < installedServiceInfoCount; i++) {
            ResolveInfo resolveInfo = installedServiceInfos.get(i).getResolveInfo();
            ComponentName installedService = new ComponentName(
                    resolveInfo.serviceInfo.packageName,
                    resolveInfo.serviceInfo.name);
            installedServices.add(installedService);
        }
    }


    private static void updateAccessibility(Context context, boolean enabled, String preferenceKey) {
        Set<ComponentName> enabledServices = getEnabledServicesFromSettings(context);
        if (enabledServices == (Set<?>) Collections.emptySet()) {
            enabledServices = new HashSet<ComponentName>();
        }
        ComponentName toggledService = ComponentName.unflattenFromString(preferenceKey);
        boolean accessibilityEnabled = false;
        if (enabled) {
            enabledServices.add(toggledService);
            accessibilityEnabled = true;
        } else {
            enabledServices.remove(toggledService);
            Set<ComponentName> installedServices = sInstalledServices;
            for (ComponentName enabledService : enabledServices) {
                if (installedServices.contains(enabledService)) {
                    // Disabling the last service disables accessibility.
                    accessibilityEnabled = true;
                    break;
                }
            }
        }
        // Update the enabled services setting.
        StringBuilder enabledServicesBuilder = new StringBuilder();
        for (ComponentName enabledService : enabledServices) {
            enabledServicesBuilder.append(enabledService.flattenToString());
            enabledServicesBuilder.append(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
        }
        final int enabledServicesBuilderLength = enabledServicesBuilder.length();
        if (enabledServicesBuilderLength > 0) {
            enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1);
        }
        Settings.Secure.putString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                enabledServicesBuilder.toString());

        // Update accessibility enabled.
        Settings.Secure.putInt(context.getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, accessibilityEnabled ? 1 : 0);
    }

    public static void updateLauncherAccessibility(Context context, boolean enabled) {
        try {
            loadInstalledServices(context);
            updateAccessibility(context, enabled, APP_LOCK_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.d("book", e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }
        return false;
    }
}
