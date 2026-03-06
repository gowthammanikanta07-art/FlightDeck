package com.flightdeck.flightinfo;

import com.flightdeck.flightinfo.model.Flight;
import com.flightdeck.flightinfo.repository.FlightRepository;
import com.flightdeck.flightinfo.service.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightService flightService;

    private Flight sampleFlight;

    @BeforeEach
    void setUp() {
        sampleFlight = new Flight(
                "AI101",
                "Air India",
                "Delhi",
                "Mumbai",
                LocalDateTime.of(2025, 6, 1, 8, 0),
                LocalDateTime.of(2025, 6, 1, 10, 0),
                2500.00,
                120
        );
        sampleFlight.setId(1L);
    }

    // FLDECK-4: Search Flights - Success
    @Test
    void testSearchFlights_Success() {
        when(flightRepository
                .findByOriginIgnoreCaseAndDestinationIgnoreCase("Delhi", "Mumbai"))
                .thenReturn(List.of(sampleFlight));

        List<Flight> result = flightService.searchFlights("Delhi", "Mumbai");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AI101", result.get(0).getFlightNumber());
        assertEquals("Delhi", result.get(0).getOrigin());
        assertEquals("Mumbai", result.get(0).getDestination());
        verify(flightRepository, times(1))
                .findByOriginIgnoreCaseAndDestinationIgnoreCase("Delhi", "Mumbai");
    }

    // FLDECK-4: Search Flights - No Results
    @Test
    void testSearchFlights_NoResults() {
        when(flightRepository
                .findByOriginIgnoreCaseAndDestinationIgnoreCase("Delhi", "London"))
                .thenReturn(Collections.emptyList());

        List<Flight> result = flightService.searchFlights("Delhi", "London");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // FLDECK-4: Search Flights - Case Insensitive
    @Test
    void testSearchFlights_CaseInsensitive() {
        when(flightRepository
                .findByOriginIgnoreCaseAndDestinationIgnoreCase("delhi", "mumbai"))
                .thenReturn(List.of(sampleFlight));

        List<Flight> result = flightService.searchFlights("delhi", "mumbai");

        assertFalse(result.isEmpty());
        assertEquals("AI101", result.get(0).getFlightNumber());
    }

    // FLDECK-5: Get Flight By ID - Found
    @Test
    void testGetFlightById_Found() {
        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(sampleFlight));

        Flight result = flightService.getFlightById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("AI101", result.getFlightNumber());
        assertEquals("Air India", result.getAirlineName());
        assertEquals(2500.00, result.getPrice());
        assertEquals(120, result.getAvailableSeats());
    }

    // FLDECK-5: Get Flight By ID - Not Found
    @Test
    void testGetFlightById_NotFound() {
        when(flightRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> flightService.getFlightById(99L)
        );

        assertEquals("Flight not found with ID: 99", exception.getMessage());
    }

    // FLDECK-6 Get All Flights
    @Test
    void testGetAllFlights() {
        when(flightRepository.findAll())
                .thenReturn(List.of(sampleFlight));

        List<Flight> result = flightService.getAllFlights();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // FLDECK-6 Get All Flights - Empty
    @Test
    void testGetAllFlights_Empty() {
        when(flightRepository.findAll())
                .thenReturn(Collections.emptyList());

        List<Flight> result = flightService.getAllFlights();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}