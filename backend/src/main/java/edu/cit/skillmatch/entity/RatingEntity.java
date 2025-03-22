package edu.cit.skillmatch.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
public class RatingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private UserEntity customer; // The user giving the rating

    @ManyToOne
    @JoinColumn(name = "service_provider_id", nullable = false)
    private UserEntity serviceProvider; // The user receiving the rating

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

    public UserEntity getCustomer() {
        return customer;
    }

    public void setCustomer(UserEntity customer) {
        this.customer = customer;
    }

    public UserEntity getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(UserEntity serviceProvider) {
        this.serviceProvider = serviceProvider;
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
