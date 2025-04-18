package edu.cit.skillmatch.entity;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "portfolios")
public class PortfolioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
     @JsonBackReference
    private UserEntity user;

    @Column
    private String workExperience;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    private List<ServiceEntity> servicesOffered;
    
    @Column
    private String clientTestimonials;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments; // Comments related to the portfolio

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
}
