package edu.cit.skillmatch.service;

import edu.cit.skillmatch.entity.CommentEntity;
import edu.cit.skillmatch.entity.PortfolioEntity;
import edu.cit.skillmatch.entity.UserEntity;
import edu.cit.skillmatch.repository.CommentRepository;
import edu.cit.skillmatch.repository.PortfolioRepository;
import edu.cit.skillmatch.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, PortfolioRepository portfolioRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public List<CommentEntity> getCommentsByPortfolio(Long portfolioId) {
        return commentRepository.findByPortfolioId(portfolioId);
    }

    public CommentEntity addComment(Long userId, Long portfolioId, String message, Integer rating) {
        // Validate rating to be between 1 and 5
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        Optional<UserEntity> user = userRepository.findById(userId);
        Optional<PortfolioEntity> portfolio = portfolioRepository.findById(portfolioId);

        if (user.isPresent() && portfolio.isPresent()) {
            CommentEntity comment = new CommentEntity();
            comment.setAuthor(user.get());
            comment.setPortfolio(portfolio.get());
            comment.setMessage(message);
            comment.setRating(rating); // Set the rating
            return commentRepository.save(comment);
        } else {
            throw new RuntimeException("User or Portfolio not found");
        }
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
