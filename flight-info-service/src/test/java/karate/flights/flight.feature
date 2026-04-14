Feature: flight-info-service — flights API

  Background:
    * url 'http://localhost:30081'

  
  Scenario: get all flights
    Given path '/api/flights'
    When method GET
    Then status 200
    And match response == '#[] #object'
    And assert response.length > 0

  Scenario: get flight by valid ID
    Given path '/api/flights'
    When method GET
    Then status 200
    * def flightId = response[0].id

    Given path '/api/flights/' + flightId
    When method GET
    Then status 200
    And match response.id == flightId
    And match response.flightNumber == '#string'
    And match response.origin == '#string'
    And match response.destination == '#string'
    And match response.price == '#number'

  Scenario: flight ID that does not exist
    Given path '/api/flights/99999'
    When method GET
    Then status 404

  Scenario: search with valid origin and destination
    Given path '/api/flights/search'
    And param origin = 'Delhi'
    And param destination = 'Mumbai'
    When method GET
    Then status 200
    And match response == '#[] #object'

  Scenario: search with unknown route returns not found
    Given path '/api/flights/search'
    And param origin = 'Nowhere'
    And param destination = 'Nowhere'
    When method GET
    Then status 404