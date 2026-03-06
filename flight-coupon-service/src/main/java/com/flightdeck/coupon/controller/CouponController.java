package com.flightdeck.coupon.controller;

import com.flightdeck.coupon.model.Coupon;
import com.flightdeck.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    
 // Get all coupons
 // GET http://localhost:8082/api/coupons
 @GetMapping
 public ResponseEntity<?> getAllCoupons() {
     try {
         return ResponseEntity.ok(
                 couponService.getAllCoupons());
     } catch (Exception e) {
         return ResponseEntity
                 .status(HttpStatus.INTERNAL_SERVER_ERROR)
                 .body("Error fetching coupons: "
                         + e.getMessage());
     }
 }
 
 
    // Validate coupon code
    // GET http://localhost:8082/api/coupons/validate?code=SAVE10
    @GetMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestParam String code) {
        try {
            boolean isValid = couponService.validateCoupon(code);
            Map<String, Object> response = new HashMap<>();

            if (isValid) {
                Coupon coupon = couponService.getCouponByCode(code);
                response.put("valid", true);
                response.put("code", coupon.getCode());
                response.put("discountPercent", coupon.getDiscountPercent());
                response.put("expiryDate", coupon.getExpiryDate());
                return ResponseEntity.ok(response);
            } else {
                response.put("valid", false);
                response.put("message", "Coupon is invalid, inactive or expired");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error validating coupon: " + e.getMessage());
        }
    }

    // Apply coupon to flight price
    // POST http://localhost:8082/api/coupons/apply
    // Body: { "originalPrice": 2500.00, "code": "SAVE10" }
    @PostMapping("/apply")
    public ResponseEntity<?> applyCoupon(@RequestBody Map<String, Object> request) {
        try {
            Double originalPrice = Double.valueOf(request.get("originalPrice").toString());
            String code = request.get("code").toString();

            double discountedPrice = couponService.applyDiscount(originalPrice, code);
            Coupon coupon = couponService.getCouponByCode(code);

            Map<String, Object> response = new HashMap<>();
            response.put("code", code);
            response.put("originalPrice", originalPrice);
            response.put("discountPercent", coupon.getDiscountPercent());
            response.put("discountedPrice", discountedPrice);
            response.put("savings", originalPrice - discountedPrice);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
