package com.idehub.Billing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;

import com.idehub.Billing.BillingHandler;

public class InAppBillingBridge extends ReactContextBaseJavaModule {
    ReactApplicationContext _reactContext;
    String LICENSE_KEY = null;
    final Activity _activity;

	public InAppBillingBridge(ReactApplicationContext reactContext, String licenseKey, Activity activity) {
        super(reactContext);
        _reactContext = reactContext;
        LICENSE_KEY = licenseKey;
        _activity = activity;
    }

    public InAppBillingBridge(ReactApplicationContext reactContext, Activity activity) {
        super(reactContext);
        _reactContext = reactContext;
        int keyResourceId =
                _reactContext.getResources()
                        .getIdentifier("RNP_GOOGLE_PLAY_TOKEN", "string", _reactContext.getPackageName());
        LICENSE_KEY = _reactContext.getString(keyResourceId);
        _activity = activity;
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

    @ReactMethod
    public void purchase(final String productId, final Promise promise){
        try {
            if (isIabServiceAvailable()) {
                BillingHandler handler = new BillingHandler(
                        new BillingHandler.IBillingInitialized() {
                            @Override
                            public void invoke(BillingProcessor bp) {
                                boolean purchaseProcessStarted = bp.purchase(_activity, productId);
                                if (!purchaseProcessStarted)
                                    promise.reject("Could not start purchase process.");
                            }
                        },
                        new BillingHandler.IProductPurchased() {
                            @Override
                            public void invoke(BillingProcessor bp, String pId, TransactionDetails details) {
                                if (pId == productId)
                                {
                                    promise.resolve(true);
                                }
                                bp.release();
                            }
                        },
                        new BillingHandler.IBillingError() {
                            @Override
                            public void invoke(BillingProcessor bp, int errorCode, Throwable error) {
                                promise.reject("Purchase failed with error " + errorCode);
                                bp.release();
                            }
                        }, null);
                handler.setupBillingProcessor(_reactContext, LICENSE_KEY, MERCHANT_ID);
            } else  {
                promise.reject("InApp billing is not available.");
            }
        } catch (Exception e) {
            promise.reject("Unknown error.");
        }
    }

    @ReactMethod
    public void consumePurchase(final String productId, final Promise promise){
        try {
            if (isIabServiceAvailable()) {
                BillingHandler handler = new BillingHandler(
                        new BillingHandler.IBillingInitialized() {
                            @Override
                            public void invoke(BillingProcessor bp) {
                                boolean consumed = bp.consumePurchase(productId);
                                if (consumed)
                                  promise.resolve(true);
                                else
                                  promise.reject("Could not consume purchase");

                                bp.release();
                            }
                        },
                        null, null, null);
                handler.setupBillingProcessor(_reactContext, LICENSE_KEY, MERCHANT_ID);
            } else  {
                promise.reject("InApp billing is not available.");
            }
        } catch (Exception e) {
            promise.reject("Unknown error.");
        }
    }

    @ReactMethod
    public void getProductDetails(final String productId, final Promise promise) {
        try {
            if (isIabServiceAvailable()) {
                BillingHandler handler = new BillingHandler(
                    new BillingHandler.IBillingInitialized() {
                        @Override
                        public void invoke(BillingProcessor bp) {
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
                            bp.release();
                        }
                    }, null, null, null);
                handler.setupBillingProcessor(_reactContext, LICENSE_KEY, MERCHANT_ID);
            } else  {
                promise.reject("InApp billing is not available.");
            }
        } catch (Exception e) {
            promise.reject("Unknown error.");
        }
    }

    private Boolean isIabServiceAvailable() {
        return BillingProcessor.isIabServiceAvailable(_reactContext);
    }
}
