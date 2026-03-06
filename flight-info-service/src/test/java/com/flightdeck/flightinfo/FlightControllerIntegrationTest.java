package com.flightdeck.flightinfo;

import com.flightdeck.flightinfo.model.Flight;
import com.flightdeck.flightinfo.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FlightControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FlightRepository flightRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/flights";

        flightRepository.deleteAll();

        Flight flight = new Flight(
                "AI101", "Air India", "Delhi", "Mumbai",
                LocalDateTime.of(2025, 6, 1, 8, 0),
                LocalDateTime.of(2025, 6, 1, 10, 0),
                2500.00, 120
        );
        flightRepository.save(flight);
    }

    // Get All Flights
    @Test
    void testGetAllFlights_Integration() {
        ResponseEntity<String> response = restTemplate
                .getForEntity(baseUrl, String.class);

        assertEquals(HttpStatus.OK,
                response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("AI101"));
    }

    // Search Flights - Found
    @Test
    void testSearchFlights_Found_Integration() {
        ResponseEntity<String> response = restTemplate
                .getForEntity(
                        baseUrl + "/search?origin=Delhi&destination=Mumbai",
                        String.class);

        assertEquals(HttpStatus.OK,
                response.getStatusCode());
        assertTrue(response.getBody().contains("AI101"));
        assertTrue(response.getBody().contains("Delhi"));
        assertTrue(response.getBody().contains("Mumbai"));
    }

    // Search Flights - Not Found
    @Test
    void testSearchFlights_NotFound_Integration() {
        ResponseEntity<String> response = restTemplate
                .getForEntity(
                        baseUrl + "/search?origin=Delhi&destination=London",
                        String.class);

        assertEquals(HttpStatus.NOT_FOUND,
                response.getStatusCode());
    }

    //  Get Flight By ID - Found
    @Test
    void testGetFlightById_Found_Integration() {
        Flight saved = flightRepository
                .findByFlightNumber("AI101");

        ResponseEntity<String> response = restTemplate
                .getForEntity(
                        baseUrl + "/" + saved.getId(),
                        String.class);

        assertEquals(HttpStatus.OK,
                response.getStatusCode());
        assertTrue(response.getBody().contains("AI101"));
        assertTrue(response.getBody().contains("Air India"));
    }

    // Get Flight By ID - Not Found
    @Test
    void testGetFlightById_NotFound_Integration() {
        ResponseEntity<String> response = restTemplate
                .getForEntity(
                        baseUrl + "/9999",
                        String.class);

        assertEquals(HttpStatus.NOT_FOUND,
                response.getStatusCode());
    }

    //  Search Flights - Case Insensitive
    @Test
    void testSearchFlights_CaseInsensitive_Integration() {
        ResponseEntity<String> response = restTemplate
                .getForEntity(
                        baseUrl + "/search?origin=delhi&destination=mumbai",
                        String.class);

        assertEquals(HttpStatus.OK,
                response.getStatusCode());
        assertTrue(response.getBody().contains("AI101"));
    }
}