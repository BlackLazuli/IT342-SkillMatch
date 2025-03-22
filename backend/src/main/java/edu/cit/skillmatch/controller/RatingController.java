package edu.cit.skillmatch.controller;

import edu.cit.skillmatch.entity.RatingEntity;
import edu.cit.skillmatch.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {
    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    // Get all ratings for a service provider
    @GetMapping("/service-provider/{serviceProviderId}")
    public ResponseEntity<List<RatingEntity>> getRatingsByServiceProvider(@PathVariable Long serviceProviderId) {
        List<RatingEntity> ratings = ratingService.getRatingsByServiceProvider(serviceProviderId);
        return ResponseEntity.ok(ratings);
    }

    // Get a specific rating by ID
    @GetMapping("/{id}")
    public ResponseEntity<RatingEntity> getRatingById(@PathVariable Long id) {
        Optional<RatingEntity> rating = ratingService.getRatingById(id);
        return rating.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Add a new rating
    @PostMapping("/")
    public ResponseEntity<RatingEntity> addRating(@RequestParam Long customerId,
                                                  @RequestParam Long serviceProviderId,
                                                  @RequestParam int rating,
                                                  @RequestParam(required = false) String review) {
        try {
            RatingEntity savedRating = ratingService.addRating(customerId, serviceProviderId, rating, review);
            return ResponseEntity.ok(savedRating);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete a rating
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        ratingService.deleteRating(id);
        return ResponseEntity.noContent().build();
    }
}
