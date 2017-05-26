package com.idehub.Billing;

import android.content.Intent;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InAppBillingBridgePackage implements ReactPackage {

    public InAppBillingBridgePackage(String licenseKey) {
        _licenseKey = licenseKey;
        _licenseKeySetInConstructor = true;
    }

    public InAppBillingBridgePackage() {
    }

    private String _licenseKey;
    private Boolean _licenseKeySetInConstructor = false;
    private InAppBillingBridge billingBridge;

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
      	if (!_licenseKeySetInConstructor) {
            billingBridge = new InAppBillingBridge(reactContext);
      	} else {
            billingBridge = new InAppBillingBridge(reactContext, _licenseKey);
        }
        modules.add(billingBridge);
        return modules;
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Arrays.<ViewManager>asList();
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (billingBridge != null) {
            billingBridge.onActivityResult(requestCode, resultCode, intent);
        }
    }
}
