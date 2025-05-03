package edu.cit.skillmatch.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "portfolios")
public class PortfolioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonManagedReference("portfolio-user")
    private UserEntity user;

    @Column
    private String workExperience;

    @Column
    private List<String> daysAvailable; // Comma-separated list like "Monday,Tuesday,Friday"

    // ✅ Change time to startTime and endTime as LocalTime
    @Column
    private LocalTime startTime; // Start time of service

    @Column
    private LocalTime endTime; // End time of service

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    private List<ServiceEntity> servicesOffered;

    @Column
    private String clientTestimonials;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("portfolio-appointments")
    private List<AppointmentEntity> appointments;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getWorkExperience() {
        return workExperience;
    }

    public void setWorkExperience(String workExperience) {
        this.workExperience = workExperience;
    }

    public List<String> getDaysAvailable() {
        return daysAvailable;
    }

    public void setDaysAvailable(List<String> daysAvailable) {
        this.daysAvailable = daysAvailable;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public List<ServiceEntity> getServicesOffered() {
        return servicesOffered;
    }

    public void setServicesOffered(List<ServiceEntity> servicesOffered) {
        this.servicesOffered = servicesOffered;
    }

    public String getClientTestimonials() {
        return clientTestimonials;
    }

    public void setClientTestimonials(String clientTestimonials) {
        this.clientTestimonials = clientTestimonials;
    }

    public List<CommentEntity> getComments() {
        return comments;
    }

    public void setComments(List<CommentEntity> comments) {
        this.comments = comments;
    }

    public List<AppointmentEntity> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<AppointmentEntity> appointments) {
        this.appointments = appointments;
    }
}
