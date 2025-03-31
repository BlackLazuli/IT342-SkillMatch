package edu.cit.skillmatch.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "portfolios")
public class PortfolioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column
    private String workExperience;

    @Column
    private String servicesOffered;

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

    public String getServicesOffered() {
        return servicesOffered;
    }

    public void setServicesOffered(String servicesOffered) {
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
