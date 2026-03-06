package com.flightdeck.flightinfo;

import com.flightdeck.flightinfo.model.Flight;
import com.flightdeck.flightinfo.repository.BookingRepository;
import com.flightdeck.flightinfo.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookingControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private String baseUrl;
    private Flight savedFlight;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/flights";

        bookingRepository.deleteAll();
        flightRepository.deleteAll();

        Flight flight = new Flight(
                "AI101", "Air India", "Delhi", "Mumbai",
                LocalDateTime.of(2025, 6, 1, 8, 0),
                LocalDateTime.of(2025, 6, 1, 10, 0),
                2500.00, 120
        );
        savedFlight = flightRepository.save(flight);
    }

    // Integration Test 1: Book Flight - Success
    @Test
    void testBookFlight_Success_Integration() {
        Map<String, String> request = new HashMap<>();
        request.put("passengerName", "Gowtham M");
        request.put("passengerEmail", "gowtham@email.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate
                .postForEntity(
                        baseUrl + "/" + savedFlight.getId() + "/book",
                        entity, String.class);

        assertEquals(HttpStatus.CREATED,
                response.getStatusCode());
        assertTrue(response.getBody()
                .contains("Booking confirmed!"));
        assertTrue(response.getBody()
                .contains("Gowtham M"));
        assertTrue(response.getBody()
                .contains("AI101"));
    }

    // Integration Test 2: Book Flight - Missing Name
    @Test
    void testBookFlight_MissingName_Integration() {
        Map<String, String> request = new HashMap<>();
        request.put("passengerName", "");
        request.put("passengerEmail", "gowtham@email.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate
                .postForEntity(
                        baseUrl + "/" + savedFlight.getId() + "/book",
                        entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());
        assertTrue(response.getBody()
                .contains("Passenger name is required"));
    }

    // Integration Test 3: Book Flight - Flight Not Found
    @Test
    void testBookFlight_FlightNotFound_Integration() {
        Map<String, String> request = new HashMap<>();
        request.put("passengerName", "Gowtham M");
        request.put("passengerEmail", "gowtham@email.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate
                .postForEntity(
                        baseUrl + "/9999/book",
                        entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());
        assertTrue(response.getBody()
                .contains("Flight not found with ID: 9999"));
    }

    // Integration Test 4: Get Booking By ID - Success
    @Test
    void testGetBookingById_Success_Integration() {
        // First create a booking
        Map<String, String> bookRequest = new HashMap<>();
        bookRequest.put("passengerName", "Gowtham M");
        bookRequest.put("passengerEmail", "gowtham@email.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity =
                new HttpEntity<>(bookRequest, headers);

        restTemplate.postForEntity(
                baseUrl + "/" + savedFlight.getId() + "/book",
                entity, String.class);

        Long bookingId = bookingRepository.findAll()
                .get(0).getId();

        ResponseEntity<String> response = restTemplate
                .getForEntity(
                        baseUrl + "/bookings/" + bookingId,
                        String.class);

        assertEquals(HttpStatus.OK,
                response.getStatusCode());
        assertTrue(response.getBody()
                .contains("Gowtham M"));
        assertTrue(response.getBody()
                .contains("AI101"));
    }

    // Integration Test 5: Get Booking By ID - Not Found
    @Test
    void testGetBookingById_NotFound_Integration() {
        ResponseEntity<String> response = restTemplate
                .getForEntity(
                        baseUrl + "/bookings/9999",
                        String.class);

        assertEquals(HttpStatus.NOT_FOUND,
                response.getStatusCode());
        assertTrue(response.getBody()
                .contains("Booking not found with ID: 9999"));
    }

    // Integration Test 6: Update Booking - Success
    @Test
    void testUpdateBooking_Success_Integration() {
        // First create a booking
        Map<String, String> bookRequest = new HashMap<>();
        bookRequest.put("passengerName", "Gowtham M");
        bookRequest.put("passengerEmail", "gowtham@email.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(
                baseUrl + "/" + savedFlight.getId() + "/book",
                new HttpEntity<>(bookRequest, headers),
                String.class);

        Long bookingId = bookingRepository.findAll()
                .get(0).getId();

        Map<String, String> updateRequest = new HashMap<>();
        updateRequest.put("passengerName", "Gowtham Manikanta");
        updateRequest.put("passengerEmail",
                "gowtham.new@email.com");

        ResponseEntity<String> response = restTemplate
                .exchange(
                        baseUrl + "/bookings/" + bookingId,
                        HttpMethod.PUT,
                        new HttpEntity<>(updateRequest, headers),
                        String.class);

        assertEquals(HttpStatus.OK,
                response.getStatusCode());
        assertTrue(response.getBody()
                .contains("Booking updated successfully!"));
        assertTrue(response.getBody()
                .contains("Gowtham Manikanta"));
    }

    // Integration Test 7: Update Booking - Not Found
    @Test
    void testUpdateBooking_NotFound_Integration() {
        Map<String, String> request = new HashMap<>();
        request.put("passengerName", "Gowtham M");
        request.put("passengerEmail", "gowtham@email.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate
                .exchange(
                        baseUrl + "/bookings/9999",
                        HttpMethod.PUT,
                        new HttpEntity<>(request, headers),
                        String.class);

        assertEquals(HttpStatus.NOT_FOUND,
                response.getStatusCode());
        assertTrue(response.getBody()
                .contains("Booking not found with ID: 9999"));
    }
}