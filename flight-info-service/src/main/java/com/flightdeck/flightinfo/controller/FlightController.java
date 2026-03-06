package com.flightdeck.flightinfo.controller;

import com.flightdeck.flightinfo.model.Flight;
import com.flightdeck.flightinfo.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    @Autowired
    private FlightService flightService;

    // FLDECK-4 : Search flights by origin & destination
    // TEST API GET http://localhost:8081/api/flights/search?origin=Delhi&destination=Mumbai
    @GetMapping("/search")
    public ResponseEntity<?> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination) {
        try {
            List<Flight> flights = flightService.searchFlights(origin, destination);
            if (flights.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No flights found from " + origin + " to " + destination);
            }
            return ResponseEntity.ok(flights);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching flights: " + e.getMessage());
        }
    }

    // FLDECK-5 : Get flight details by ID
    // TEST API GET http://localhost:8081/api/flights/1
    @GetMapping("/{id}")
    public ResponseEntity<?> getFlightById(@PathVariable Long id) {
        try {
            Flight flight = flightService.getFlightById(id);
            return ResponseEntity.ok(flight);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // FLDECK-6 GET all flights
    // TEST API GET http://localhost:8081/api/flights
    @GetMapping
    public ResponseEntity<List<Flight>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlights());
    }
}
