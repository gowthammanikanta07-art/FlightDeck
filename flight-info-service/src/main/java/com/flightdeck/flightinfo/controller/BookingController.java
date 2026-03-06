package com.flightdeck.flightinfo.controller;

import com.flightdeck.flightinfo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/flights")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Story 4: Book a flight (with optional coupon code)
    // POST http://localhost:8081/api/flights/1/book
    // Body: { "passengerName": "Gowtham", "passengerEmail": "g@email.com", "couponCode": "SAVE10" }
    @PostMapping("/{id}/book")
    public ResponseEntity<?> bookFlight(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String passengerName  = request.get("passengerName");
            String passengerEmail = request.get("passengerEmail");
            String couponCode     = request.get("couponCode"); // optional

            // Validate required fields
            if (passengerName == null || passengerName.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Passenger name is required");
            }
            if (passengerEmail == null || passengerEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Passenger email is required");
            }

            Map<String, Object> response = bookingService
                    .bookFlight(id, passengerName,
                            passengerEmail, couponCode);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // Story 5: Get booking by ID
    // GET http://localhost:8081/api/flights/bookings/1
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<?> getBookingById(
            @PathVariable Long bookingId) {
        try {
            Map<String, Object> response = bookingService
                    .getBookingById(bookingId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // Story 6: Update passenger details
    // PUT http://localhost:8081/api/flights/bookings/1
    // Body: { "passengerName": "Gowtham M", "passengerEmail": "new@email.com" }
    @PutMapping("/bookings/{bookingId}")
    public ResponseEntity<?> updateBooking(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> request) {
        try {
            String passengerName  = request.get("passengerName");
            String passengerEmail = request.get("passengerEmail");

            if (passengerName == null || passengerName.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Passenger name is required");
            }
            if (passengerEmail == null || passengerEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Passenger email is required");
            }

            Map<String, Object> response = bookingService
                    .updateBooking(bookingId,
                            passengerName, passengerEmail);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}