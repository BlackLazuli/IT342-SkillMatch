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
    @JoinColumn(name = "customer_id", nullable = false)
    private UserEntity customer; // The user booking the appointment

    @ManyToOne
    @JoinColumn(name = "service_provider_id", nullable = false)
    private UserEntity serviceProvider; // The service provider

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
