package com.flightdeck.coupon.service;

import com.flightdeck.coupon.model.Coupon;
import com.flightdeck.coupon.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;
    
 // Get all coupons
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    //Validate coupon code
    public boolean validateCoupon(String code) {
        Optional<Coupon> couponOpt = couponRepository
                .findByCodeIgnoreCase(code);

        if (couponOpt.isEmpty()) {
            return false; // Coupon not found
        }

        Coupon coupon = couponOpt.get();

        if (!coupon.getActive()) {
            return false; // Coupon is inactive
        }

        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            return false; // Coupon is expired
        }

        return true;
    }

    // Get coupon details
    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException(
                        "Coupon not found: " + code));
    }

    // Apply coupon to a flight price
    public double applyDiscount(Double originalPrice, String code) {
        if (!validateCoupon(code)) {
            throw new RuntimeException(
                    "Invalid or expired coupon: " + code);
        }

        Coupon coupon = getCouponByCode(code);
        double discount = (originalPrice * coupon.getDiscountPercent()) / 100;
        return originalPrice - discount;
    }
}
