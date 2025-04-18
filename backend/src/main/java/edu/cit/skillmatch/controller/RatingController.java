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

    // Get ratings for a user (customer or service provider)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RatingEntity>> getRatingsForUser(@PathVariable Long userId) {
        List<RatingEntity> ratings = ratingService.getRatingsForUser(userId);
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
    public ResponseEntity<RatingEntity> addRating(@RequestBody RatingEntity ratingEntity) {
        try {
            RatingEntity savedRating = ratingService.addRating(
                    ratingEntity.getUser().getId(), 
                    ratingEntity.getRating(), 
                    ratingEntity.getReview()
            );
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
