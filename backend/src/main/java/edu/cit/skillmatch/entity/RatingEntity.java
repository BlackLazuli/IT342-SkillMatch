package edu.cit.skillmatch.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "ratings")
public class RatingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference("rating-user")  // Unique name for this back-reference
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private int rating; // 1 to 5 stars

    @Column(length = 500)
    private String review; // Optional review text

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RatingEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
