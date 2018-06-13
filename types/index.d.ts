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
  isSubscription: boolean;
  currency: string;
  priceValue: number;
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
  static open(): Promise<void>;

  static close(): Promise<void>;

  static loadOwnedPurchasesFromGoogle(): Promise<any>;

  static purchase(
    productId: string,
    developerPayload?: string
  ): Promise<ITransactionDetails>;

  static consumePurchase(
    productId: string,
    developerPayload?: string
  ): Promise<ITransactionDetails>;

  static subscribe(
    productId: string,
    developerPayload?: string
  ): Promise<ITransactionDetails>;

  static updateSubscription(
    oldProductIds: string[],
    productId: string,
    developerPayload?: string
  ): Promise<ITransactionDetails>;

  static isSubscribed(productId: string): Promise<boolean>;

  static isPurchased(productId: string): Promise<boolean>;

  static isOneTimePurchaseSupported(): Promise<boolean>;

  static isValidTransactionDetails(productId: string): Promise<boolean>;

  static listOwnedProducts(): Promise<string[]>;

  static listOwnedSubscriptions(): Promise<string[]>;

  static getProductDetails(productId: string): Promise<IProductDetails>;

  static getPurchaseTransactionDetails(
    productId: string
  ): Promise<ITransactionDetails>;

  static getSubscriptionTransactionDetails(
    productId: string
  ): Promise<ITransactionDetails>;

  static getProductDetailsArray(
    productIds: string[]
  ): Promise<IProductDetails[]>;

  static getSubscriptionDetails(
    productId: string
  ): Promise<ISubscriptionDetails>;

  static getSubscriptionDetailsArray(
    productIds: string[]
  ): Promise<ISubscriptionDetails[]>;
}
