Feature: flight-info-service — bookings API

  Background:
    * url 'http://localhost:30081'

  Scenario: book and retrieve a flight booking
    Given path '/api/flights/580/book'
    And request { passengerName: 'Test User', passengerEmail: 'test@email.com' }
    When method POST
    Then status 201
    * def bookingId = response.bookingId
    And match response.bookingReference == '#string'
    And match response.finalPrice == '#number'
    And match response.message == 'Booking confirmed!'

    Given path '/api/flights/bookings/' + bookingId
    When method GET
    Then status 200
    And match response.bookingId == bookingId
    And match response.passengerName == 'Test User'

  Scenario: book a flight that does not exist
    Given path '/api/flights/99999/book'
    And request { passengerName: 'Test User', passengerEmail: 'test@email.com' }
    When method POST
    Then status 400

  Scenario: missing passenger name returns bad request
    Given path '/api/flights/580/book'
    And request { passengerEmail: 'test@email.com' }
    When method POST
    Then status 400

  Scenario: retrieve a booking that does not exist
    Given path '/api/flights/bookings/99999'
    When method GET
    Then status 404
    
  Scenario: book and retrieve a flight booking
    Given path '/api/flights/554/book'
    And request { passengerName: 'Test User', passengerEmail: 'test@email.com' }
    When method POST
    Then status 201
    * def bookingId = response.bookingId
    And match response.bookingReference == '#string'
    And match response.finalPrice == '#number'
    And match response.message == 'Booking confirmed!'

    Given path '/api/flights/bookings/' + bookingId
    When method GET
    Then status 200
    And match response.bookingId == bookingId
    And match response.passengerName == 'Test User'

  Scenario: book with coupon shows inter-service communication
    Given path '/api/flights/580/book'
    And request
      """
      {
        "passengerName": "Coupon Test User",
        "passengerEmail": "coupon@test.com",
        "couponCode": "SAVE10"
      }
      """
    When method POST
    Then status 201
    And match response.bookingReference == '#string'
    And match response.message == 'Booking confirmed!'
    And match response.couponApplied == 'SAVE10'
    And match response.originalPrice == '#number'
    And match response.finalPrice == '#number'
    And match response.savings == '#number'
    And assert response.finalPrice < response.originalPrice
    And assert response.savings > 0