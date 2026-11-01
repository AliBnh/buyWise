package com.buywise.modules.ai.tools;

import com.buywise.modules.ai.dto.CitedReferenceDto;
import com.buywise.modules.comparison.model.Comparison;
import com.buywise.modules.comparison.model.Offer;
import com.buywise.modules.comparison.repository.ComparisonRepository;
import com.buywise.modules.deadline.model.Deadline;
import com.buywise.modules.deadline.repository.DeadlineRepository;
import com.buywise.modules.document.model.Document;
import com.buywise.modules.document.model.ExtractedFact;
import com.buywise.modules.document.repository.DocumentRepository;
import com.buywise.modules.purchase.model.Purchase;
import com.buywise.modules.purchase.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlled Tool and Context Registry for BuyWise AI Assistant.
 * 
 * Demonstrates:
 * - Controlled Tool Invocation: AI Assistant accesses data strictly through typed, authorized Java methods.
 * - Multi-Tenant Isolation: User ID is enforced on every query so the LLM cannot see another user's data.
 * - Traceability: Collects cited reference metadata for user visibility.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssistantToolRegistry {

    private final PurchaseRepository purchaseRepository;
    private final ComparisonRepository comparisonRepository;
    private final DeadlineRepository deadlineRepository;
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public UserContextBundle gatherUserContext(Long userId, String userQuery) {
        List<CitedReferenceDto> citations = new ArrayList<>();
        StringBuilder contextBuilder = new StringBuilder();

        // 1. Fetch User Purchases
        List<Purchase> purchases = purchaseRepository.findByUserIdOrderByPurchaseDateDesc(userId);
        contextBuilder.append("=== USER'S REGISTERED PURCHASES & OWNED PRODUCTS ===\n");
        if (purchases.isEmpty()) {
            contextBuilder.append("None registered.\n");
        } else {
            for (Purchase p : purchases) {
                boolean active = p.getWarrantyExpirationDate() != null && !p.getWarrantyExpirationDate().isBefore(LocalDate.now());
                contextBuilder.append(String.format("- [Purchase ID %d] %s (%s) | Seller: %s | Price: %s %s | Purchased: %s | Warranty: %d months (Expires: %s, Active: %s) | Return Deadline: %s\n",
                        p.getId(), p.getProductName(), p.getModelNumber() != null ? p.getModelNumber() : "No model",
                        p.getSellerName(), p.getPricePaid(), p.getCurrency(), p.getPurchaseDate(),
                        p.getWarrantyMonths(), p.getWarrantyExpirationDate(), active,
                        p.getReturnDeadline() != null ? p.getReturnDeadline() : "None"));

                citations.add(CitedReferenceDto.builder()
                        .resourceType("PURCHASE")
                        .resourceId(p.getId())
                        .title(p.getProductName())
                        .snippet(String.format("Warranty expires %s", p.getWarrantyExpirationDate()))
                        .build());
            }
        }
        contextBuilder.append("\n");

        // 2. Fetch User Comparisons
        List<Comparison> comparisons = comparisonRepository.findByUserIdOrderByCreatedAtDesc(userId);
        contextBuilder.append("=== USER'S ACTIVE COMPARISONS ===\n");
        if (comparisons.isEmpty()) {
            contextBuilder.append("None created.\n");
        } else {
            for (Comparison c : comparisons) {
                contextBuilder.append(String.format("- [Comparison ID %d] %s (Category: %s, Status: %s)\n",
                        c.getId(), c.getTitle(), c.getCategoryName(), c.getStatus()));
                if (c.getOffers() != null) {
                    for (Offer o : c.getOffers()) {
                        contextBuilder.append(String.format("    * Offer: %s | Seller: %s | Base: %s %s | Total: %s %s | Warranty: %d mo\n",
                                o.getTitle(), o.getSellerName(), o.getBasePrice(), o.getCurrency(), o.getTotalPrice(), o.getCurrency(), o.getWarrantyMonths()));
                    }
                }

                citations.add(CitedReferenceDto.builder()
                        .resourceType("COMPARISON")
                        .resourceId(c.getId())
                        .title(c.getTitle())
                        .snippet(String.format("%d offers compared", c.getOffers() != null ? c.getOffers().size() : 0))
                        .build());
            }
        }
        contextBuilder.append("\n");

        // 3. Fetch Deadlines & Alerts
        List<Deadline> deadlines = deadlineRepository.findByUserIdAndIsCompletedFalseOrderByDueDateAsc(userId);
        contextBuilder.append("=== UPCOMING & PENDING DEADLINES ===\n");
        if (deadlines.isEmpty()) {
            contextBuilder.append("No active pending deadlines.\n");
        } else {
            for (Deadline d : deadlines) {
                contextBuilder.append(String.format("- [Deadline ID %d] %s | Type: %s | Due: %s | Urgency: %s\n",
                        d.getId(), d.getTitle(), d.getDeadlineType(), d.getDueDate(), d.getUrgency()));

                citations.add(CitedReferenceDto.builder()
                        .resourceType("DEADLINE")
                        .resourceId(d.getId())
                        .title(d.getTitle())
                        .snippet("Due: " + d.getDueDate())
                        .build());
            }
        }
        contextBuilder.append("\n");

        // 4. Fetch Documents & Verified Facts
        List<Document> documents = documentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        contextBuilder.append("=== STORED PURCHASE DOCUMENTS & EXTRACTED FACTS ===\n");
        if (documents.isEmpty()) {
            contextBuilder.append("No documents uploaded.\n");
        } else {
            for (Document doc : documents) {
                contextBuilder.append(String.format("- [Doc ID %d] %s (%s)\n", doc.getId(), doc.getFileName(), doc.getDocumentType()));
                if (doc.getExtractedFacts() != null) {
                    for (ExtractedFact fact : doc.getExtractedFacts()) {
                        contextBuilder.append(String.format("    * %s: %s (Status: %s, Excerpt: \"%s\")\n",
                                fact.getFactKey(),
                                fact.getVerifiedValue() != null ? fact.getVerifiedValue() : fact.getExtractedValue(),
                                fact.getVerificationStatus(),
                                fact.getSourceExcerpt() != null ? fact.getSourceExcerpt() : ""));
                    }
                }

                citations.add(CitedReferenceDto.builder()
                        .resourceType("DOCUMENT")
                        .resourceId(doc.getId())
                        .title(doc.getFileName())
                        .snippet(doc.getDocumentType().name())
                        .build());
            }
        }

        return new UserContextBundle(contextBuilder.toString(), citations);
    }

    public record UserContextBundle(String formattedContext, List<CitedReferenceDto> citations) {}
}
