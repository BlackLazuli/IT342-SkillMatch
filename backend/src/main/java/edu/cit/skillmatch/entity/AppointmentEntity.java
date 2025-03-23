package edu.cit.skillmatch.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class AppointmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // The user booking or providing the appointment

    @Column(nullable = false)
    private String role; // "CUSTOMER" or "SERVICE_PROVIDER"

    @Column(nullable = false)
    private LocalDateTime appointmentTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status; // Status of the appointment

    @Column(length = 500)
    private String notes; // Optional notes for the appointment

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AppointmentEntity() {
        this.createdAt = LocalDateTime.now();
        this.status = AppointmentStatus.SCHEDULED; // Default status
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
