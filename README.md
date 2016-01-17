InApp Billing for Android ![npm version](https://img.shields.io/npm/v/react-native-billing.svg)
=============
This is a simple bridge for InApp Billing (Purchase) on Android for React Native.

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
```
<string name="RNB_GOOGLE_PLAY_LICENSE_KEY">YOUR_GOOGLE_PLAY_LICENSE_KEY_HERE</string>
```
