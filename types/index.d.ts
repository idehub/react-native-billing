// TypeScript Version: 2.4

export enum PurchaseState {
  PurchasedSuccessfully = "PurchasedSuccessfully",
  Canceled = "Canceled",
  Refunded = "Refunded",
  SubscriptionExpired = "SubscriptionExpired"
}

export interface ITransactionDetails {
  productId: string;
  orderId: string;
  purchaseToken: string;
  purchaseTime: string;
  purchaseState: PurchaseState;
  receiptSignature: string;
  receiptData: string;
  autoRenewing: boolean;
  developerPayload: string;
}

export interface IProductDetails {
  productId: string;
  title: string;
  description: string;
  isSubscription: string;
  currency: string;
  priceValue: string;
  priceText: string;
}

export interface ISubscriptionDetails extends IProductDetails {
  subscriptionPeriod: string;
  subscriptionFreeTrialPeriod?: string;
  haveTrialPeriod: boolean;
  introductoryPriceValue: number;
  introductoryPriceText?: string;
  introductoryPricePeriod?: string;
  haveIntroductoryPeriod: boolean;
  introductoryPriceCycles: number;
}

export default class InAppBilling {
  open(): Promise<void>;

  close(): Promise<void>;

  purchase(
    productId: string,
    developerPayload?: string
  ): Promise<ITransactionDetails>;

  consumePurchase(
    productId: string,
    developerPayload?: string
  ): Promise<ITransactionDetails>;

  isSubscribed(productId: string): Promise<boolean>;

  updateSubscription(
    oldProductIds: string[],
    productId: string,
    developerPayload?: string
  ): Promise<ITransactionDetails>;

  isPurchased(productId: string): Promise<boolean>;

  isOneTimePurchaseSupported(): Promise<boolean>;

  isValidTransactionDetails(productId: string): Promise<boolean>;

  listOwnedProducts(): Promise<string[]>;

  listOwnedSubscriptions(): Promise<string[]>;

  getProductDetails(productId: string): Promise<IProductDetails>;

  getProductDetailsArray(productIds: string[]): Promise<IProductDetails[]>;

  getSubscriptionDetails(productId: string): Promise<ISubscriptionDetails>;

  getSubscriptionDetailsArray(
    productIds: string[]
  ): Promise<ISubscriptionDetails[]>;

  getPurchaseTransactionDetails(
    productId: string
  ): Promise<ITransactionDetails>;

  getSubscriptionTransactionDetails(
    productId: string
  ): Promise<ITransactionDetails>;

  loadOwnedPurchasesFromGoogle(): Promise<any>;
}
