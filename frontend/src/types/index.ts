export type Role = 'ROLE_USER' | 'ROLE_ADMIN';

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  preferredCurrency: string;
  createdAt?: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  id: number;
  email: string;
  fullName: string;
  role: string;
  preferredCurrency: string;
}

export type StrategyType = 'LOWEST_PRICE' | 'BEST_WARRANTY' | 'BEST_VALUE' | 'BALANCED';

export interface OfferScoreResult {
  offerId: number;
  offerTitle: string;
  sellerName: string;
  score: number;
  rank: number;
  rationale: string;
  isWinner: boolean;
}

export interface DeterministicOfferSummary {
  offerId: number;
  sellerName: string;
  title: string;
  productModel?: string;
  currency: string;
  basePrice: number;
  deliveryFee: number;
  additionalFees: number;
  discountAmount: number;
  totalPrice: number;
  warrantyMonths: number;
  returnWindowDays: number;
  monthlyCostPerWarrantyMonth: number;
  priceDifferenceFromLowest: number;
  percentageDifferenceFromLowest: number;
}

export interface DeterministicComparisonSummary {
  offerCount: number;
  currency: string;
  cheapestOfferId: number;
  cheapestOfferSeller: string;
  lowestTotalPrice: number;
  highestPriceOfferId: number;
  highestPriceOfferSeller: string;
  highestTotalPrice: number;
  absolutePriceDifference: number;
  percentagePriceDifference: number;
  bestWarrantyOfferId: number;
  longestWarrantyMonths: number;
  shortestWarrantyMonths: number;
  warrantyDifferenceMonths: number;
  offerSummaries: DeterministicOfferSummary[];
  rankingsByStrategy: Record<StrategyType, OfferScoreResult[]>;
  specificationComparisonMatrix: Record<string, Record<string, string>>;
}

export interface Offer {
  id: number;
  comparisonId: number;
  sellerName: string;
  sellerId?: number;
  title: string;
  productModel?: string;
  sourceUrl?: string;
  currency: string;
  basePrice: number;
  deliveryFee: number;
  additionalFees: number;
  discountAmount: number;
  totalPrice: number;
  warrantyMonths: number;
  returnWindowDays: number;
  specifications?: Record<string, any>;
  specificationsJson?: string;
  warrantyConditions?: string;
  returnPolicy?: string;
  isRecommended?: boolean;
  valueScore?: number;
  createdAt: string;
  updatedAt: string;
}

export interface OfferTradeoffAnalysis {
  offerId: number;
  sellerName: string;
  strengths: string[];
  weaknesses: string[];
  warrantyAssessment: string;
  returnPolicyAssessment: string;
  sellerCredibilityNotes: string[];
  itemsToVerifyBeforeBuying: string[];
}

export interface OfferAnalysisAiResult {
  executiveSummary: string;
  recommendedOfferId?: number;
  recommendedOfferSeller?: string;
  recommendationRationale?: string;
  keySpecificationDifferences: string[];
  offerEvaluations: OfferTradeoffAnalysis[];
  missingInformationAlerts: string[];
  purchasingRisks: string[];
  confidenceScore: number;
}

export interface Comparison {
  id: number;
  userId: number;
  title: string;
  categoryName: string;
  status: 'DRAFT' | 'ANALYZED' | 'SAVED' | 'ARCHIVED';
  notes?: string;
  aiAnalysisJson?: string;
  aiAnalyzedAt?: string;
  offers: Offer[];
  deterministicSummary?: DeterministicComparisonSummary;
  createdAt: string;
  updatedAt: string;
}

export interface Purchase {
  id: number;
  userId: number;
  productName: string;
  modelNumber?: string;
  category?: string;
  sellerName: string;
  purchaseDate: string;
  pricePaid: number;
  currency: string;
  warrantyMonths: number;
  warrantyExpirationDate: string;
  returnDeadline?: string;
  serialNumber?: string;
  status: 'ACTIVE' | 'EXPIRED_WARRANTY' | 'RETURNED' | 'REPLACED';
  notes?: string;
  daysUntilWarrantyExpiry: number;
  isWarrantyActive: boolean;
  isReturnWindowActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PurchaseStats {
  totalOwnedProducts: number;
  totalExpenditure: number;
  currency: string;
  activeWarrantiesCount: number;
  expiringSoonWarrantiesCount: number;
}

export type DocumentType = 'RECEIPT' | 'INVOICE' | 'WARRANTY' | 'RETURN_POLICY' | 'SPEC_SHEET';
export type VerificationStatus = 'PENDING_VERIFICATION' | 'VERIFIED' | 'REJECTED' | 'MODIFIED';

export interface ExtractedFact {
  id: number;
  documentId: number;
  factKey: string;
  extractedValue: string;
  verifiedValue?: string;
  sourceExcerpt?: string;
  pageNumber: number;
  confidenceScore: number;
  verificationStatus: VerificationStatus;
  createdAt: string;
}

export interface DocumentItem {
  id: number;
  userId: number;
  purchaseId?: number;
  comparisonId?: number;
  fileName: string;
  contentType: string;
  fileSizeBytes: number;
  documentType: DocumentType;
  extractionStatus: string;
  rawTextContent?: string;
  createdAt: string;
  extractedFacts: ExtractedFact[];
}

export type DeadlineType = 'WARRANTY_EXPIRY' | 'RETURN_WINDOW' | 'DOCUMENT_REVIEW' | 'CUSTOM';

export interface Deadline {
  id: number;
  userId: number;
  purchaseId?: number;
  title: string;
  deadlineType: DeadlineType;
  dueDate: string;
  urgency: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | 'EXPIRED';
  isCompleted: boolean;
  notes?: string;
  daysRemaining: number;
  isExpired: boolean;
  createdAt: string;
}

export interface Reminder {
  id: number;
  deadlineId: number;
  userId: number;
  triggerDate: string;
  message: string;
  status: 'PENDING' | 'TRIGGERED' | 'DISMISSED';
  triggeredAt?: string;
  createdAt: string;
}

export interface CitedReference {
  resourceType: 'PURCHASE' | 'COMPARISON' | 'DOCUMENT' | 'DEADLINE';
  resourceId: number;
  title: string;
  snippet: string;
}

export interface AssistantChatResponse {
  conversationId: number;
  response: string;
  toolCallsExecuted: string[];
  citedReferences: CitedReference[];
  timestamp: string;
}

export interface ProductCategory {
  id: number;
  name: string;
  slug: string;
  description: string;
  iconName: string;
}

export interface Seller {
  id: number;
  name: string;
  websiteUrl?: string;
  reputationScore: number;
  returnPolicySummary?: string;
  isVerified: boolean;
}

export interface DashboardOverview {
  userFullName: string;
  preferredCurrency: string;
  totalPurchasesCount: number;
  totalExpenditureAmount: number;
  activeComparisonsCount: number;
  activeWarrantiesCount: number;
  expiringSoonWarrantiesCount: number;
  upcomingDeadlines: Deadline[];
  activeReminders: Reminder[];
  recentComparisons: Comparison[];
  recentPurchases: Purchase[];
}
