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
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;

public class InAppBillingBridge extends ReactContextBaseJavaModule implements ActivityEventListener, BillingProcessor.IBillingHandler {
    ReactApplicationContext _reactContext;
    String LICENSE_KEY = null;
    final Activity _activity;
    BillingProcessor bp;
    Promise mInitializationPromise;
    Promise mPurchasePromise;

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
        if (mInitializationPromise != null) {
            mInitializationPromise.resolve(true);
            mInitializationPromise = null;
        }
    }

    @ReactMethod
    public void open(final Promise promise){
        if (isIabServiceAvailable()) {
            mInitializationPromise = promise;
            bp = new BillingProcessor(_reactContext, LICENSE_KEY, this);
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
        if (mPurchasePromise != null) {
            if (productId == productId && details != null)
            {
                WritableMap map = Arguments.createMap();

                map.putString("receiptData", details.purchaseInfo.responseData.toString());
                map.putString("receiptSignature", details.purchaseInfo.signature.toString());

                map.putString("productId", details.productId);
                map.putString("orderId", details.orderId);
                map.putString("purchaseToken", details.purchaseToken);
                map.putString("purchaseTime", details.purchaseTime.toString());

                ResponseData responseData = details.purchaseInfo.parseResponseData();
                map.putString("purchaseState", responseData.purchaseState.toString());

                mPurchasePromise.resolve(map);
            }
            
            mPurchasePromise = null;
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        if (mPurchasePromise != null) {
            mPurchasePromise.reject("Purchase failed with error " + errorCode);
            mPurchasePromise = null;
        }
    }

    @ReactMethod
    public void purchase(final String productId, final Promise promise){
        mPurchasePromise = promise;
        if (bp != null) {
            boolean purchaseProcessStarted = bp.purchase(_activity, productId);
            if (!purchaseProcessStarted)
                promise.reject("Could not start purchase process.");
        } else {
            promise.reject("Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @ReactMethod
    public void consumePurchase(final String productId, final Promise promise) {
        if (bp != null) {
            boolean consumed = bp.consumePurchase(productId);
            if (consumed)
                promise.resolve(true);
            else
                promise.reject("Could not consume purchase");
        } else {
            promise.reject("Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @ReactMethod
    public void getProductDetails(final String productId, final Promise promise) {
        if (bp != null) {
            SkuDetails details = bp.getPurchaseListingDetails(productId);
            if (details != null) {
                WritableMap map = Arguments.createMap();

                map.putString("productId", details.productId);
                map.putString("title", details.title);
                map.putString("description", details.description);
                map.putBoolean("isSubscription", details.isSubscription);
                map.putString("currency", details.currency);
                map.putDouble("priceValue", details.priceValue);
                map.putString("priceText", details.priceText);

                promise.resolve(map);
            }
            else {
                promise.reject("Details was not found.");
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
}
