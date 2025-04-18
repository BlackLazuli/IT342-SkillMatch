package edu.cit.skillmatch.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "services")
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Service name
    private String description; // Service description
    private String pricing; // Service pricing
    private String time; // Time of the service

    @ElementCollection
    @CollectionTable(name = "service_days", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "day")
    private List<String> daysOfTheWeek = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    @JsonIgnore // Prevent cyclic reference
    private PortfolioEntity portfolio; // Back-reference to PortfolioEntity

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPricing() {
        return pricing;
    }

    public void setPricing(String pricing) {
        this.pricing = pricing;
    }

    public List<String> getDaysOfTheWeek() {
        return daysOfTheWeek;
    }
    
    public void setDaysOfTheWeek(List<String> daysOfTheWeek) {
        this.daysOfTheWeek = daysOfTheWeek;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public PortfolioEntity getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(PortfolioEntity portfolio) {
        this.portfolio = portfolio;
    }
}
