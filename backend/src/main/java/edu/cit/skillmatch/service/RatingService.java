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

    public List<RatingEntity> getRatingsByServiceProvider(Long serviceProviderId) {
        return ratingRepository.findByServiceProviderId(serviceProviderId);
    }

    public Optional<RatingEntity> getRatingById(Long id) {
        return ratingRepository.findById(id);
    }

    public RatingEntity addRating(Long customerId, Long serviceProviderId, int rating, String review) {
        Optional<UserEntity> customerOptional = userRepository.findById(customerId);
        Optional<UserEntity> serviceProviderOptional = userRepository.findById(serviceProviderId);

        if (customerOptional.isEmpty() || serviceProviderOptional.isEmpty()) {
            throw new RuntimeException("Customer or Service Provider not found");
        }

        RatingEntity ratingEntity = new RatingEntity();
        ratingEntity.setCustomer(customerOptional.get());
        ratingEntity.setServiceProvider(serviceProviderOptional.get());
        ratingEntity.setRating(rating);
        ratingEntity.setReview(review);

        return ratingRepository.save(ratingEntity);
    }

    public void deleteRating(Long id) {
        ratingRepository.deleteById(id);
    }
}
