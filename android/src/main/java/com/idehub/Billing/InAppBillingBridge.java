package com.idehub.Billing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.anjlab.android.iab.v3.PurchaseInfo.ResponseData;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.idehub.Billing.PromiseConstants;

public class InAppBillingBridge extends ReactContextBaseJavaModule implements ActivityEventListener, BillingProcessor.IBillingHandler {
    ReactApplicationContext _reactContext;
    String LICENSE_KEY = null;
    final Activity _activity;
    BillingProcessor bp;

    public InAppBillingBridge(ReactApplicationContext reactContext, String licenseKey, Activity activity) {
        super(reactContext);
        _reactContext = reactContext;
        LICENSE_KEY = licenseKey;
        _activity = activity;

        reactContext.addActivityEventListener(this);
    }

    public InAppBillingBridge(ReactApplicationContext reactContext, Activity activity) {
        super(reactContext);
        _reactContext = reactContext;
        int keyResourceId = _reactContext
                .getResources()
                .getIdentifier("RNB_GOOGLE_PLAY_LICENSE_KEY", "string", _reactContext.getPackageName());
        LICENSE_KEY = _reactContext.getString(keyResourceId);
        _activity = activity;

        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "InAppBillingBridge";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        return constants;
    }

    @Override
    public void onBillingInitialized() {
        resolvePromise(PromiseConstants.OPEN, true);
    }

    @ReactMethod
    public void open(final Promise promise){
        if (isIabServiceAvailable()) {
            if (putPromise(PromiseConstants.OPEN, promise)) {
                try {
                    bp = new BillingProcessor(_reactContext, LICENSE_KEY, this);
                } catch (Exception ex) {
                    rejectPromise(PromiseConstants.OPEN, "Failure on open: " + ex.getMessage());
                }
            } else {
                promise.reject("Previous open operation is not resolved.");
            }
        } else {
            promise.reject("InApp billing is not available.");
        }
    }

    @ReactMethod
    public void close(final Promise promise){
        if (bp != null) {
            bp.release();
            bp = null;
        }

        promise.resolve(true);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        if (productId == productId && details != null)
        {
            try {
                WritableMap map = Arguments.createMap();

                map.putString("receiptData", details.purchaseInfo.responseData.toString());

                if (details.purchaseInfo.signature != null)
                    map.putString("receiptSignature", details.purchaseInfo.signature.toString());

                map.putString("productId", details.productId);
                map.putString("orderId", details.orderId);
                map.putString("purchaseToken", details.purchaseToken);
                map.putString("purchaseTime", details.purchaseTime.toString());

                ResponseData responseData = details.purchaseInfo.parseResponseData();
                map.putString("purchaseState", responseData.purchaseState.toString());

                resolvePromise(PromiseConstants.PURCHASE, map);
            } catch (Exception ex) {
                rejectPromise(PromiseConstants.PURCHASE, "Failure on purchase-callback: " + ex.getMessage());
            }
        } else {
            rejectPromise(PromiseConstants.PURCHASE, "");
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        rejectPromise(PromiseConstants.PURCHASE, "Purchase failed with error " + errorCode);
    }

    @ReactMethod
    public void purchase(final String productId, final Promise promise){
        if (bp != null) {
            if (putPromise(PromiseConstants.PURCHASE, promise)) {
                boolean purchaseProcessStarted = bp.purchase(_activity, productId);
                if (!purchaseProcessStarted)
                    rejectPromise(PromiseConstants.PURCHASE + productId, "Could not start purchase process.");
            } else {
                promise.reject("Previous purchase operation is not resolved.");
            }
        } else {
            promise.reject("Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @ReactMethod
    public void consumePurchase(final String productId, final Promise promise) {
        if (bp != null) {
            try {
                boolean consumed = bp.consumePurchase(productId);
                if (consumed)
                    promise.resolve(true);
                else
                    promise.reject("Could not consume purchase");
            } catch (Exception ex) {
                promise.reject("Failure on purchase-callback: " + ex.getMessage());
            }
        } else {
            promise.reject("Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @ReactMethod
    public void getProductDetails(final ReadableArray productIds, final Promise promise) {
        if (bp != null) {
            try {
                ArrayList<String> productIdList = new ArrayList<>();
                for (int i = 0; i < productIds.size(); i++) {
                    productIdList.add(productIds.getString(i));
                }

                List<SkuDetails> details = bp.getPurchaseListingDetails(productIdList);

                if (details != null) {
                    WritableArray arr = Arguments.createArray();
                    for (SkuDetails detail : details) {
                        if (detail != null) {
                            WritableMap map = Arguments.createMap();

                            map.putString("productId", detail.productId);
                            map.putString("title", detail.title);
                            map.putString("description", detail.description);
                            map.putBoolean("isSubscription", detail.isSubscription);
                            map.putString("currency", detail.currency);
                            map.putDouble("priceValue", detail.priceValue);
                            map.putString("priceText", detail.priceText);
                            arr.pushMap(map);
                        }
                    }

                    promise.resolve(arr);
                } else {
                    promise.reject("Details was not found.");
                }
            } catch (Exception ex) {
                promise.reject("Failure on getting product details: " + ex.getMessage());
            }
        } else {
            promise.reject("Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }

    private Boolean isIabServiceAvailable() {
        return BillingProcessor.isIabServiceAvailable(_reactContext);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (bp != null)
            bp.handleActivityResult(requestCode, resultCode, intent);
    }

    HashMap<String, Promise> mPromiseCache = new HashMap<>();

    synchronized void resolvePromise(String key, Object value) {
        if (mPromiseCache.containsKey(key)) {
            Promise promise = mPromiseCache.get(key);
            promise.resolve(value);
            mPromiseCache.remove(key);
        }
    }

    synchronized void rejectPromise(String key, String reason) {
        if (mPromiseCache.containsKey(key)) {
            Promise promise = mPromiseCache.get(key);
            promise.reject(reason);
            mPromiseCache.remove(key);
        }
    }

    synchronized Boolean putPromise(String key, Promise promise) {
        if (!mPromiseCache.containsKey(key)) {
            mPromiseCache.put(key, promise);
            return true;
        }
        return false;
    }
}
