package com.flightdeck.flightinfo.repository;

import com.flightdeck.flightinfo.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find booking by reference
    Optional<Booking> findByBookingReference(String bookingReference);

    // Count bookings per flight (to generate reference)
    Long countByFlightId(Long flightId);
}