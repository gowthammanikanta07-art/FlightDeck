package com.flightdeck.coupon;

import com.flightdeck.coupon.model.Coupon;
import com.flightdeck.coupon.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CouponControllerIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CouponRepository couponRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/coupons";

        couponRepository.deleteAll();

        // Valid coupon
        couponRepository.save(new Coupon(
                "SAVE10", 10.0,
                LocalDate.now().plusDays(30),
                true));

        // Expired coupon
        couponRepository.save(new Coupon(
                "EXPIRED20", 20.0,
                LocalDate.now().minusDays(1),
                true));

        // Inactive coupon
        couponRepository.save(new Coupon(
                "INACTIVE30", 30.0,
                LocalDate.now().plusDays(30),
                false));
    }

    // Get All Coupons
    @Test
    void testGetAllCoupons_Integration() {
        ResponseEntity<String> response = restTemplate
                .getForEntity(baseUrl, String.class);

        assertEquals(HttpStatus.OK,
                response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("SAVE10"));
        assertTrue(response.getBody().contains("EXPIRED20"));
        assertTrue(response.getBody().contains("INACTIVE30"));
    }

    // Validate Coupon - Valid
    @Test
    void testValidateCoupon_Valid_Integration() {
        ResponseEntity<String> response = restTemplate
                .getForEntity(
                        baseUrl + "/validate?code=SAVE10",
                        String.class);

        assertEquals(HttpStatus.OK,
                response.getStatusCode());
        assertTrue(response.getBody().contains("true"));
        assertTrue(response.getBody().contains("SAVE10"));
        assertTrue(response.getBody()
                .contains("discountPercent"));
    }

    // Validate Coupon - Invalid
    @Test
    void testValidateCoupon_Invalid_Integration() {
        ResponseEntity<String> response = restTemplate
                .getForEntity(
                        baseUrl + "/validate?code=INVALID",
                        String.class);

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());
        assertTrue(response.getBody().contains("false"));
    }

    // Validate Coupon - Expired
    @Test
    void testValidateCoupon_Expired_Integration() {
        ResponseEntity<String> response = restTemplate
                .getForEntity(
                        baseUrl + "/validate?code=EXPIRED20",
                        String.class);

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());
        assertTrue(response.getBody().contains("false"));
    }

    // Validate Coupon - Inactive
    @Test
    void testValidateCoupon_Inactive_Integration() {
        ResponseEntity<String> response = restTemplate
                .getForEntity(
                        baseUrl + "/validate?code=INACTIVE30",
                        String.class);

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());
        assertTrue(response.getBody().contains("false"));
    }

    // Validate Coupon - Case Insensitive
    @Test
    void testValidateCoupon_CaseInsensitive_Integration() {
        ResponseEntity<String> response = restTemplate
                .getForEntity(
                        baseUrl + "/validate?code=save10",
                        String.class);

        assertEquals(HttpStatus.OK,
                response.getStatusCode());
        assertTrue(response.getBody().contains("true"));
    }

    //  Apply Coupon - Success
    @Test
    void testApplyCoupon_Success_Integration() {
        Map<String, Object> request = new HashMap<>();
        request.put("originalPrice", 2500.00);
        request.put("code", "SAVE10");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate
                .postForEntity(
                        baseUrl + "/apply",
                        new HttpEntity<>(request, headers),
                        String.class);

        assertEquals(HttpStatus.OK,
                response.getStatusCode());
        assertTrue(response.getBody()
                .contains("originalPrice"));
        assertTrue(response.getBody()
                .contains("discountedPrice"));
        assertTrue(response.getBody()
                .contains("savings"));
        assertTrue(response.getBody().contains("2250"));
    }

    // Apply Coupon - Expired
    @Test
    void testApplyCoupon_Expired_Integration() {
        Map<String, Object> request = new HashMap<>();
        request.put("originalPrice", 2500.00);
        request.put("code", "EXPIRED20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate
                .postForEntity(
                        baseUrl + "/apply",
                        new HttpEntity<>(request, headers),
                        String.class);

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());
    }

    // Apply Coupon - Invalid
    @Test
    void testApplyCoupon_Invalid_Integration() {
        Map<String, Object> request = new HashMap<>();
        request.put("originalPrice", 2500.00);
        request.put("code", "INVALID");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate
                .postForEntity(
                        baseUrl + "/apply",
                        new HttpEntity<>(request, headers),
                        String.class);

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());
    }
}