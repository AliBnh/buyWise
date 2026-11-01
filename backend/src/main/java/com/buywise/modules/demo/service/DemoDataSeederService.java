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
 * - Rich Demo State: Seeds extensive purchase, comparison, document, and deadline records reflecting a real consumer portfolio.
 * - Multi-Currency Support: Defaults to Moroccan Dirham (MAD) with realistic market pricing.
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
            seedSmartphoneComparison(demoUser);
            seedOledTvComparison(demoUser);
            seedVacuumComparison(demoUser);
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

    private void seedSmartphoneComparison(User user) {
        Comparison phoneComp = Comparison.builder()
                .userId(user.getId())
                .title("Apple iPhone 16 Pro 256GB — Reseller Comparison")
                .categoryName("Smartphones & Mobile")
                .status(ComparisonStatus.ANALYZED)
                .notes("Comparing official Apple authorized resellers vs independent grey-market import. Key criteria: Moroccan carrier eSIM support, VAT invoice, and local Apple warranty.")
                .aiAnalysisJson("{\"executiveSummary\":\"Comparing 3 offers for iPhone 16 Pro 256GB. Virgin Megastore and FNAC offer official Apple 1-year Moroccan warranty. Derb Ghallef offers a lower base price but sells a US-locked eSIM-only model without official local warranty.\",\"recommendedOfferId\":2,\"recommendedOfferSeller\":\"FNAC Maroc\",\"recommendationRationale\":\"FNAC Maroc includes an exclusive 24-month extended retailer warranty and 1,000 MAD trade-in voucher, making the effective cost lower than grey-market imports when warranty risk is factored.\",\"keySpecificationDifferences\":[\"Warranty: 12 months (Virgin) vs 24 months (FNAC) vs 3 months shop (Derb Ghallef)\",\"SIM Slot: Physical SIM + eSIM (Virgin & FNAC) vs Dual eSIM only (Derb Ghallef US variant)\"],\"missingInformationAlerts\":[\"Verify whether Derb Ghallef includes a valid VAT invoice for business expense deduction.\"],\"purchasingRisks\":[\"US models (A3081) do not have a physical SIM tray and cannot be serviced by Moroccan Apple Authorized Service Providers.\"],\"confidenceScore\":0.97}")
                .aiAnalyzedAt(Instant.now().minusSeconds(7200))
                .build();

        Offer offerVirgin = Offer.builder()
                .sellerName("Virgin Megastore Maroc")
                .title("iPhone 16 Pro 256GB Natural Titanium")
                .productModel("A3293")
                .currency("MAD")
                .basePrice(new BigDecimal("14990.00"))
                .deliveryFee(BigDecimal.ZERO)
                .additionalFees(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .warrantyMonths(12)
                .returnWindowDays(14)
                .specificationsJson("{\"chip\":\"A18 Pro with 6-core GPU\",\"storage\":\"256 GB NVMe\",\"camera\":\"48MP Fusion + 48MP Ultra Wide + 12MP 5x Telephoto\",\"display\":\"6.3\\\" Super Retina XDR OLED ProMotion 120Hz\",\"connectivity\":\"5G, Wi-Fi 7, USB-C 3.0 (10Gbps)\"}")
                .warrantyConditions("Official Apple 1-year international warranty valid at all Moroccan AASPs.")
                .returnPolicy("14-day standard return window for sealed items.")
                .isRecommended(false)
                .valueScore(new BigDecimal("88.40"))
                .build();
        offerVirgin.recalculateTotalPrice();
        phoneComp.addOffer(offerVirgin);

        Offer offerFnac = Offer.builder()
                .sellerName("FNAC Maroc")
                .title("iPhone 16 Pro 256GB Natural Titanium + FNAC Care")
                .productModel("A3293")
                .currency("MAD")
                .basePrice(new BigDecimal("15290.00"))
                .deliveryFee(BigDecimal.ZERO)
                .additionalFees(BigDecimal.ZERO)
                .discountAmount(new BigDecimal("800.00"))
                .warrantyMonths(24)
                .returnWindowDays(15)
                .specificationsJson("{\"chip\":\"A18 Pro with 6-core GPU\",\"storage\":\"256 GB NVMe\",\"camera\":\"48MP Fusion + 48MP Ultra Wide + 12MP 5x Telephoto\",\"display\":\"6.3\\\" Super Retina XDR OLED ProMotion 120Hz\",\"connectivity\":\"5G, Wi-Fi 7, USB-C 3.0 (10Gbps)\"}")
                .warrantyConditions("24-month warranty: 1st year Apple official + 2nd year FNAC Care replacement guarantee.")
                .returnPolicy("15-day satisfaction guarantee with immediate voucher refund.")
                .isRecommended(true)
                .valueScore(new BigDecimal("95.60"))
                .build();
        offerFnac.recalculateTotalPrice();
        phoneComp.addOffer(offerFnac);

        Offer offerGrey = Offer.builder()
                .sellerName("Derb Ghallef Tech Store")
                .title("iPhone 16 Pro 256GB (US Model - No SIM Tray)")
                .productModel("A3081")
                .currency("MAD")
                .basePrice(new BigDecimal("13800.00"))
                .deliveryFee(BigDecimal.ZERO)
                .additionalFees(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .warrantyMonths(3)
                .returnWindowDays(0)
                .specificationsJson("{\"chip\":\"A18 Pro\",\"storage\":\"256 GB\",\"display\":\"6.3\\\" OLED ProMotion\",\"sim\":\"Dual eSIM only (No physical nano-SIM slot)\"}")
                .warrantyConditions("3-month shop warranty. No official Apple coverage in Morocco for US models.")
                .returnPolicy("No returns or exchanges once box seal is broken.")
                .isRecommended(false)
                .valueScore(new BigDecimal("68.10"))
                .build();
        offerGrey.recalculateTotalPrice();
        phoneComp.addOffer(offerGrey);

        comparisonRepository.save(phoneComp);
        log.info("Seeded demo comparison for iPhone 16 Pro");
    }

    private void seedOledTvComparison(User user) {
        Comparison tvComp = Comparison.builder()
                .userId(user.getId())
                .title("Sony BRAVIA XR 65'' OLED (A80L) vs Competitors")
                .categoryName("Televisions & Home Theater")
                .status(ComparisonStatus.ANALYZED)
                .notes("Evaluating high-end 65-inch OLED for living room cinema and PlayStation 5 gaming. Comparing installation fees and burn-in warranty policies.")
                .aiAnalysisJson("{\"executiveSummary\":\"Comparing Electroplanet and Cosmos Electro for Sony BRAVIA XR-65A80L OLED. Electroplanet includes certified wall-mounting and full 24-month nationwide warranty.\",\"recommendedOfferId\":1,\"recommendedOfferSeller\":\"Electroplanet\",\"recommendationRationale\":\"Electroplanet includes free wall mounting (worth 350 MAD) and an extra 12 months manufacturer warranty, compensating for the small price gap.\",\"keySpecificationDifferences\":[\"Warranty: 24 months official vs 12 months seller\",\"Installation: Free professional wall mounting included at Electroplanet\"],\"missingInformationAlerts\":[\"Confirm HDMI 2.1 cable is included in box.\"],\"purchasingRisks\":[\"Cosmos does not explicitly cover accidental OLED panel burn-in under importer warranty.\"],\"confidenceScore\":0.94}")
                .aiAnalyzedAt(Instant.now().minusSeconds(14400))
                .build();

        Offer offerEp = Offer.builder()
                .sellerName("Electroplanet")
                .title("Sony BRAVIA XR 65\" A80L 4K HDR OLED Google TV")
                .productModel("XR-65A80L")
                .currency("MAD")
                .basePrice(new BigDecimal("21990.00"))
                .deliveryFee(BigDecimal.ZERO)
                .additionalFees(BigDecimal.ZERO)
                .discountAmount(new BigDecimal("1000.00"))
                .warrantyMonths(24)
                .returnWindowDays(10)
                .specificationsJson("{\"panel\":\"4K OLED (3840x2160)\",\"refreshRate\":\"120Hz Native\",\"processor\":\"Cognitive Processor XR\",\"audio\":\"Acoustic Surface Audio+ (3.2 ch, 50W)\",\"gaming\":\"HDMI 2.1 4K120, VRR, ALLM, Auto HDR Tone Mapping\"}")
                .warrantyConditions("24-month Sony Morocco official warranty covering panel and power supply.")
                .returnPolicy("10-day exchange window with free collection.")
                .isRecommended(true)
                .valueScore(new BigDecimal("93.80"))
                .build();
        offerEp.recalculateTotalPrice();
        tvComp.addOffer(offerEp);

        Offer offerCosmos = Offer.builder()
                .sellerName("Cosmos Electro")
                .title("Sony 65\" 4K OLED XR-65A80L")
                .productModel("XR-65A80L")
                .currency("MAD")
                .basePrice(new BigDecimal("20490.00"))
                .deliveryFee(new BigDecimal("150.00"))
                .additionalFees(new BigDecimal("350.00")) // Wall mount installation fee
                .discountAmount(BigDecimal.ZERO)
                .warrantyMonths(12)
                .returnWindowDays(7)
                .specificationsJson("{\"panel\":\"4K OLED (3840x2160)\",\"refreshRate\":\"120Hz\",\"processor\":\"Cognitive Processor XR\"}")
                .warrantyConditions("12-month standard importer warranty.")
                .returnPolicy("7-day return period strictly in unopened packaging.")
                .isRecommended(false)
                .valueScore(new BigDecimal("84.20"))
                .build();
        offerCosmos.recalculateTotalPrice();
        tvComp.addOffer(offerCosmos);

        comparisonRepository.save(tvComp);
        log.info("Seeded demo comparison for Sony OLED TV");
    }

    private void seedVacuumComparison(User user) {
        Comparison vacuumComp = Comparison.builder()
                .userId(user.getId())
                .title("Dyson V15 Detect Absolute — Cordless Vacuum")
                .categoryName("Small Appliances & Cleaning")
                .status(ComparisonStatus.DRAFT)
                .notes("Researching laser illumination head and suction power. Evaluating Dyson direct vs Jumia official store.")
                .build();

        Offer offerDyson = Offer.builder()
                .sellerName("Dyson Online Maroc")
                .title("Dyson V15 Detect Absolute (Gold/Iron)")
                .productModel("368340-01")
                .currency("MAD")
                .basePrice(new BigDecimal("7499.00"))
                .deliveryFee(BigDecimal.ZERO)
                .additionalFees(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .warrantyMonths(24)
                .returnWindowDays(30)
                .specificationsJson("{\"suction\":\"240 AW\",\"runTime\":\"Up to 60 minutes\",\"filtration\":\"Whole-machine HEPA 99.99% to 0.1 microns\",\"weight\":\"3.0 kg\",\"heads\":\"Digital Motorbar + Fluffy Optic\"}")
                .warrantyConditions("24-month official Dyson guarantee with free home pick-up repair service.")
                .returnPolicy("30-day risk-free money-back trial.")
                .isRecommended(true)
                .valueScore(new BigDecimal("91.50"))
                .build();
        offerDyson.recalculateTotalPrice();
        vacuumComp.addOffer(offerDyson);

        comparisonRepository.save(vacuumComp);
        log.info("Seeded demo comparison for Dyson V15");
    }

    private void seedPurchasesAndDeadlines(User user) {
        LocalDate today = LocalDate.now();

        // 1. Samsung Washing Machine
        Purchase washingMachine = Purchase.builder()
                .userId(user.getId())
                .productName("Samsung EcoBubble 8kg Front Load Washing Machine")
                .modelNumber("WW80TA046AX")
                .category("Major Appliances")
                .sellerName("Cosmos Electro Casablanca")
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

        // 2. LG InstaView Refrigerator (Return deadline in 4 days!)
        Purchase refrigerator = Purchase.builder()
                .userId(user.getId())
                .productName("LG InstaView Door-in-Door Refrigerator 601L")
                .modelNumber("GSX961MCVZ")
                .category("Major Appliances")
                .sellerName("Marjane Mall Casablanca")
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

        // 3. Apple MacBook Pro 14" M3 Pro (Warranty expiring in 30 days)
        Purchase macbook = Purchase.builder()
                .userId(user.getId())
                .productName("Apple MacBook Pro 14\" M3 Pro (18GB RAM / 512GB SSD)")
                .modelNumber("MRX33FN/A")
                .category("Laptops & Computers")
                .sellerName("Uno.ma (Apple Authorised Reseller)")
                .purchaseDate(today.minusMonths(11))
                .pricePaid(new BigDecimal("23990.00"))
                .currency("MAD")
                .warrantyMonths(12)
                .warrantyExpirationDate(today.plusDays(30)) // Expiring in 30 days!
                .returnDeadline(today.minusMonths(11).plusDays(14))
                .serialNumber("C02G4088MD6R")
                .status(PurchaseStatus.ACTIVE)
                .notes("Space Black. Primary development machine. Check AppleCare+ renewal option before expiration.")
                .build();
        macbook = purchaseRepository.save(macbook);

        // 4. Sony WH-1000XM5 Wireless Headphones
        Purchase headphones = Purchase.builder()
                .userId(user.getId())
                .productName("Sony WH-1000XM5 Noise-Cancelling Headphones")
                .modelNumber("WH1000XM5/B")
                .category("Audio & Accessories")
                .sellerName("Electroplanet Casablanca Anfa")
                .purchaseDate(today.minusMonths(8))
                .pricePaid(new BigDecimal("3499.00"))
                .currency("MAD")
                .warrantyMonths(24)
                .warrantyExpirationDate(today.minusMonths(8).plusMonths(24))
                .returnDeadline(today.minusMonths(8).plusDays(14))
                .serialNumber("SN-SONY-XM5-99412")
                .status(PurchaseStatus.ACTIVE)
                .notes("Black finish. Used daily for remote meetings and travel.")
                .build();
        headphones = purchaseRepository.save(headphones);

        // 5. De'Longhi Magnifica S Espresso Machine
        Purchase coffeeMachine = Purchase.builder()
                .userId(user.getId())
                .productName("De'Longhi Magnifica S Bean-to-Cup Espresso Machine")
                .modelNumber("ECAM22.110.B")
                .category("Kitchen Appliances")
                .sellerName("Cosmos Electro Rabat")
                .purchaseDate(today.minusMonths(3))
                .pricePaid(new BigDecimal("3890.00"))
                .currency("MAD")
                .warrantyMonths(24)
                .warrantyExpirationDate(today.minusMonths(3).plusMonths(24))
                .returnDeadline(today.minusMonths(3).plusDays(14))
                .serialNumber("DL-MAGS-772910")
                .status(PurchaseStatus.ACTIVE)
                .notes("Includes 1kg Lavazza espresso beans starter pack. Descaling filter cycle due quarterly.")
                .build();
        coffeeMachine = purchaseRepository.save(coffeeMachine);

        // 6. PlayStation 5 Digital Edition (Expired warranty demonstration)
        Purchase ps5 = Purchase.builder()
                .userId(user.getId())
                .productName("Sony PlayStation 5 Digital Edition (Slim)")
                .modelNumber("CFI-2000B01")
                .category("Gaming & Entertainment")
                .sellerName("Virgin Megastore Marrakech")
                .purchaseDate(today.minusMonths(18))
                .pricePaid(new BigDecimal("4799.00"))
                .currency("MAD")
                .warrantyMonths(12)
                .warrantyExpirationDate(today.minusMonths(6)) // Expired 6 months ago!
                .returnDeadline(today.minusMonths(18).plusDays(14))
                .serialNumber("PS5-SLIM-098234-MA")
                .status(PurchaseStatus.ACTIVE)
                .notes("Maintained in living room console. Out of warranty; clean dust intake vents every 6 months.")
                .build();
        ps5 = purchaseRepository.save(ps5);

        // --- Seed Deadlines ---
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

        Deadline appleCareDeadline = Deadline.builder()
                .userId(user.getId())
                .purchaseId(macbook.getId())
                .title("AppleCare+ Warranty Expiration: MacBook Pro 14\"")
                .deadlineType(DeadlineType.WARRANTY_EXPIRY)
                .dueDate(today.plusDays(30))
                .urgency("HIGH")
                .isCompleted(false)
                .notes("Standard 1-year warranty expiring. Decide whether to extend AppleCare annual coverage.")
                .build();
        deadlineRepository.save(appleCareDeadline);

        Deadline descalingDeadline = Deadline.builder()
                .userId(user.getId())
                .purchaseId(coffeeMachine.getId())
                .title("Quarterly Descaling Cycle: De'Longhi Magnifica S")
                .deadlineType(DeadlineType.CUSTOM)
                .dueDate(today.plusDays(12))
                .urgency("MEDIUM")
                .isCompleted(false)
                .notes("Use EcoDecalk solution to prevent limescale buildup and maintain pressure warranty.")
                .build();
        deadlineRepository.save(descalingDeadline);

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

        Deadline ps5Checkup = Deadline.builder()
                .userId(user.getId())
                .purchaseId(ps5.getId())
                .title("Ventilation Dust Cleaning: PlayStation 5")
                .deadlineType(DeadlineType.CUSTOM)
                .dueDate(today.minusDays(5))
                .urgency("LOW")
                .isCompleted(true) // Marked completed to showcase completed history!
                .notes("Vents vacuumed and internal fan cleared of dust.")
                .build();
        deadlineRepository.save(ps5Checkup);

        // --- Seed Active Reminders ---
        Reminder activeReminder1 = Reminder.builder()
                .deadlineId(returnDeadline.getId())
                .userId(user.getId())
                .triggerDate(today)
                .message("ACTION REQUIRED: Only 4 days remaining in the return window for 'LG InstaView Refrigerator'. Verify ice-maker and temperature stability.")
                .status(ReminderStatus.TRIGGERED)
                .triggeredAt(Instant.now().minusSeconds(1800))
                .build();
        reminderRepository.save(activeReminder1);

        Reminder activeReminder2 = Reminder.builder()
                .deadlineId(appleCareDeadline.getId())
                .userId(user.getId())
                .triggerDate(today)
                .message("WARRANTY NOTICE: 30 days remaining on your Apple MacBook Pro 14\" official warranty. Run Apple Hardware Diagnostics to check battery health.")
                .status(ReminderStatus.TRIGGERED)
                .triggeredAt(Instant.now().minusSeconds(3600))
                .build();
        reminderRepository.save(activeReminder2);

        // --- Seed Documents & Verified Facts ---
        Document receiptDoc1 = Document.builder()
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
        receiptDoc1 = documentRepository.save(receiptDoc1);

        factRepository.save(ExtractedFact.builder()
                .document(receiptDoc1)
                .factKey("PRODUCT_NAME")
                .extractedValue("LG InstaView Door-in-Door 601L (GSX961MCVZ)")
                .verifiedValue("LG InstaView Door-in-Door 601L (GSX961MCVZ)")
                .sourceExcerpt("1x Refrigérateur LG InstaView Door-in-Door 601L (GSX961MCVZ)")
                .pageNumber(1)
                .confidenceScore(new BigDecimal("0.98"))
                .verificationStatus(VerificationStatus.VERIFIED)
                .build());

        factRepository.save(ExtractedFact.builder()
                .document(receiptDoc1)
                .factKey("PURCHASE_PRICE")
                .extractedValue("11 490,00 MAD")
                .verifiedValue("11490.00")
                .sourceExcerpt("TOTAL TTC PAYE: 11 490,00 MAD")
                .pageNumber(1)
                .confidenceScore(new BigDecimal("0.99"))
                .verificationStatus(VerificationStatus.VERIFIED)
                .build());

        factRepository.save(ExtractedFact.builder()
                .document(receiptDoc1)
                .factKey("WARRANTY_DURATION_MONTHS")
                .extractedValue("36 mois")
                .verifiedValue("36")
                .sourceExcerpt("Garantie constructeur: 36 mois pièces et main d'œuvre.")
                .pageNumber(1)
                .confidenceScore(new BigDecimal("0.95"))
                .verificationStatus(VerificationStatus.VERIFIED)
                .build());

        factRepository.save(ExtractedFact.builder()
                .document(receiptDoc1)
                .factKey("WARRANTY_EXCLUSIONS")
                .extractedValue("Dégradations causées par surtension électrique sans parasurtenseur certifié")
                .sourceExcerpt("Exclusions: Dégradations causées par surtension électrique sans parasurtenseur certifié.")
                .pageNumber(1)
                .confidenceScore(new BigDecimal("0.91"))
                .verificationStatus(VerificationStatus.PENDING_VERIFICATION)
                .build());

        // Document 2: MacBook Pro Invoice
        Document receiptDoc2 = Document.builder()
                .userId(user.getId())
                .purchaseId(macbook.getId())
                .fileName("Facture_Uno_MacBook_Pro_M3.txt")
                .storedFilePath("./uploads/Facture_Uno_MacBook_Pro_M3.txt")
                .contentType("text/plain")
                .fileSizeBytes(1680L)
                .documentType(DocumentType.INVOICE)
                .rawTextContent("""
                        UNO.MA - APPLE AUTHORISED RESELLER
                        BOULEVARD D'ANFA, CASABLANCA
                        FACTURE N°: UNO-2023-10492
                        Client: Karim Bennani
                        
                        1x MacBook Pro 14" M3 Pro 18GB/512GB Space Black (MRX33FN/A)
                        Numéro de Série: C02G4088MD6R
                        Prix TTC: 23 990,00 MAD
                        
                        Garantie légale Apple: 12 mois à compter de la date de facture.
                        Service Après-Vente agréé: Uno Service Center Casablanca.
                        """)
                .extractionStatus("COMPLETED")
                .build();
        receiptDoc2 = documentRepository.save(receiptDoc2);

        factRepository.save(ExtractedFact.builder()
                .document(receiptDoc2)
                .factKey("PRODUCT_NAME")
                .extractedValue("MacBook Pro 14\" M3 Pro 18GB/512GB")
                .verifiedValue("MacBook Pro 14\" M3 Pro 18GB/512GB")
                .sourceExcerpt("1x MacBook Pro 14\" M3 Pro 18GB/512GB Space Black (MRX33FN/A)")
                .pageNumber(1)
                .confidenceScore(new BigDecimal("0.99"))
                .verificationStatus(VerificationStatus.VERIFIED)
                .build());

        factRepository.save(ExtractedFact.builder()
                .document(receiptDoc2)
                .factKey("SERIAL_NUMBER")
                .extractedValue("C02G4088MD6R")
                .verifiedValue("C02G4088MD6R")
                .sourceExcerpt("Numéro de Série: C02G4088MD6R")
                .pageNumber(1)
                .confidenceScore(new BigDecimal("1.00"))
                .verificationStatus(VerificationStatus.VERIFIED)
                .build());

        factRepository.save(ExtractedFact.builder()
                .document(receiptDoc2)
                .factKey("PURCHASE_PRICE")
                .extractedValue("23 990,00 MAD")
                .verifiedValue("23990.00")
                .sourceExcerpt("Prix TTC: 23 990,00 MAD")
                .pageNumber(1)
                .confidenceScore(new BigDecimal("0.99"))
                .verificationStatus(VerificationStatus.VERIFIED)
                .build());

        log.info("Seeded comprehensive demo comparisons, 6 purchases, 5 deadlines, and verified document facts.");
    }
}
