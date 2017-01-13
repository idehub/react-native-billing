package com.idehub.Billing;

import android.app.Activity;
import android.content.Intent;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.anjlab.android.iab.v3.PurchaseData;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InAppBillingBridge extends ReactContextBaseJavaModule implements ActivityEventListener, BillingProcessor.IBillingHandler {
    ReactApplicationContext _reactContext;
    String LICENSE_KEY = null;
    BillingProcessor bp;

    public InAppBillingBridge(ReactApplicationContext reactContext, String licenseKey) {
        super(reactContext);
        _reactContext = reactContext;
        LICENSE_KEY = licenseKey;

        reactContext.addActivityEventListener(this);
    }

    public InAppBillingBridge(ReactApplicationContext reactContext) {
        super(reactContext);
        _reactContext = reactContext;
        int keyResourceId = _reactContext
                .getResources()
                .getIdentifier("RNB_GOOGLE_PLAY_LICENSE_KEY", "string", _reactContext.getPackageName());
        LICENSE_KEY = _reactContext.getString(keyResourceId);

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
            if (bp == null) {
                clearPromises();
                if (putPromise(PromiseConstants.OPEN, promise)) {
                    try {
                        bp = new BillingProcessor(_reactContext, LICENSE_KEY, this);
                    } catch (Exception ex) {
                        rejectPromise(PromiseConstants.OPEN, "Failure on open: " + ex.getMessage());
                    }
                } else {
                    promise.reject("EUNSPECIFIED", "Previous open operation is not resolved.");
                }
            } else {
                promise.reject("EUNSPECIFIED", "Channel is already open. Call close() on InAppBilling to be able to open().");
            }
        } else {
            promise.reject("EUNSPECIFIED", "InAppBilling is not available. InAppBilling will not work/test on an emulator, only a physical Android device.");
        }
    }

    @ReactMethod
    public void close(final Promise promise){
        if (bp != null) {
            bp.release();
            bp = null;
        }

        clearPromises();
        promise.resolve(true);
    }

    @ReactMethod
    public void loadOwnedPurchasesFromGoogle(final Promise promise){
      if (bp != null) {
          bp.loadOwnedPurchasesFromGoogle();
          promise.resolve(true);
      } else {
          promise.reject("EUNSPECIFIED", "Channel is not opened. Call open() on InAppBilling.");
      }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        if (details != null && productId.equals(details.purchaseInfo.purchaseData.productId))
        {
            try {
                WritableMap map = mapTransactionDetails(details);
                resolvePromise(PromiseConstants.PURCHASE_OR_SUBSCRIBE, map);
            } catch (Exception ex) {
                rejectPromise(PromiseConstants.PURCHASE_OR_SUBSCRIBE, "Failure on purchase or subscribe callback: " + ex.getMessage());
            }
        } else {
            rejectPromise(PromiseConstants.PURCHASE_OR_SUBSCRIBE, "Failure on purchase or subscribe callback. Details were empty.");
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        if (hasPromise(PromiseConstants.PURCHASE_OR_SUBSCRIBE))
            rejectPromise(PromiseConstants.PURCHASE_OR_SUBSCRIBE, "Purchase or subscribe failed with error: " + errorCode);
    }

    @ReactMethod
    public void purchase(final String productId, final String developerPayload, final Promise promise){
        if (bp != null) {
            if (putPromise(PromiseConstants.PURCHASE_OR_SUBSCRIBE, promise)) {
                boolean purchaseProcessStarted = bp.purchase(getCurrentActivity(), productId, developerPayload);
                if (!purchaseProcessStarted)
                    rejectPromise(PromiseConstants.PURCHASE_OR_SUBSCRIBE, "Could not start purchase process.");
            } else {
                promise.reject("EUNSPECIFIED", "Previous purchase or subscribe operation is not resolved.");
            }
        } else {
            promise.reject("EUNSPECIFIED", "Channel is not opened. Call open() on InAppBilling.");
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
                    promise.reject("EUNSPECIFIED", "Could not consume purchase");
            } catch (Exception ex) {
                promise.reject("EUNSPECIFIED", "Failure on consume: " + ex.getMessage());
            }
        } else {
            promise.reject("EUNSPECIFIED", "Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @ReactMethod
    public void subscribe(final String productId, final String developerPayload, final Promise promise){
        if (bp != null) {
            if (putPromise(PromiseConstants.PURCHASE_OR_SUBSCRIBE, promise)) {
                boolean subscribeProcessStarted = bp.subscribe(getCurrentActivity(), productId, developerPayload);
                if (!subscribeProcessStarted)
                    rejectPromise(PromiseConstants.PURCHASE_OR_SUBSCRIBE, "Could not start subscribe process.");
            } else {
                promise.reject("EUNSPECIFIED", "Previous subscribe or purchase operation is not resolved.");
            }
        } else {
            promise.reject("EUNSPECIFIED", "Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @ReactMethod
    public void updateSubscription(final ReadableArray oldProductIds, final String productId, final Promise promise){
        if (bp != null) {
            if (putPromise(PromiseConstants.PURCHASE_OR_SUBSCRIBE, promise)) {
                ArrayList<String> oldProductIdList = new ArrayList<>();
                for (int i = 0; i < oldProductIds.size(); i++) {
                    oldProductIdList.add(oldProductIds.getString(i));
                }

                boolean updateProcessStarted = bp.updateSubscription(getCurrentActivity(), oldProductIdList, productId);

                if (!updateProcessStarted)
                    rejectPromise(PromiseConstants.PURCHASE_OR_SUBSCRIBE, "Could not start updateSubscription process.");
            } else {
                promise.reject("EUNSPECIFIED", "Previous subscribe or purchase operation is not resolved.");
            }
        } else {
            promise.reject("EUNSPECIFIED", "Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @ReactMethod
    public void isSubscribed(final String productId, final Promise promise){
        if (bp != null) {
            boolean subscribed = bp.isSubscribed(productId);
            promise.resolve(subscribed);
        } else {
            promise.reject("EUNSPECIFIED", "Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @ReactMethod
    public void isPurchased(final String productId, final Promise promise){
        if (bp != null) {
            boolean purchased = bp.isPurchased(productId);
            promise.resolve(purchased);
        } else {
            promise.reject("EUNSPECIFIED", "Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @ReactMethod
    public void listOwnedProducts(final Promise promise){
        if (bp != null) {
            List<String> purchasedProductIds = bp.listOwnedProducts();
            WritableArray arr = Arguments.createArray();

            for (int i = 0; i < purchasedProductIds.size(); i++) {
                arr.pushString(purchasedProductIds.get(i));
            }

            promise.resolve(arr);
        } else {
            promise.reject("EUNSPECIFIED", "Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @ReactMethod
    public void listOwnedSubscriptions(final Promise promise){
        if (bp != null) {
            List<String> ownedSubscriptionsIds = bp.listOwnedSubscriptions();
            WritableArray arr = Arguments.createArray();

            for (int i = 0; i < ownedSubscriptionsIds.size(); i++) {
                arr.pushString(ownedSubscriptionsIds.get(i));
            }

            promise.resolve(arr);
        } else {
            promise.reject("EUNSPECIFIED", "Channel is not opened. Call open() on InAppBilling.");
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
                    promise.reject("EUNSPECIFIED", "Details was not found.");
                }
            } catch (Exception ex) {
                promise.reject("EUNSPECIFIED", "Failure on getting product details: " + ex.getMessage());
            }
        } else {
            promise.reject("EUNSPECIFIED", "Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @ReactMethod
    public void getSubscriptionDetails(final ReadableArray productIds, final Promise promise) {
        if (bp != null) {
            try {
                ArrayList<String> productIdList = new ArrayList<>();
                for (int i = 0; i < productIds.size(); i++) {
                    productIdList.add(productIds.getString(i));
                }

                List<SkuDetails> details = bp.getSubscriptionListingDetails(productIdList);

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
                    promise.reject("EUNSPECIFIED", "Details was not found.");
                }
            } catch (Exception ex) {
                promise.reject("EUNSPECIFIED", "Failure on getting product details: " + ex.getMessage());
            }
        } else {
            promise.reject("EUNSPECIFIED", "Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @ReactMethod
    public void getPurchaseTransactionDetails(final String productId, final Promise promise) {
        if (bp != null) {
            TransactionDetails details = bp.getPurchaseTransactionDetails(productId);
            if (details != null && productId.equals(details.purchaseInfo.purchaseData.productId))
            {
                  WritableMap map = mapTransactionDetails(details);
                  promise.resolve(map);
            } else {
                promise.reject("EUNSPECIFIED", "Could not find transaction details for productId.");
            }
        } else {
            promise.reject("EUNSPECIFIED", "Channel is not opened. Call open() on InAppBilling.");
        }
    }

    @ReactMethod
    public void getSubscriptionTransactionDetails(final String productId, final Promise promise) {
        if (bp != null) {
            TransactionDetails details = bp.getSubscriptionTransactionDetails(productId);
            if (details != null && productId.equals(details.purchaseInfo.purchaseData.productId))
            {
                  WritableMap map = mapTransactionDetails(details);
                  promise.resolve(map);
            } else {
                promise.reject("EUNSPECIFIED", "Could not find transaction details for productId.");
            }
        } else {
            promise.reject("EUNSPECIFIED", "Channel is not opened. Call open() on InAppBilling.");
        }
    }

    private WritableMap mapTransactionDetails(TransactionDetails details) {
        WritableMap map = Arguments.createMap();

        map.putString("receiptData", details.purchaseInfo.responseData.toString());

        if (details.purchaseInfo.signature != null)
            map.putString("receiptSignature", details.purchaseInfo.signature.toString());

        PurchaseData purchaseData = details.purchaseInfo.purchaseData;

        map.putString("productId", purchaseData.productId);
        map.putString("orderId", purchaseData.orderId);
        map.putString("purchaseToken", purchaseData.purchaseToken);
        map.putString("purchaseTime", purchaseData.purchaseTime == null
          ? "" : purchaseData.purchaseTime.toString());
        map.putString("purchaseState", purchaseData.purchaseState == null
          ? "" : purchaseData.purchaseState.toString());


        if (purchaseData.developerPayload != null)
            map.putString("developerPayload", purchaseData.developerPayload);

        return map;
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

    @Deprecated
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (bp != null)
            bp.handleActivityResult(requestCode, resultCode, intent);
    }

    public void onActivityResult(final Activity activity, final int requestCode, final int resultCode, final Intent intent) {
        if (bp != null)
            bp.handleActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onNewIntent(Intent intent){

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
            promise.reject("EUNSPECIFIED", reason);
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

    synchronized Boolean hasPromise(String key) {
        return mPromiseCache.containsKey(key);
    }

    synchronized void clearPromises() {
        mPromiseCache.clear();
    }
}
