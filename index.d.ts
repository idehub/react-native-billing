namespace InAppBilling {
  enum PurchaseState {
    PurchasedSuccessfully = "PurchasedSuccessfully",
    Canceled = "Canceled",
    Refunded = "Refunded",
    SubscriptionExpired = "SubscriptionExpired",
  }

  interface ITransactionDetails {
    productId: string;
    orderId: string;
    purchaseToken: string;
    purchaseTime: string;
    purchaseState: PurchaseState;
    receiptSignature: string;
    receiptData: string;
    developerPayload: string;
  }

  interface IProductDetails {
    productId: string;
    title: string;
    description: string;
    isSubscription: string;
    currency: string;
    priceValue: string;
    priceText: string;
  }

  interface ISubscriptionDetails extends IProductDetails {
    subscriptionPeriod: string;
    subscriptionFreeTrialPeriod?: string;
    haveTrialPeriod: boolean;
    introductoryPriceValue: number;
    introductoryPriceText?: string;
    introductoryPricePeriod?: string;
    haveIntroductoryPeriod: boolean;
    introductoryPriceCycles: number;
  }

  function open(): Promise<void>;

  function close(): Promise<void>;

  function purchase(productId: string, developerPayload?: string): Promise<ITransactionDetails>;

  function consumePurchase(productId: string, developerPayload?: string): Promise<ITransactionDetails>;

  function isSubscribed(productId: string): Promise<boolean>;

  function updateSubscription(
    oldProductIds: string[],
    productId: string,
    developerPayload?: string,
  ): Promise<ITransactionDetails>;

  function isPurchased(productId: string): Promise<boolean>;

  function isOneTimePurchaseSupported(): Promise<boolean>;

  function isValidTransactionDetails(productId: string): Promise<boolean>;

  function listOwnedProducts(): Promise<string[]>;

  function listOwnedSubscriptions(): Promise<string[]>;

  function getProductDetails(productId: string): Promise<IProductDetails>;

  function getProductDetailsArray(productIds: string[]): Promise<IProductDetails[]>;

  function getSubscriptionDetails(productId: string): Promise<ISubscriptionDetails>;

  function getSubscriptionDetailsArray(productIds: string[]): Promise<ISubscriptionDetails[]>;

  function getPurchaseTransactionDetails(productId: string): Promise<ITransactionDetails>;

  function getSubscriptionTransactionDetails(productId: string): Promise<ITransactionDetails>;

  function loadOwnedPurchasesFromGoogle(): Promise<any>;
}

export = InAppBilling;
