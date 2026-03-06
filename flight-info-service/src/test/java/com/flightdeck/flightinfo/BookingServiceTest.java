package com.flightdeck.flightinfo;

import com.flightdeck.flightinfo.model.Booking;
import com.flightdeck.flightinfo.model.Flight;
import com.flightdeck.flightinfo.repository.BookingRepository;
import com.flightdeck.flightinfo.repository.FlightRepository;
import com.flightdeck.flightinfo.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.http.ResponseEntity;
import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BookingService bookingService;

    private Flight sampleFlight;
    private Booking sampleBooking;

    @BeforeEach
    void setUp() {
        sampleFlight = new Flight(
                "AI101", "Air India", "Delhi", "Mumbai",
                LocalDateTime.of(2025, 6, 1, 8, 0),
                LocalDateTime.of(2025, 6, 1, 10, 0),
                2500.00, 120
        );
        sampleFlight.setId(1L);

        sampleBooking = new Booking(
                "Gowtham M",
                "gowtham@email.com",
                1L,
                "FD-20250601-001"
        );
        sampleBooking.setId(1L);
    }

 // Story 7: Book Flight - With Valid Coupon
    @Test
    void testBookFlight_Success_WithCoupon() {
        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(sampleFlight));
        when(bookingRepository.countByFlightId(1L))
                .thenReturn(0L);
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(sampleBooking);
        when(flightRepository.save(any(Flight.class)))
                .thenReturn(sampleFlight);

        // Mock coupon service response
        Map<String, Object> couponResponse = new HashMap<>();
        couponResponse.put("discountedPrice", 2250.0);
        couponResponse.put("savings", 250.0);

        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(Map.class)))
                .thenReturn(ResponseEntity.ok(couponResponse));

        Map<String, Object> result = bookingService
                .bookFlight(1L, "Gowtham M",
                        "gowtham@email.com", "SAVE10");

        assertNotNull(result);
        assertEquals("Booking confirmed!",
                result.get("message"));
        assertEquals(2250.0, result.get("finalPrice"));
        assertEquals(250.0, result.get("savings"));
        assertEquals("SAVE10", result.get("couponApplied"));
    }

    // Book Flight - Coupon Service Down (Graceful Fallback)
    @Test
    void testBookFlight_CouponServiceDown_FallsBackToFullPrice() {
        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(sampleFlight));
        when(bookingRepository.countByFlightId(1L))
                .thenReturn(0L);
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(sampleBooking);
        when(flightRepository.save(any(Flight.class)))
                .thenReturn(sampleFlight);

        // Simulate coupon service being down
        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(Map.class)))
                .thenThrow(new RuntimeException(
                        "Coupon service unavailable"));

        Map<String, Object> result = bookingService
                .bookFlight(1L, "Gowtham M",
                        "gowtham@email.com", "SAVE10");

        assertNotNull(result);
        assertEquals("Booking confirmed!",
                result.get("message"));
        // Falls back to full price
        assertEquals(2500.0, result.get("finalPrice"));
        assertEquals(0.0, result.get("savings"));
    }
    
    // Story 4: Book Flight - Success (no coupon)
    @Test
    void testBookFlight_Success_NoCoupon() {
        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(sampleFlight));
        when(bookingRepository.countByFlightId(1L))
                .thenReturn(0L);
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(sampleBooking);
        when(flightRepository.save(any(Flight.class)))
                .thenReturn(sampleFlight);

        Map<String, Object> result = bookingService
                .bookFlight(1L, "Gowtham M",
                        "gowtham@email.com", null);

        assertNotNull(result);
        assertEquals("Booking confirmed!",
                result.get("message"));
        assertEquals("AI101", result.get("flightNumber"));
        assertEquals(2500.00, result.get("originalPrice"));
        assertEquals(0.0, result.get("savings"));
        verify(flightRepository, times(1))
                .save(any(Flight.class));
    }

    // Story 4: Book Flight - Flight Not Found
    @Test
    void testBookFlight_FlightNotFound() {
        when(flightRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bookingService.bookFlight(
                        99L, "Gowtham M",
                        "gowtham@email.com", null)
        );
        assertEquals("Flight not found with ID: 99",
                exception.getMessage());
    }

    // Story 4: Book Flight - No Seats Available
    @Test
    void testBookFlight_NoSeatsAvailable() {
        sampleFlight.setAvailableSeats(0);

        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(sampleFlight));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bookingService.bookFlight(
                        1L, "Gowtham M",
                        "gowtham@email.com", null)
        );
        assertTrue(exception.getMessage()
                .contains("No seats available"));
    }

    // Story 5: Get Booking By ID - Success
    @Test
    void testGetBookingById_Success() {
        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(sampleBooking));
        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(sampleFlight));

        Map<String, Object> result = bookingService
                .getBookingById(1L);

        assertNotNull(result);
        assertEquals("FD-20250601-001",
                result.get("bookingReference"));
        assertEquals("AI101",
                result.get("flightNumber"));
        assertEquals("Gowtham M",
                result.get("passengerName"));
    }

    // Story 5: Get Booking By ID - Not Found
    @Test
    void testGetBookingById_NotFound() {
        when(bookingRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bookingService.getBookingById(99L)
        );
        assertEquals("Booking not found with ID: 99",
                exception.getMessage());
    }

    // Story 6: Update Booking - Success
    @Test
    void testUpdateBooking_Success() {
        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(sampleBooking);

        Map<String, Object> result = bookingService
                .updateBooking(1L,
                        "Gowtham Manikanta",
                        "gowtham.new@email.com");

        assertNotNull(result);
        assertEquals("Booking updated successfully!",
                result.get("message"));
        verify(bookingRepository, times(1))
                .save(any(Booking.class));
    }

    // Story 6: Update Booking - Not Found
    @Test
    void testUpdateBooking_NotFound() {
        when(bookingRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bookingService.updateBooking(
                        99L, "Gowtham M",
                        "gowtham@email.com")
        );
        assertEquals("Booking not found with ID: 99",
                exception.getMessage());
    }
}