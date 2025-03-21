package edu.cit.skillmatch.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // "CUSTOMER" or "SERVICE_PROVIDER"

    @Column(length = 500)
    private String bio;

    @Column(unique = true)
    private String phoneNumber;

    @Column
    private Double rating; // Average rating for service providers

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private LocationEntity location; // One-to-One relationship with LocationEntity

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private PortfolioEntity portfolio; // One-to-One relationship with PortfolioEntity

    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RatingEntity> receivedRatings; // Ratings received by this user

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RatingEntity> givenRatings; // Ratings given by this user

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppointmentEntity> bookedAppointments; // Appointments where this user is a customer

    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppointmentEntity> receivedAppointments; // Appointments where this user is a service provider

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments; // Comments written by the user

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public void setLocation(LocationEntity location) {
        this.location = location;
    }

    public PortfolioEntity getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(PortfolioEntity portfolio) {
        this.portfolio = portfolio;
    }

    public List<RatingEntity> getReceivedRatings() {
        return receivedRatings;
    }

    public void setReceivedRatings(List<RatingEntity> receivedRatings) {
        this.receivedRatings = receivedRatings;
    }

    public List<RatingEntity> getGivenRatings() {
        return givenRatings;
    }

    public void setGivenRatings(List<RatingEntity> givenRatings) {
        this.givenRatings = givenRatings;
    }

    public List<AppointmentEntity> getBookedAppointments() {
        return bookedAppointments;
    }

    public void setBookedAppointments(List<AppointmentEntity> bookedAppointments) {
        this.bookedAppointments = bookedAppointments;
    }

    public List<AppointmentEntity> getReceivedAppointments() {
        return receivedAppointments;
    }

    public void setReceivedAppointments(List<AppointmentEntity> receivedAppointments) {
        this.receivedAppointments = receivedAppointments;
    }

    public List<CommentEntity> getComments() {
        return comments;
    }

    public void setComments(List<CommentEntity> comments) {
        this.comments = comments;
    }
}
