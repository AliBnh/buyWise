package com.buywise;

import com.buywise.modules.auth.dto.AuthResponse;
import com.buywise.modules.auth.dto.RegisterRequest;
import com.buywise.modules.comparison.dto.CreateComparisonRequest;
import com.buywise.modules.comparison.dto.CreateOfferRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Data Isolation & Security Integration Tests")
class ComparisonSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("User B must NOT be able to view or access User A's private purchasing comparisons")
    void testUserDataIsolationBetweenTenants() throws Exception {
        // 1. Register User A
        RegisterRequest userAReq = RegisterRequest.builder()
                .email("user.a@buywise.com")
                .fullName("User Alice")
                .password("Password123!")
                .build();

        MvcResult userARes = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userAReq)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse authA = objectMapper.readValue(userARes.getResponse().getContentAsString(), AuthResponse.class);

        // 2. User A creates a comparison
        CreateComparisonRequest compReq = CreateComparisonRequest.builder()
                .title("Alice's Private Comparison")
                .categoryName("Laptops & Computers")
                .initialOffers(List.of(
                        CreateOfferRequest.builder()
                                .sellerName("Alice Retailer")
                                .title("Laptop A")
                                .basePrice(new BigDecimal("9000.00"))
                                .build()
                ))
                .build();

        MvcResult compRes = mockMvc.perform(post("/api/v1/comparisons")
                        .header("Authorization", "Bearer " + authA.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Alice's Private Comparison"))
                .andReturn();

        Long comparisonId = objectMapper.readTree(compRes.getResponse().getContentAsString()).get("id").asLong();

        // 3. Register User B
        RegisterRequest userBReq = RegisterRequest.builder()
                .email("user.b@buywise.com")
                .fullName("User Bob")
                .password("Password123!")
                .build();

        MvcResult userBRes = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBReq)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse authB = objectMapper.readValue(userBRes.getResponse().getContentAsString(), AuthResponse.class);

        // 4. User B attempts to access User A's comparison by ID -> MUST return 404 (or 403)
        mockMvc.perform(get("/api/v1/comparisons/" + comparisonId)
                        .header("Authorization", "Bearer " + authB.getToken()))
                .andExpect(status().isNotFound());

        // 5. User B lists comparisons -> User A's comparison must NOT appear in User B's list
        mockMvc.perform(get("/api/v1/comparisons")
                        .header("Authorization", "Bearer " + authB.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
