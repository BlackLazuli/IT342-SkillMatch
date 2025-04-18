package edu.cit.skillmatch.service;

import edu.cit.skillmatch.repository.PortfolioRepository;
import edu.cit.skillmatch.entity.PortfolioEntity;
import edu.cit.skillmatch.entity.ServiceEntity;
import edu.cit.skillmatch.entity.UserEntity;
import edu.cit.skillmatch.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    public PortfolioService(PortfolioRepository portfolioRepository, UserRepository userRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
    }

    public Optional<PortfolioEntity> getPortfolioByUserId(Long userId) {
        return portfolioRepository.findByUserId(userId);
    }

public PortfolioEntity createOrUpdatePortfolio(Long userId, PortfolioEntity portfolio) {
    Optional<UserEntity> userOpt = userRepository.findById(userId);
    if (userOpt.isPresent()) {
        portfolio.setUser(userOpt.get());

        if (portfolio.getServicesOffered() != null) {
            for (ServiceEntity service : portfolio.getServicesOffered()) {
                service.setPortfolio(portfolio); // set the back-reference
            }
        }

        return portfolioRepository.save(portfolio);
    }
    throw new RuntimeException("User not found");
}
public PortfolioEntity updatePortfolio(Long portfolioId, PortfolioEntity portfolio) {
    Optional<PortfolioEntity> existingPortfolio = portfolioRepository.findById(portfolioId);
    if (existingPortfolio.isPresent()) {
        PortfolioEntity updatedPortfolio = existingPortfolio.get();
        updatedPortfolio.setWorkExperience(portfolio.getWorkExperience());

        // Update existing services or add new services
        for (ServiceEntity newService : portfolio.getServicesOffered()) {
            boolean exists = false;
            for (ServiceEntity existingService : updatedPortfolio.getServicesOffered()) {
                if (existingService.getId().equals(newService.getId())) {
                    // Update existing service
                    existingService.setName(newService.getName());
                    existingService.setDescription(newService.getDescription());
                    existingService.setPricing(newService.getPricing());
                    existingService.setDaysOfTheWeek(newService.getDaysOfTheWeek());
                    existingService.setTime(newService.getTime());
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                // Add new service
                newService.setPortfolio(updatedPortfolio);
                updatedPortfolio.getServicesOffered().add(newService);
            }
        }

        return portfolioRepository.save(updatedPortfolio);
    }
    throw new RuntimeException("Portfolio not found for portfolioId: " + portfolioId);
}



    public void deletePortfolio(Long id) {
        portfolioRepository.deleteById(id);
    }
}
