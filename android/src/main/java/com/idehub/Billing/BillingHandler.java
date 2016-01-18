package com.idehub.Billing;

import com.facebook.react.bridge.Callback;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.facebook.react.bridge.ReactApplicationContext;

public class BillingHandler implements BillingProcessor.IBillingHandler {

    IBillingInitialized mOnBillingInitializedCallback;
    IProductPurchased mOnProductPurchasedCallback;
    IBillingError mOnBillingErrorCallback;
    IPurchaseHistoryRestored mOnPurchaseHistoryRestoredCallback;
    BillingProcessor mBillingProcessor;

    public BillingHandler(IBillingInitialized onBillingInitializedCallback,
                          IProductPurchased onProductPurchasedCallback,
                          IBillingError onBillingErrorCallback,
                          IPurchaseHistoryRestored onPurchaseHistoryRestoredCallback) {
        mOnBillingInitializedCallback = onBillingInitializedCallback;
        mOnProductPurchasedCallback = onProductPurchasedCallback;
        mOnBillingErrorCallback = onBillingErrorCallback;
        mOnPurchaseHistoryRestoredCallback = onPurchaseHistoryRestoredCallback;
    }

    public BillingProcessor setupBillingProcessor(ReactApplicationContext _reactContext, String licenseKey) {
        mBillingProcessor = new BillingProcessor(_reactContext, licenseKey, null, this);
        return mBillingProcessor;
    }

    @Override
    public void onBillingInitialized() {
        if (mOnBillingInitializedCallback != null)
            mOnBillingInitializedCallback.invoke(mBillingProcessor);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        if (mOnProductPurchasedCallback != null)
            mOnProductPurchasedCallback.invoke(mBillingProcessor, productId, details);
    }
    @Override
    public void onBillingError(int errorCode, Throwable error) {
        if (mOnBillingErrorCallback != null)
            mOnBillingErrorCallback.invoke(mBillingProcessor, errorCode, error);
    }

    @Override
    public void onPurchaseHistoryRestored() {
        if (mOnPurchaseHistoryRestoredCallback != null)
            mOnPurchaseHistoryRestoredCallback.invoke(mBillingProcessor);
    }

    public interface IBillingInitialized {
        void invoke(BillingProcessor bp);
    }

    public interface IProductPurchased {
        void invoke(BillingProcessor bp, String productId, TransactionDetails details);
    }

    public interface IBillingError {
        void invoke(BillingProcessor bp, int errorCode, Throwable error);
    }

    public interface IPurchaseHistoryRestored {
        void invoke(BillingProcessor bp);
    }
}
