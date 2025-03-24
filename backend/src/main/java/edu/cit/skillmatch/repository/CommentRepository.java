package edu.cit.skillmatch.repository;

import edu.cit.skillmatch.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByPortfolioId(Long portfolioId);
}
