InApp Billing for Android [![npm version](https://img.shields.io/npm/v/react-native-billing.svg)](https://www.npmjs.com/package/react-native-billing)
=============
**React Native Billing** is built to provide an easy interface to InApp Billing on **Android**, accomplished by wrapping [anjlab's InApp Billing library](https://github.com/anjlab/android-inapp-billing-v3).

```javascript
const InAppBilling = require("react-native-billing");

InAppBilling.open()
.then(() => InAppBilling.purchase('android.test.purchased'))
.then((details) => {
  console.log("You purchased: ", details)
  return InAppBilling.close()
})
.catch((err) => {
  console.log(err);
});
```

## Installation and linking
1. `npm install --save react-native-billing` or `yarn add react-native-billing`
2. `react-native link react-native-billing`

With this, the `link`command will do most of the heavy lifting for native linking. **But**, you will still need add your Google Play license key to the `strings.xml` (step 5). If you are using a React Native version less than v18.0 you will also have to do step 4.3 (override `onActivityResult`).

## Manual installation Android

1. `npm install --save react-native-billing`
2. Add the following in `android/setting.gradle`

  ```gradle
  ...
  include ':react-native-billing', ':app'
  project(':react-native-billing').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-billing/android')
  ```

3. And the following in `android/app/build.gradle`

  ```gradle
  ...
  dependencies {
      ...
      compile project(':react-native-billing')
  }
  ```

4. Update MainActivity or MainApplication depending on React Native version.
  - React Native version >= 0.29
    Edit `MainApplication.java`.
    1. Add `import com.idehub.Billing.InAppBillingBridgePackage;`
    2. Register package:
    ```java
    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
        new MainReactPackage(),
        // add package here
        new InAppBillingBridgePackage()
      );
    }
    ```
  - React Native version < 0.29
    Edit `MainActivity.java`. Step 4.3 is only required if you are using a lower React Native version than 18.0 and/or your `MainActivity` class does not inherit from `ReactActivity`.
    1. Add `import com.idehub.Billing.InAppBillingBridgePackage;`
    2. Register package in ReactInstanceManager: `.addPackage(new InAppBillingBridgePackage())`
    3. Override `onActivityResult`:
    ```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          mReactInstanceManager.onActivityResult(requestCode, resultCode, data);
    }
    ```

     Larger example:

    ```java
    // Step 1; import package:
    import com.idehub.Billing.InAppBillingBridgePackage;

    public class MainActivity extends Activity implements DefaultHardwareBackBtnHandler {
        ...

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            ...

            mReactInstanceManager = ReactInstanceManager.builder()
                    .setApplication(getApplication())
                    .setBundleAssetName("index.android.bundle")
                    .setJSMainModuleName("index.android")
                    .addPackage(new MainReactPackage())
                    // Step 2; register package
                    .addPackage(new InAppBillingBridgePackage())
                    .setUseDeveloperSupport(BuildConfig.DEBUG)
                    .setInitialLifecycleState(LifecycleState.RESUMED)
                    .build();

            ...
        }
        // Step 3: For RN < v0.18, override onActivityResult
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            mReactInstanceManager.onActivityResult(requestCode, resultCode, data);
        }
        ...
    ```
5. Add your Google Play license key as a line to your `android/app/src/main/res/values/strings.xml` with the name `RNB_GOOGLE_PLAY_LICENSE_KEY`. For example:
```xml
<string name="RNB_GOOGLE_PLAY_LICENSE_KEY">YOUR_GOOGLE_PLAY_LICENSE_KEY_HERE</string>
```
Alternatively, you can add your license key as a parameter when registering the `InAppBillingBridgePackage`, like so:
```java
.addPackage(new InAppBillingBridgePackage("YOUR_LICENSE_KEY"))
```
or for React Native 29+
```java
new InAppBillingBridgePackage("YOUR_LICENSE_KEY")
```


## Testing with static responses
If you want to test with static responses, you can use reserved productids defined by Google. These are:
* android.test.purchased
* android.test.canceled
* android.test.refunded
* android.test.item_unavailable

If you want to test with these productids, you will have to use a `null` license key. This is because your actual license key will not validate when using these productids.

In order to do this send in `null` as parameter, along with your Activity-instance, when registering the package:
`.addPackage(new InAppBillingBridgePackage(null, this))`

[See the Google Play docs for more info on static responses](http://developer.android.com/google/play/billing/billing_testing.html#billing-testing-static).

For instance to purchase and consume the static `android.test.purchased` products, with async/await (you can chain the promise) :

```js
// To be sure the service is close before opening it
async pay() {
  await InAppBilling.close();
  try {
    await InAppBilling.open();
    if (!await InAppBilling.isPurchased(productId)) {
      const details = await InAppBilling.purchase(productId);
      console.log('You purchased: ', details);
    }
    const transactionStatus = await InAppBilling.getPurchaseTransactionDetails(productId);
    console.log('Transaction Status', transactionStatus);
    const productDetails = await InAppBilling.getProductDetails(productId);
    console.log(productDetails);
  } catch (err) {
    console.log(err);
  } finally {
    await InAppBilling.consumePurchase(productId);
    await InAppBilling.close();
  }
}
```

## Testing with your own In-app products
Testing with static responses is limited, because you are only able to test the `purchase` function. Therefore, testing with real In-app products is recommended. But before that is possible, you need to do the following:
* I will assume you've already created your Google Play Developer account and an application there.
* Now you need to create an In-app product under your application at the Google Play Developer Console and activate it (press the button at the top right).
* Assuming you have installed this module (InApp Billing), you can write the JS code as explained in the Javascript API section. I suggest you to use `getProductDetails` function to see if it's the product is retrieved.
* When you're ready to test, you'll need to properly create a signed APK. You can follow this  [guide](https://facebook.github.io/react-native/docs/signed-apk-android.html). (**Important**: You'll have to install the APK as described in the guide. Not in the way you'd normally debug an React Native app on Android).
* When you have the APK, you need to upload it to Play Developer Console, either the Alpha or the Beta channel will be fine. Remember your app will need to have a proper `applicationId` (normally your package name) and `versionCode` set in `android/app/build.gradle`.
* After uploading, you will have to publish it to the market. Don't worry, when publishing the APK to the Alpha or Beta channel the APK will not be available for general public. (**Important**: It might take several hours for Google to process the APK).
* The final part is, you'll need to add testers for the channel you've published to. The web page will give you a signup URL (opt-in) after you've approved open testing. Visit this URL in the browser of your **testing device** (it must be a physical device, not a emulator) and signup, and download the app where it redirected.
* Try to buy something with the device. The purchase will eventually be cancelled, but you can also do this manually through your Google Merchant wallet.

**Important**: You can only test on a physical Android device, not from an emulator.


## Javascript API
All  methods returns a `Promise`.

### open()

**Important:** Opens the service channel to Google Play. Must be called (once!) before any other billing methods can be called.

```javascript
InAppBilling.open()
.then(() => InAppBilling.purchase('android.test.purchased'));
```

### close()
**Important:** Must be called to close the service channel to Google Play, when you are done doing billing related work. Failure to close the service channel may degrade the performance of your app.
```javascript
InAppBilling.open()
.then(() => InAppBilling.purchase('android.test.purchased'))
.then((details) => {
  console.log("You purchased: ", details)
  return InAppBilling.close()
});
```

### purchase(productId)
##### Parameter(s)
* **productId (required):** String
* **developerPayload:** String

##### Returns:
* **transactionDetails:** Object:
  * **productId:** String
  * **orderId:** String
  * **purchaseToken:** String
  * **purchaseTime:** String
  * **purchaseState:** String ("PurchasedSuccessfully", "Canceled", "Refunded", "SubscriptionExpired")
  * **receiptSignature:** String
  * **receiptData:** String
  * **developerPayload:** String

```javascript
InAppBilling.purchase('android.test.purchased')
.then((details) => {
  console.log(details)
});
```

### consumePurchase(productId)
##### Parameter(s)
* **productId (required):** String

##### Returns:
* **consumed:** Boolean (If consumed or not)

```javascript
InAppBilling.consumePurchase('your.inapp.productid').then(...);
```

### subscribe(productId)
##### Parameter(s)
* **productId (required):** String
* **developerPayload:** String

##### Returns:
* **transactionDetails:** Object:
  * **productId:** String
  * **orderId:** String
  * **purchaseToken:** String
  * **purchaseTime:** String
  * **purchaseState:** String ("PurchasedSuccessfully", "Canceled", "Refunded", "SubscriptionExpired")
  * **receiptSignature:** String
  * **receiptData:** String
  * **developerPayload:** String

```javascript
InAppBilling.subscribe('your.inapp.productid')
.then((details) => {
  console.log(details)
});
```

### isSubscribed(productId)
##### Parameter(s)
* **productId (required):** String

##### Returns:
* **subscribed:** Boolean

```javascript
InAppBilling.isSubscribed('your.inapp.productid').then(...);
```

### updateSubscription(oldProductIds, productId)
##### Parameter(s)
* **oldProductIds (required)**: Array of String
* **productId (required)**: String

##### Returns:
* **transactionDetails:** Object:
  * **productId:** String
  * **orderId:** String
  * **purchaseToken:** String
  * **purchaseTime:** String
  * **purchaseState:** String ("PurchasedSuccessfully", "Canceled", "Refunded", "SubscriptionExpired")
  * **receiptSignature:** String
  * **receiptData:** String
  * **developerPayload:** String

```javascript
InAppBilling.updateSubscription(['subscription.p1m', 'subscription.p3m'], 'subscription.p12m').then(...)
```

### isPurchased(productId)
##### Parameter(s)
* **productId (required):** String

##### Returns:
* **purchased:** Boolean

```javascript
InAppBilling.isPurchased('your.inapp.productid').then(...);
```

### listOwnedProducts()
##### Returns:
* **ownedProductIds:** Array of String

```javascript
InAppBilling.listOwnedProducts().then(...);
```

### listOwnedSubscriptions()
##### Returns:
* **ownedSubscriptionIds:** Array of String

```javascript
InAppBilling.listOwnedSubscriptions().then(...);
```

### getProductDetails(productId)
##### Parameter(s)
* **productId (required):** String

##### Returns:
* **productDetails:** Object:
  * **productId:** String
  * **title:** String
  * **description:** String
  * **isSubscription:** Boolean
  * **currency:** String
  * **priceValue:**  Double
  * **priceText:** String

```javascript
InAppBilling.getProductDetails('your.inapp.productid').then(...);
```

### getProductDetailsArray(productIds)
##### Parameter(s)
* **productIds (required):** String-array

##### Returns:
* **productDetailsArray:** Array of the productDetails (same as above)

```javascript
InAppBilling.getProductDetailsArray(['your.inapp.productid', 'your.inapp.productid2']).then(...);
```

### getSubscriptionDetails(productId)
##### Parameter(s)
* **productId (required):** String

##### Returns:
* **productDetails:** Object:
  * **productId:** String
  * **title:** String
  * **description:** String
  * **isSubscription:** Boolean
  * **currency:** String
  * **priceValue:**  Double
  * **priceText:** String

```javascript
InAppBilling.getSubscriptionDetails('your.inapp.productid').then(...);
```

### getSubscriptionDetailsArray(productIds)
##### Parameter(s)
* **productIds (required):** String-Array

##### Returns:
* **productDetailsArray:** Array of the productDetails (same as above)

```javascript
InAppBilling.getSubscriptionDetailsArray(['your.inapp.productid', 'your.inapp.productid2']).then(...);
```

### getPurchaseTransactionDetails(productId)
##### Parameter(s)
* **productId (required):** String

##### Returns:
* **transactionDetails:** Object:
  * **productId:** String
  * **orderId:** String
  * **purchaseToken:** String
  * **purchaseTime:** String
  * **purchaseState:** String ("PurchasedSuccessfully", "Canceled", "Refunded", "SubscriptionExpired")
  * **receiptSignature:** String
  * **receiptData:** String
  * **developerPayload:** String

```javascript
InAppBilling.getPurchaseTransactionDetails('your.inapp.productid')
.then((details) => {
  console.log(details)
});
```

### getSubscriptionTransactionDetails(productId)
##### Parameter(s)
* **productId (required):** String

##### Returns:
* **transactionDetails:** Object:
  * **productId:** String
  * **orderId:** String
  * **purchaseToken:** String
  * **purchaseTime:** String
  * **purchaseState:** String ("PurchasedSuccessfully", "Canceled", "Refunded", "SubscriptionExpired")
  * **receiptSignature:** String
  * **receiptData:** String
  * **developerPayload:** String

```javascript
InAppBilling.getSubscriptionTransactionDetails('your.inapp.productid')
.then((details) => {
  console.log(details)
});
```

### loadOwnedPurchasesFromGoogle()
Refreshes the internal purchases & subscriptions status cache.
```javascript
InAppBilling.loadOwnedPurchasesFromGoogle().then(...);
```
