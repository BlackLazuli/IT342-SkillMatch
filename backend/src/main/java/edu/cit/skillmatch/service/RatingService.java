package edu.cit.skillmatch.service;

import edu.cit.skillmatch.entity.RatingEntity;
import edu.cit.skillmatch.entity.UserEntity;
import edu.cit.skillmatch.repository.RatingRepository;
import edu.cit.skillmatch.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RatingService {
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
    }

    public List<RatingEntity> getRatingsForUser(Long userId) {
        return ratingRepository.findByUserId(userId);
    }

    public Optional<RatingEntity> getRatingById(Long id) {
        return ratingRepository.findById(id);
    }

    public RatingEntity addRating(Long userId, int rating, String review) {
        Optional<UserEntity> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        RatingEntity ratingEntity = new RatingEntity();
        ratingEntity.setUser(userOptional.get());
        ratingEntity.setRating(rating);
        ratingEntity.setReview(review);

        return ratingRepository.save(ratingEntity);
    }

    public void deleteRating(Long id) {
        ratingRepository.deleteById(id);
    }
}
