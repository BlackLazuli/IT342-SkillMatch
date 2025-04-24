package edu.cit.skillmatch.repository;

import edu.cit.skillmatch.entity.AppointmentEntity;
import edu.cit.skillmatch.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findByUserIdAndRole(Long userId, String role);
    List<AppointmentEntity> findByUserIdAndStatus(Long userId, AppointmentStatus status);
    List<AppointmentEntity> findByUserIdAndAppointmentTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<AppointmentEntity> findByPortfolioId(Long portfolioId);
    List<AppointmentEntity> findByPortfolioIdAndStatus(Long portfolioId, AppointmentStatus status);

}

