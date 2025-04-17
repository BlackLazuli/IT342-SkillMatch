package edu.cit.skillmatch.controller;

import edu.cit.skillmatch.entity.LocationEntity;
import edu.cit.skillmatch.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = {"http://localhost:5173", "http://10.0.2.2:8080"}) // Allow React frontend and Android emulator
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // Get a user's location
    @GetMapping("/{userId}")
    public ResponseEntity<LocationEntity> getLocationByUserId(@PathVariable Long userId) {
        Optional<LocationEntity> location = locationService.getLocationByUserId(userId);
        return location.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Add or update a user's location
    @PostMapping("/{userId}")
    public ResponseEntity<LocationEntity> saveOrUpdateLocation(@PathVariable Long userId,
                                                               @RequestBody LocationEntity location) {
        try {
            LocationEntity savedLocation = locationService.saveOrUpdateLocation(
                    userId, location.getLatitude(), location.getLongitude(), location.getAddress());
            return ResponseEntity.ok(savedLocation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Add a PUT endpoint for updating location
    @PutMapping("/{userId}")
    public ResponseEntity<LocationEntity> updateLocation(@PathVariable Long userId,
                                                        @RequestBody LocationEntity location) {
        try {
            LocationEntity savedLocation = locationService.saveOrUpdateLocation(
                    userId, location.getLatitude(), location.getLongitude(), location.getAddress());
            return ResponseEntity.ok(savedLocation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete a user's location
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long userId) {
        locationService.deleteLocation(userId);
        return ResponseEntity.noContent().build();
    }
}
