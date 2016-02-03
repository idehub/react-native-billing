"use strict";

const InAppBillingBridge = require("react-native").NativeModules.InAppBillingBridge;

class InAppBilling {
    static open() {
      return InAppBillingBridge.open();
    }

    static close() {
      return InAppBillingBridge.close();
    }

    static getProductDetails(productId) {
      return InAppBillingBridge.getProductDetails([productId])
        .then((arr) => {
          if (array.length > 0) {
            return Promise.resolve(arr[0]);
          } else {
            return Promise.reject("Could not find details.");
          }
        })
        .catch((error) => {
          return Promise.reject(error);
        });
    }

    static getProductDetailsArray(productIds) {
      return InAppBillingBridge.getProductDetails(productIds);
    }

    static purchase(productId) {
      return InAppBillingBridge.purchase(productId);
    }

    static consumePurchase(productId) {
      return InAppBillingBridge.consumePurchase(productId);
    }
}

module.exports = InAppBilling;
