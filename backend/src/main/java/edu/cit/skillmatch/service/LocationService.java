package edu.cit.skillmatch.service;

import edu.cit.skillmatch.entity.LocationEntity;
import edu.cit.skillmatch.entity.UserEntity;
import edu.cit.skillmatch.repository.LocationRepository;
import edu.cit.skillmatch.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public LocationService(LocationRepository locationRepository, UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    public Optional<LocationEntity> getLocationByUserId(Long userId) {
        return locationRepository.findByUserId(userId);
    }

    public LocationEntity saveOrUpdateLocation(Long userId, double latitude, double longitude, String address) {
        Optional<UserEntity> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        UserEntity user = userOptional.get();
        LocationEntity location = locationRepository.findByUserId(userId)
                .orElse(new LocationEntity());

        location.setUser(user);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAddress(address);

        return locationRepository.save(location);
    }

    public void deleteLocation(Long userId) {
        locationRepository.findByUserId(userId).ifPresent(locationRepository::delete);
    }
}
