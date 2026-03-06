package com.flightdeck.coupon.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Double discountPercent;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private Boolean active;

    
    public Coupon() {}

    public Coupon(String code, Double discountPercent,
                  LocalDate expiryDate, Boolean active) {
        this.code            = code;
        this.discountPercent = discountPercent;
        this.expiryDate      = expiryDate;
        this.active          = active;
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Double discountPercent) { this.discountPercent = discountPercent; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
