"use strict";

const InAppBillingBridge = require("react-native").NativeModules.InAppBillingBridge;

class InAppBilling {
    static getProductDetails(productId) {
      return InAppBillingBridge.getProductDetails(productId);
    }

    static purchase(productId) {
      return InAppBillingBridge.purchase(productId);
    }

    static consumePurchase(productId) {
      return InAppBillingBridge.consumePurchase(productId);
    }
}

module.exports = InAppBilling;
