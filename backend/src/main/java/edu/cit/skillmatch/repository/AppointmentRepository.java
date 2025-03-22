package edu.cit.skillmatch.repository;

import edu.cit.skillmatch.entity.AppointmentEntity;
import edu.cit.skillmatch.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findByServiceProviderId(Long serviceProviderId);
    List<AppointmentEntity> findByCustomerId(Long customerId);
    List<AppointmentEntity> findByServiceProviderIdAndStatus(Long serviceProviderId, AppointmentStatus status);
    List<AppointmentEntity> findByServiceProviderIdAndAppointmentTimeBetween(Long serviceProviderId, LocalDateTime start, LocalDateTime end);
}
