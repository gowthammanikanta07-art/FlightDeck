Feature: flight-coupon-service — coupons API

  Background:
    * url 'http://localhost:30082'
    
  Scenario: get all coupons
    Given path '/api/coupons'
    When method GET
    Then status 200
    And match response == '#[] #object'

  Scenario: validate a valid coupon code
    Given path '/api/coupons/validate'
    And param code = 'SAVE10'
    When method GET
    Then status 200
    And match response.valid == true
    And match response.discountPercent == '#number'

  Scenario: validate an invalid coupon code
    Given path '/api/coupons/validate'
    And param code = 'INVALIDCODE'
    When method GET
    Then status 400
    And match response.valid == false

  Scenario: apply a valid coupon to a price
    Given path '/api/coupons/apply'
    And request { originalPrice: 2500.00, code: 'SAVE10' }
    When method POST
    Then status 200
    And match response.discountedPrice == '#number'
    And match response.savings == '#number'
    And assert response.discountedPrice < response.originalPrice
    And assert response.savings > 0

  Scenario: apply an expired or invalid coupon
    Given path '/api/coupons/apply'
    And request { originalPrice: 2500.00, code: 'EXPIREDCODE' }
    When method POST
    Then status 400