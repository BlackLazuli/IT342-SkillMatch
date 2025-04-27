package edu.cit.skillmatch.repository;

import edu.cit.skillmatch.entity.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<PortfolioEntity, Long> {

    Optional<PortfolioEntity> findByUserId(Long userId);

    List<PortfolioEntity> findAllByUserId(Long userId); // returns list

    @Query("SELECT p FROM PortfolioEntity p JOIN FETCH p.user WHERE p.id = :portfolioId")
    Optional<PortfolioEntity> findByIdWithUser(Long portfolioId);
}
