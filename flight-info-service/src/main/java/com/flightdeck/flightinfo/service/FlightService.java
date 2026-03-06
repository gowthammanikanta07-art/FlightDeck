	package com.flightdeck.flightinfo.service;

import com.flightdeck.flightinfo.model.Flight;
import com.flightdeck.flightinfo.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    // FLDECK-4: Search flights by origin and destination
    public List<Flight> searchFlights(String origin, String destination) {
        return flightRepository
                .findByOriginIgnoreCaseAndDestinationIgnoreCase(origin, destination);
    }

    // FLDECK-5: Get flight details by ID
    public Flight getFlightById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Flight not found with ID: " + id));
    }

    //FLDECK-6 Get all flights
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }
}
