package edu.cit.skillmatch.controller;

import edu.cit.skillmatch.entity.PortfolioEntity;
import edu.cit.skillmatch.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://10.0.2.2:8080"})
@RequestMapping("/api/portfolios")
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    // Get Portfolio by User ID
    @GetMapping("/{userId}")
    public ResponseEntity<PortfolioEntity> getPortfolioByUserId(@PathVariable Long userId) {
        Optional<PortfolioEntity> portfolio = portfolioService.getPortfolioByUserId(userId);
        return portfolio.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create or Update Portfolio (POST)
    @PostMapping("/{userId}")
    public ResponseEntity<PortfolioEntity> createOrUpdatePortfolio(@PathVariable Long userId,
                                                                   @RequestBody PortfolioEntity portfolio) {
        try {
            System.out.println("Creating/updating portfolio for userId: " + userId); // Debugging log
            PortfolioEntity savedPortfolio = portfolioService.createOrUpdatePortfolio(userId, portfolio);
            return ResponseEntity.ok(savedPortfolio);
        } catch (RuntimeException e) {
            System.err.println("Error in POST portfolio: " + e.getMessage()); // Debugging log
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Update Portfolio (PUT) - Updated to use userId instead of portfolioId
    @PutMapping("/{userId}")
    public ResponseEntity<PortfolioEntity> updatePortfolio(@PathVariable Long userId,
                                                           @RequestBody PortfolioEntity portfolio) {
        try {
            System.out.println("Updating portfolio for userId: " + userId); // Debugging log
            PortfolioEntity updatedPortfolio = portfolioService.updatePortfolio(userId, portfolio);
            return ResponseEntity.ok(updatedPortfolio);
        } catch (RuntimeException e) {
            System.err.println("Error in PUT portfolio: " + e.getMessage()); // Debugging log
            return ResponseEntity.badRequest().build(); // Return 400 instead of 404 for better error handling
        }
    }
    
    // Delete Portfolio
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long portfolioId) {
        portfolioService.deletePortfolio(portfolioId);
        return ResponseEntity.noContent().build();
    }
}
