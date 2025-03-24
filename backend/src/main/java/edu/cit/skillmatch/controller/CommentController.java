package edu.cit.skillmatch.controller;

import edu.cit.skillmatch.entity.CommentEntity;
import edu.cit.skillmatch.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // Get all comments for a specific portfolio
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<CommentEntity>> getCommentsByPortfolio(@PathVariable Long portfolioId) {
        List<CommentEntity> comments = commentService.getCommentsByPortfolio(portfolioId);
        return ResponseEntity.ok(comments);
    }

    // Add a new comment
    @PostMapping("/{userId}/{portfolioId}")
    public ResponseEntity<CommentEntity> addComment(@PathVariable Long userId,
                                                    @PathVariable Long portfolioId,
                                                    @RequestBody CommentEntity comment) {
        try {
            CommentEntity savedComment = commentService.addComment(userId, portfolioId, comment.getMessage());
            return ResponseEntity.ok(savedComment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete a comment
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
