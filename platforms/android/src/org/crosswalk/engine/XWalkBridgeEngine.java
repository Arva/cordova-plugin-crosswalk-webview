package org.crosswalk.engine;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.View;
import android.webkit.ValueCallback;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.ICordovaCookieManager;
import org.apache.cordova.NativeToJsMessageQueue;
import org.apache.cordova.PluginManager;
import org.apache.cordova.engine.SystemWebViewEngine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import nl.bizboard.xw.xw.MainActivity;

/**
 * Created by lundfall on 26/07/2017.
 */

public class XWalkBridgeEngine implements CordovaWebViewEngine {

    CordovaWebViewEngine underlyingEngine;
    CordovaPreferences preferences;
    Context context;

    public XWalkBridgeEngine(Context context, CordovaPreferences preferences) {
        this.preferences = preferences;
        this.context = context;
        if (XWalkBridgeEngine.shouldMakeXwalkWebView(context)) {
            underlyingEngine = new XWalkWebViewEngine(context, preferences);
        } else {
            underlyingEngine = new SystemWebViewEngine(context, preferences);
        }
    }

    @Override
    public void init(CordovaWebView parentWebView, CordovaInterface cordova, Client client, CordovaResourceApi resourceApi, PluginManager pluginManager, NativeToJsMessageQueue nativeToJsMessageQueue) {
        underlyingEngine.init(parentWebView, cordova, client, resourceApi, pluginManager, nativeToJsMessageQueue);
    }

    @Override
    public CordovaWebView getCordovaWebView() {
        return underlyingEngine.getCordovaWebView();
    }

    @Override
    public ICordovaCookieManager getCookieManager() {
        return underlyingEngine.getCookieManager();
    }

    @Override
    public View getView() {
        return underlyingEngine.getView();
    }

    @Override
    public void loadUrl(String url, boolean clearNavigationStack) {
        underlyingEngine.loadUrl(url, clearNavigationStack);
    }

    @Override
    public void stopLoading() {
        underlyingEngine.stopLoading();
    }

    @Override
    public String getUrl() {
        return underlyingEngine.getUrl();
    }

    @Override
    public void clearCache() {
        underlyingEngine.clearCache();
    }

    @Override
    public void clearHistory() {
        underlyingEngine.clearHistory();
    }

    @Override
    public boolean canGoBack() {
        return underlyingEngine.canGoBack();
    }

    @Override
    public boolean goBack() {
        return underlyingEngine.goBack();
    }

    @Override
    public void setPaused(boolean value) {
        underlyingEngine.setPaused(value);
    }

    @Override
    public void destroy() {
        underlyingEngine.destroy();
    }

    @Override
    public void evaluateJavascript(String js, ValueCallback<String> callback) {
        underlyingEngine.evaluateJavascript(js, callback);
    }

    static private boolean checkedShouldMakeXwalkWebView = false;
    static private boolean cachedShouldMakeXwalkWebView = false;

    static public boolean shouldMakeXwalkWebView(Context context) {

        if (XWalkBridgeEngine.checkedShouldMakeXwalkWebView) {
            return XWalkBridgeEngine.cachedShouldMakeXwalkWebView;
        }

        XWalkBridgeEngine.checkedShouldMakeXwalkWebView = true;
        
        // only for Nougat and newer versions
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            XWalkBridgeEngine.cachedShouldMakeXwalkWebView = false;
            return false;
        }
        
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo("\"com.google.android.webview\"", 0);
        } catch (PackageManager.NameNotFoundException e) {
            XWalkBridgeEngine.cachedShouldMakeXwalkWebView = true;
            return true;
        };
        int enabledSetting = packageManager.getApplicationEnabledSetting("com.google.android.webview");
        if (enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_ENABLED && enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
            XWalkBridgeEngine.cachedShouldMakeXwalkWebView = true;
            return true;
        }
        String majorVersionNumber = "0";
        int systemWebViewVersion = 0;
        try {
            String versionName = context.getPackageManager().getPackageInfo("com.google.android.webview", 0).versionName;
            majorVersionNumber = versionName.split("\\.")[0];
            systemWebViewVersion = Integer.parseInt(majorVersionNumber);
            /* Parse exceptions generate true */
        } catch (Exception e) {
            XWalkBridgeEngine.cachedShouldMakeXwalkWebView = true;
            return true;
        }

        /* Xwalk will make a web view with chrome version 55, so if the systemWebView has a higher version, it should be used */
        XWalkBridgeEngine.cachedShouldMakeXwalkWebView = systemWebViewVersion < 55;
        return XWalkBridgeEngine.cachedShouldMakeXwalkWebView;
    }
}
