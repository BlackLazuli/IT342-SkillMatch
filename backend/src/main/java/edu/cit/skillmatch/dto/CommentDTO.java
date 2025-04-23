package edu.cit.skillmatch.dto;

import java.time.LocalDateTime;

public class CommentDTO {

    private Long id;
    private String message;
    private LocalDateTime timestamp;
    private int rating;
    private String authorName;
    private String profilePicture;

    // Constructor
    public CommentDTO(Long id, String message, LocalDateTime timestamp, int rating, String authorName, String profilePicture) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        this.rating = rating;
        this.authorName = authorName;
        this.profilePicture = profilePicture;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
