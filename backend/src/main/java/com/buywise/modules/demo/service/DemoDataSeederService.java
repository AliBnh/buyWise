package com.buywise.modules.demo.service;

import com.buywise.modules.auth.model.Role;
import com.buywise.modules.auth.model.User;
import com.buywise.modules.auth.repository.UserRepository;
import com.buywise.modules.comparison.model.Comparison;
import com.buywise.modules.comparison.model.ComparisonStatus;
import com.buywise.modules.comparison.model.Offer;
import com.buywise.modules.comparison.repository.ComparisonRepository;
import com.buywise.modules.comparison.repository.OfferRepository;
import com.buywise.modules.deadline.model.Deadline;
import com.buywise.modules.deadline.model.DeadlineType;
import com.buywise.modules.deadline.model.Reminder;
import com.buywise.modules.deadline.model.ReminderStatus;
import com.buywise.modules.deadline.repository.DeadlineRepository;
import com.buywise.modules.deadline.repository.ReminderRepository;
import com.buywise.modules.document.model.Document;
import com.buywise.modules.document.model.DocumentType;
import com.buywise.modules.document.model.ExtractedFact;
import com.buywise.modules.document.model.VerificationStatus;
import com.buywise.modules.document.repository.DocumentRepository;
import com.buywise.modules.document.repository.ExtractedFactRepository;
import com.buywise.modules.product.model.Seller;
import com.buywise.modules.product.repository.SellerRepository;
import com.buywise.modules.purchase.model.Purchase;
import com.buywise.modules.purchase.model.PurchaseStatus;
import com.buywise.modules.purchase.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Service seeding realistic, internally consistent demo data and providing an idempotent reset mechanism.
 * 
 * Demonstrates:
 * - Deterministic Demo State: Seeds rich purchase, comparison, document, and deadline records without external APIs.
 * - Non-Destructive Reset: Only resets data for the demo user account (demo@buywise.com), protecting actual user data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoDataSeederService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final ComparisonRepository comparisonRepository;
    private final OfferRepository offerRepository;
    private final PurchaseRepository purchaseRepository;
    private final DocumentRepository documentRepository;
    private final ExtractedFactRepository factRepository;
    private final DeadlineRepository deadlineRepository;
    private final ReminderRepository reminderRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${buywise.demo.seed-on-startup:true}")
    private boolean seedOnStartup;

    @Override
    public void run(String... args) {
        if (seedOnStartup) {
            log.info("Checking and seeding baseline demo accounts and records...");
            seedDemoUserAndData();
        }
    }

    @Transactional
    public void resetDemoData() {
        log.info("Resetting demo user data to pristine baseline state...");
        User demoUser = userRepository.findByEmail("demo@buywise.com").orElse(null);
        if (demoUser != null) {
            // Delete in strict child-to-parent order to respect relational integrity and prevent stale state
            reminderRepository.deleteAll(reminderRepository.findByUserIdAndStatusOrderByTriggerDateDesc(demoUser.getId(), ReminderStatus.TRIGGERED));
            reminderRepository.deleteAll(reminderRepository.findByUserIdAndStatusOrderByTriggerDateDesc(demoUser.getId(), ReminderStatus.PENDING));
            reminderRepository.flush();

            deadlineRepository.deleteAll(deadlineRepository.findByUserIdOrderByDueDateAsc(demoUser.getId()));
            deadlineRepository.flush();

            documentRepository.deleteAll(documentRepository.findByUserIdOrderByCreatedAtDesc(demoUser.getId()));
            documentRepository.flush();

            comparisonRepository.deleteAll(comparisonRepository.findByUserIdOrderByCreatedAtDesc(demoUser.getId()));
            comparisonRepository.flush();

            purchaseRepository.deleteAll(purchaseRepository.findByUserIdOrderByPurchaseDateDesc(demoUser.getId()));
            purchaseRepository.flush();
        }

        seedDemoUserAndData();
        log.info("Demo user data reset complete.");
    }

    @Transactional
    public void seedDemoUserAndData() {
        // 1. Create or ensure Demo User
        User demoUser = userRepository.findByEmail("demo@buywise.com")
                .orElseGet(() -> User.builder()
                        .email("demo@buywise.com")
                        .fullName("Karim Bennani")
                        .passwordHash(passwordEncoder.encode("Demo123!"))
                        .role(Role.ROLE_USER)
                        .preferredCurrency("MAD")
                        .build());
        demoUser.setPasswordHash(passwordEncoder.encode("Demo123!"));
        demoUser = userRepository.save(demoUser);

        // 2. Create or ensure Admin User
        userRepository.findByEmail("admin@buywise.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("admin@buywise.com")
                        .fullName("BuyWise Administrator")
                        .passwordHash(passwordEncoder.encode("Admin123!"))
                        .role(Role.ROLE_ADMIN)
                        .preferredCurrency("MAD")
                        .build()));

        // Ensure baseline demo records exist for demoUser
        if (comparisonRepository.countByUserId(demoUser.getId()) == 0) {
            seedLaptopComparison(demoUser);
        }

        if (purchaseRepository.countByUserId(demoUser.getId()) == 0) {
            seedPurchasesAndDeadlines(demoUser);
        }
    }

    private void seedLaptopComparison(User user) {
        Comparison laptopComp = Comparison.builder()
                .userId(user.getId())
                .title("Lenovo IdeaPad Slim 5 — Work & Coding Laptop")
                .categoryName("Laptops & Computers")
                .status(ComparisonStatus.ANALYZED)
                .notes("Comparing two retailers in Casablanca for the AMD Ryzen 7 7730U model. Evaluating whether extra 512GB SSD and +12 months warranty justify 650 MAD price difference.")
                .aiAnalysisJson("{\"executiveSummary\":\"Comparing two Lenovo IdeaPad Slim 5 configurations. Offer B (ElectroMaroc) offers double the SSD storage (1TB vs 512GB) and an additional 12 months warranty for a 650 MAD premium.\",\"recommendedOfferId\":2,\"recommendedOfferSeller\":\"ElectroMaroc\",\"recommendationRationale\":\"Offer B provides superior long-term ownership value: double storage and 24-month local warranty outweighs the 650 MAD difference for software development use.\",\"keySpecificationDifferences\":[\"Storage: 512GB vs 1TB SSD (+100% capacity)\",\"Warranty: 12 months vs 24 months (+100% coverage)\",\"Net Price: 8,549 MAD vs 9,199 MAD (+650 MAD difference)\"],\"missingInformationAlerts\":[\"Verify keyboard layout (AZERTY vs QWERTY) with TechHub Casablanca prior to pickup.\"],\"purchasingRisks\":[\"TechHub's 7-day return window is brief for testing hardware defects under load.\"],\"confidenceScore\":0.96}")
                .aiAnalyzedAt(Instant.now().minusSeconds(3600))
                .build();

        Offer offerA = Offer.builder()
                .sellerName("TechHub Casablanca")
                .title("Lenovo IdeaPad Slim 5 (16GB RAM / 512GB SSD / Ryzen 7)")
                .productModel("IdeaPad Slim 5 16ABR8")
                .sourceUrl("https://techhub.ma/products/lenovo-ideapad-slim-5-ryzen7")
                .currency("MAD")
                .basePrice(new BigDecimal("8499.00"))
                .deliveryFee(new BigDecimal("50.00"))
                .additionalFees(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .warrantyMonths(12)
                .returnWindowDays(7)
                .specificationsJson("{\"processor\":\"AMD Ryzen 7 7730U (8 cores, up to 4.5 GHz)\",\"ram\":\"16 GB DDR4-3200MHz\",\"storage\":\"512 GB M.2 NVMe SSD\",\"display\":\"16\\\" WUXGA (1920x1200) IPS 300nits Anti-glare\",\"battery\":\"56.6Wh Fast Charge\",\"weight\":\"1.89 kg\"}")
                .warrantyConditions("12-month standard importer warranty. Repairs handled through Casablanca service point.")
                .returnPolicy("7-day return period strictly for unopened original packaging.")
                .isRecommended(false)
                .valueScore(new BigDecimal("82.50"))
                .build();
        offerA.recalculateTotalPrice();
        laptopComp.addOffer(offerA);

        Offer offerB = Offer.builder()
                .sellerName("ElectroMaroc")
                .title("Lenovo IdeaPad Slim 5 (16GB RAM / 1TB SSD / Ryzen 7)")
                .productModel("IdeaPad Slim 5 16ABR8")
                .sourceUrl("https://electromaroc.ma/pc-portables/lenovo-ideapad-slim-5-1tb")
                .currency("MAD")
                .basePrice(new BigDecimal("9199.00"))
                .deliveryFee(BigDecimal.ZERO)
                .additionalFees(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .warrantyMonths(24)
                .returnWindowDays(14)
                .specificationsJson("{\"processor\":\"AMD Ryzen 7 7730U (8 cores, up to 4.5 GHz)\",\"ram\":\"16 GB DDR4-3200MHz\",\"storage\":\"1 TB M.2 NVMe SSD\",\"display\":\"16\\\" WUXGA (1920x1200) IPS 300nits Anti-glare\",\"battery\":\"56.6Wh Fast Charge\",\"weight\":\"1.89 kg\"}")
                .warrantyConditions("24-month official manufacturer warranty honored nationwide.")
                .returnPolicy("14-day hassle-free return window with free in-store drop-off.")
                .isRecommended(true)
                .valueScore(new BigDecimal("94.20"))
                .build();
        offerB.recalculateTotalPrice();
        laptopComp.addOffer(offerB);

        comparisonRepository.save(laptopComp);
        log.info("Seeded demo comparison for Lenovo IdeaPad Slim 5");
    }

    private void seedPurchasesAndDeadlines(User user) {
        LocalDate today = LocalDate.now();

        // Purchase 1: Samsung Washing Machine (Bought 6 months ago, active warranty)
        Purchase washingMachine = Purchase.builder()
                .userId(user.getId())
                .productName("Samsung EcoBubble 8kg Front Load Washing Machine")
                .modelNumber("WW80TA046AX")
                .category("Major Appliances")
                .sellerName("Cosmos Electro")
                .purchaseDate(today.minusMonths(6))
                .pricePaid(new BigDecimal("4299.00"))
                .currency("MAD")
                .warrantyMonths(24)
                .warrantyExpirationDate(today.minusMonths(6).plusMonths(24))
                .returnDeadline(today.minusMonths(6).plusDays(15))
                .serialNumber("SN-WW80-9928174-MA")
                .status(PurchaseStatus.ACTIVE)
                .notes("Installed in laundry room. Includes 10-year digital inverter motor guarantee.")
                .build();
        washingMachine = purchaseRepository.save(washingMachine);

        // Purchase 2: LG InstaView Refrigerator (Bought 10 days ago, RETURN DEADLINE IN 4 DAYS!)
        Purchase refrigerator = Purchase.builder()
                .userId(user.getId())
                .productName("LG InstaView Door-in-Door Refrigerator 601L")
                .modelNumber("GSX961MCVZ")
                .category("Major Appliances")
                .sellerName("Marjane Mall")
                .purchaseDate(today.minusDays(10))
                .pricePaid(new BigDecimal("11490.00"))
                .currency("MAD")
                .warrantyMonths(36)
                .warrantyExpirationDate(today.minusDays(10).plusMonths(36))
                .returnDeadline(today.plusDays(4)) // 4 days remaining!
                .serialNumber("LG-REF-449102-CAS")
                .status(PurchaseStatus.ACTIVE)
                .notes("Premium matte black finish. Free scheduled delivery and setup included.")
                .build();
        refrigerator = purchaseRepository.save(refrigerator);

        // Seed Deadlines
        Deadline returnDeadline = Deadline.builder()
                .userId(user.getId())
                .purchaseId(refrigerator.getId())
                .title("Return Window Closing: LG InstaView Refrigerator")
                .deadlineType(DeadlineType.RETURN_WINDOW)
                .dueDate(today.plusDays(4))
                .urgency("CRITICAL")
                .isCompleted(false)
                .notes("Last chance to request store pickup and 100% refund from Marjane Mall.")
                .build();
        deadlineRepository.save(returnDeadline);

        Deadline maintenanceDeadline = Deadline.builder()
                .userId(user.getId())
                .purchaseId(washingMachine.getId())
                .title("Filter & Drum Clean: Samsung EcoBubble")
                .deadlineType(DeadlineType.CUSTOM)
                .dueDate(today.plusDays(25))
                .urgency("MEDIUM")
                .isCompleted(false)
                .notes("Run 90°C Eco Drum Clean cycle to maintain warranty validity.")
                .build();
        deadlineRepository.save(maintenanceDeadline);

        // Seed Active Reminder
        Reminder activeReminder = Reminder.builder()
                .deadlineId(returnDeadline.getId())
                .userId(user.getId())
                .triggerDate(today)
                .message("ACTION REQUIRED: Only 4 days remaining in the return window for 'LG InstaView Refrigerator'. Verify ice-maker and temperature stability.")
                .status(ReminderStatus.TRIGGERED)
                .triggeredAt(Instant.now().minusSeconds(1800))
                .build();
        reminderRepository.save(activeReminder);

        // Seed Sample Document & Verified Facts
        Document receiptDoc = Document.builder()
                .userId(user.getId())
                .purchaseId(refrigerator.getId())
                .fileName("Facture_MarjaneMall_LG_InstaView.txt")
                .storedFilePath("./uploads/Facture_MarjaneMall_LG_InstaView.txt")
                .contentType("text/plain")
                .fileSizeBytes(1420L)
                .documentType(DocumentType.INVOICE)
                .rawTextContent("""
                        MARJANE MALL CASABLANCA
                        FACTURE OFFICIELLE N°: MM-2024-88410
                        Date d'achat: il y a 10 jours
                        Client: Karim Bennani
                        
                        DESIGNATION:
                        1x Refrigérateur LG InstaView Door-in-Door 601L (GSX961MCVZ)
                        Prix Unitaire: 11 490,00 MAD
                        Livraison Standard: OFFERTE (0,00 MAD)
                        TOTAL TTC PAYE: 11 490,00 MAD
                        
                        CONDITIONS DE RETOUR ET GARANTIE:
                        - Délai de rétractation: 14 jours à compter de la date de livraison.
                        - Garantie constructeur: 36 mois pièces et main d'œuvre.
                        - Garantie compresseur linéaire: 10 ans.
                        - Exclusions: Dégradations causées par surtension électrique sans parasurtenseur certifié.
                        """)
                .extractionStatus("COMPLETED")
                .build();
        receiptDoc = documentRepository.save(receiptDoc);

        // Add extracted facts with verification statuses
        ExtractedFact fact1 = ExtractedFact.builder()
                .document(receiptDoc)
                .factKey("PRODUCT_NAME")
                .extractedValue("LG InstaView Door-in-Door 601L (GSX961MCVZ)")
                .verifiedValue("LG InstaView Door-in-Door 601L (GSX961MCVZ)")
                .sourceExcerpt("1x Refrigérateur LG InstaView Door-in-Door 601L (GSX961MCVZ)")
                .pageNumber(1)
                .confidenceScore(new BigDecimal("0.98"))
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();
        factRepository.save(fact1);

        ExtractedFact fact2 = ExtractedFact.builder()
                .document(receiptDoc)
                .factKey("PURCHASE_PRICE")
                .extractedValue("11 490,00 MAD")
                .verifiedValue("11490.00")
                .sourceExcerpt("TOTAL TTC PAYE: 11 490,00 MAD")
                .pageNumber(1)
                .confidenceScore(new BigDecimal("0.99"))
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();
        factRepository.save(fact2);

        ExtractedFact fact3 = ExtractedFact.builder()
                .document(receiptDoc)
                .factKey("WARRANTY_DURATION_MONTHS")
                .extractedValue("36 mois")
                .verifiedValue("36")
                .sourceExcerpt("Garantie constructeur: 36 mois pièces et main d'œuvre.")
                .pageNumber(1)
                .confidenceScore(new BigDecimal("0.95"))
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();
        factRepository.save(fact3);

        ExtractedFact fact4 = ExtractedFact.builder()
                .document(receiptDoc)
                .factKey("WARRANTY_EXCLUSIONS")
                .extractedValue("Dégradations causées par surtension électrique sans parasurtenseur certifié")
                .sourceExcerpt("Exclusions: Dégradations causées par surtension électrique sans parasurtenseur certifié.")
                .pageNumber(1)
                .confidenceScore(new BigDecimal("0.91"))
                .verificationStatus(VerificationStatus.PENDING_VERIFICATION)
                .build();
        factRepository.save(fact4);

        log.info("Seeded demo purchases, deadlines, and verified document facts.");
    }
}
