InApp Billing for Android ![npm version](https://img.shields.io/npm/v/react-native-billing.svg)
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

## Installation with rnpm
1. `npm install --save react-native-billing`
2. `rnpm link react-native-google-billing`

With this, [rnpm](https://github.com/rnpm/rnpm) will do most of the heavy lifting for linking, **but** you will still need add your Google Play license key to the `strings.xml` (step 5).

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

4. Register package in `MainActivity.java`

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
                  // Step 2; register package, with your and send in the MainActivity as a parameter (this):
                  .addPackage(new InAppBillingBridgePackage(this))
                  .setUseDeveloperSupport(BuildConfig.DEBUG)
                  .setInitialLifecycleState(LifecycleState.RESUMED)
                  .build();

          ...
      }
      ...
  ```
5. Add your Google Play license key as a line to your `android/app/src/main/res/values/strings.xml` with the name `RNB_GOOGLE_PLAY_LICENSE_KEY`. For example:
```xml
<string name="RNB_GOOGLE_PLAY_LICENSE_KEY">YOUR_GOOGLE_PLAY_LICENSE_KEY_HERE</string>
```
Alternatively, you can add your license key as a parameter when registering the `InAppBillingBridgePackage`, like so:
```java
.addPackage(new InAppBillingBridgePackage("YOUR_LICENSE_KEY", this))
```

## Javascript API
All  methods returns a `Promise`.

### open()

**Important:** Must be called to open the service channel to Google Play. Must be called (once!) before any other billing methods can be called.

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

##### Returns:
* **transactionDetails:** Object:
  * **productId:** String
  * **orderId:** String
  * **purchaseToken:** String
  * **purchaseTime:** String
  * **purchaseState:** String
  * **receiptSignature:** String
  * **receiptData:** String

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
InAppBilling.consumePurchase('android.test.purchased').then(...);
```

### subscribe(productId)
##### Parameter(s)
* **productId (required):** String

##### Returns:
* **transactionDetails:** Object:
  * **productId:** String
  * **orderId:** String
  * **purchaseToken:** String
  * **purchaseTime:** String
  * **purchaseState:** String
  * **receiptSignature:** String
  * **receiptData:** String

```javascript
InAppBilling.subscribe('android.test.purchased')
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
InAppBilling.isSubscribed('android.test.purchased').then(...);
```

### isPurchased(productId)
##### Parameter(s)
* **productId (required):** String

##### Returns:
* **purchased:** Boolean

```javascript
InAppBilling.isPurchased('android.test.purchased').then(...);
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
InAppBilling.getProductDetails('android.test.purchased').then(...);
```

### getProductDetailsArray(productIds)
##### Parameter(s)
* **productIds (required):** String-array

##### Returns:
* **productDetailsArray:** Array of the productDetails (same as above)

```javascript
InAppBilling.getProductDetailsArray(['android.test.purchased', 'android.test.purchased2']).then(...);
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
InAppBilling.getSubscriptionDetails('android.test.subscription').then(...);
```

### getSubscriptionDetailsArray(productIds)
##### Parameter(s)
* **productIds (required):** String-Array

##### Returns:
* **productDetailsArray:** Array of the productDetails (same as above)

```javascript
InAppBilling.getSubscriptionDetailsArray(['android.test.subscription', 'android.test.subscription2']).then(...);
```
