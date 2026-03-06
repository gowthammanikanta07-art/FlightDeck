package com.flightdeck.coupon;

import com.flightdeck.coupon.model.Coupon;
import com.flightdeck.coupon.repository.CouponRepository;
import com.flightdeck.coupon.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    private Coupon validCoupon;
    private Coupon expiredCoupon;
    private Coupon inactiveCoupon;

    @BeforeEach
    void setUp() {
        
        validCoupon = new Coupon(
                "SAVE10",
                10.0,
                LocalDate.now().plusDays(30),
                true
        );
        validCoupon.setId(1L);

        
        expiredCoupon = new Coupon(
                "EXPIRED20",
                20.0,
                LocalDate.now().minusDays(1),
                true
        );
        expiredCoupon.setId(2L);

       
        inactiveCoupon = new Coupon(
                "INACTIVE30",
                30.0,
                LocalDate.now().plusDays(30),
                false
        );
        inactiveCoupon.setId(3L);
    }

    // Validate Coupon - Valid
    @Test
    void testValidateCoupon_Valid() {
        when(couponRepository.findByCodeIgnoreCase("SAVE10"))
                .thenReturn(Optional.of(validCoupon));

        boolean result = couponService.validateCoupon("SAVE10");

        assertTrue(result);
        verify(couponRepository, times(1))
                .findByCodeIgnoreCase("SAVE10");
    }

    // Validate Coupon - Not Found
    @Test
    void testValidateCoupon_NotFound() {
        when(couponRepository.findByCodeIgnoreCase("INVALID"))
                .thenReturn(Optional.empty());

        boolean result = couponService.validateCoupon("INVALID");

        assertFalse(result);
    }

    // Validate Coupon - Expired
    @Test
    void testValidateCoupon_Expired() {
        when(couponRepository.findByCodeIgnoreCase("EXPIRED20"))
                .thenReturn(Optional.of(expiredCoupon));

        boolean result = couponService.validateCoupon("EXPIRED20");

        assertFalse(result);
    }

    // Validate Coupon - Inactive
    @Test
    void testValidateCoupon_Inactive() {
        when(couponRepository.findByCodeIgnoreCase("INACTIVE30"))
                .thenReturn(Optional.of(inactiveCoupon));

        boolean result = couponService.validateCoupon("INACTIVE30");

        assertFalse(result);
    }

    // Get Coupon By Code - Found
    @Test
    void testGetCouponByCode_Found() {
        when(couponRepository.findByCodeIgnoreCase("SAVE10"))
                .thenReturn(Optional.of(validCoupon));

        Coupon result = couponService.getCouponByCode("SAVE10");

        assertNotNull(result);
        assertEquals("SAVE10", result.getCode());
        assertEquals(10.0, result.getDiscountPercent());
    }

    // Get Coupon By Code - Not Found
    @Test
    void testGetCouponByCode_NotFound() {
        when(couponRepository.findByCodeIgnoreCase("UNKNOWN"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> couponService.getCouponByCode("UNKNOWN")
        );

        assertEquals("Coupon not found: UNKNOWN", exception.getMessage());
    }

    // Apply Discount - Success
    @Test
    void testApplyDiscount_Success() {
        when(couponRepository.findByCodeIgnoreCase("SAVE10"))
                .thenReturn(Optional.of(validCoupon));

        double result = couponService.applyDiscount(2500.00, "SAVE10");

        assertEquals(2250.00, result);
        // 10% of 2500 = 250 discount → 2500 - 250 = 2250
    }

    //  Apply Discount - Invalid Coupon
    @Test
    void testApplyDiscount_InvalidCoupon() {
        when(couponRepository.findByCodeIgnoreCase("INVALID"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> couponService.applyDiscount(2500.00, "INVALID")
        );

        assertTrue(exception.getMessage()
                .contains("Invalid or expired coupon"));
    }

    // Apply Discount - Expired Coupon
    @Test
    void testApplyDiscount_ExpiredCoupon() {
        when(couponRepository.findByCodeIgnoreCase("EXPIRED20"))
                .thenReturn(Optional.of(expiredCoupon));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> couponService.applyDiscount(2500.00, "EXPIRED20")
        );

        assertTrue(exception.getMessage()
                .contains("Invalid or expired coupon"));
    }
    
 // Get All Coupons - Success
    @Test
    void testGetAllCoupons_Success() {
        when(couponRepository.findAll())
                .thenReturn(List.of(validCoupon));

        List<Coupon> result = couponService.getAllCoupons();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SAVE10",
                result.get(0).getCode());
    }

    // Get All Coupons - Empty
    @Test
    void testGetAllCoupons_Empty() {
        when(couponRepository.findAll())
                .thenReturn(Collections.emptyList());

        List<Coupon> result = couponService.getAllCoupons();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
 
}