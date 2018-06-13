import InAppBilling, {
  ITransactionDetails,
  IProductDetails,
  ISubscriptionDetails
} from "react-native-billing";

// $ExpectType Promise<void>
InAppBilling.open();

// $ExpectType Promise<ITransactionDetails>
InAppBilling.purchase("productId");

InAppBilling.purchase("productId").then(
  (transactionDetails: ITransactionDetails) => {
    const productId = transactionDetails.productId;
  }
);

// $ExpectType Promise<ITransactionDetails>
InAppBilling.subscribe("productId");

// $ExpectType Promise<void>
InAppBilling.close();

// $ExpectType Promise<IProductDetails>
InAppBilling.getProductDetails("productId");

InAppBilling.getProductDetails("productId").then((details: IProductDetails) => {
  const price = details.priceValue;
});

// $ExpectType Promise<ISubscriptionDetails>
InAppBilling.getSubscriptionDetails("productId");

InAppBilling.getSubscriptionDetails("productId").then(
  (details: ISubscriptionDetails) => {
    const price = details.priceValue;
  }
);
