package com.flightdeck.flightinfo.service;

import com.flightdeck.flightinfo.model.Booking;
import com.flightdeck.flightinfo.model.Flight;
import com.flightdeck.flightinfo.repository.BookingRepository;
import com.flightdeck.flightinfo.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${coupon.service.url}")
    private String couponServiceUrl;

    // Story 5: Book a flight (with optional coupon)
    public Map<String, Object> bookFlight(
            Long flightId,
            String passengerName,
            String passengerEmail,
            String couponCode) {

        // Step 1: Check flight exists
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException(
                        "Flight not found with ID: " + flightId));

        // Step 2: Check seats available
        if (flight.getAvailableSeats() <= 0) {
            throw new RuntimeException(
                    "No seats available for flight: "
                    + flight.getFlightNumber());
        }

        // Step 3: Apply coupon if provided
        Double finalPrice = flight.getPrice();
        Double savings = 0.0;
        String appliedCoupon = null;

        if (couponCode != null && !couponCode.isEmpty()) {
            try {
                // Call Coupon Service on port 8082
                String url = couponServiceUrl
                        + "/api/coupons/apply";

                Map<String, Object> couponRequest = new HashMap<>();
                couponRequest.put("originalPrice", flight.getPrice());
                couponRequest.put("code", couponCode);

                ResponseEntity<Map> couponResponse =
                        restTemplate.postForEntity(
                                url,
                                couponRequest,
                                Map.class);

                if (couponResponse.getStatusCode() == HttpStatus.OK) {
                    Map body = couponResponse.getBody();
                    finalPrice = Double.valueOf(
                            body.get("discountedPrice").toString());
                    savings = Double.valueOf(
                            body.get("savings").toString());
                    appliedCoupon = couponCode;
                }
            } catch (Exception e) {
                // If coupon service is down, book at full price
                System.out.println(
                        "Coupon service unavailable: "
                        + e.getMessage());
            }
        }

        // Step 4: Generate booking reference FD-YYYYMMDD-001
        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long count = bookingRepository.countByFlightId(flightId) + 1;
        String bookingReference = String.format(
                "FD-%s-%03d", date, count);

        // Step 5: Save booking
        Booking booking = new Booking(
                passengerName, passengerEmail,
                flightId, bookingReference);
        bookingRepository.save(booking);

        // Step 6: Reduce available seats by 1
        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        flightRepository.save(flight);

        // Step 7: Build response
        Map<String, Object> response = new HashMap<>();
        response.put("bookingId",        booking.getId());
        response.put("bookingReference", bookingReference);
        response.put("passengerName",    passengerName);
        response.put("passengerEmail",   passengerEmail);
        response.put("flightNumber",     flight.getFlightNumber());
        response.put("airlineName",      flight.getAirlineName());
        response.put("origin",           flight.getOrigin());
        response.put("destination",      flight.getDestination());
        response.put("departureTime",    flight.getDepartureTime());
        response.put("originalPrice",    flight.getPrice());
        response.put("finalPrice",       finalPrice);
        response.put("savings",          savings);
        response.put("couponApplied",    appliedCoupon);
        response.put("message",          "Booking confirmed!");

        return response;
    }

    // Story 6: Get booking by ID
    public Map<String, Object> getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException(
                        "Booking not found with ID: " + bookingId));

        Flight flight = flightRepository
                .findById(booking.getFlightId())
                .orElseThrow(() -> new RuntimeException(
                        "Flight not found for booking"));

        Map<String, Object> response = new HashMap<>();
        response.put("bookingId",        booking.getId());
        response.put("bookingReference", booking.getBookingReference());
        response.put("passengerName",    booking.getPassengerName());
        response.put("passengerEmail",   booking.getPassengerEmail());
        response.put("flightNumber",     flight.getFlightNumber());
        response.put("airlineName",      flight.getAirlineName());
        response.put("origin",           flight.getOrigin());
        response.put("destination",      flight.getDestination());
        response.put("departureTime",    flight.getDepartureTime());
        response.put("price",            flight.getPrice());
        return response;
    }

    // Story 7: Update passenger details
    public Map<String, Object> updateBooking(
            Long bookingId,
            String passengerName,
            String passengerEmail) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException(
                        "Booking not found with ID: " + bookingId));

        booking.setPassengerName(passengerName);
        booking.setPassengerEmail(passengerEmail);
        bookingRepository.save(booking);

        Map<String, Object> response = new HashMap<>();
        response.put("bookingId",        booking.getId());
        response.put("bookingReference", booking.getBookingReference());
        response.put("passengerName",    booking.getPassengerName());
        response.put("passengerEmail",   booking.getPassengerEmail());
        response.put("message",          "Booking updated successfully!");
        return response;
    }
}