package edu.cit.skillmatch.service;

import edu.cit.skillmatch.repository.PortfolioRepository;
import edu.cit.skillmatch.entity.PortfolioEntity;
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
            return portfolioRepository.save(portfolio);
        }
        throw new RuntimeException("User not found");
    }

    public void deletePortfolio(Long id) {
        portfolioRepository.deleteById(id);
    }
}
