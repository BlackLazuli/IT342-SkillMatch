package edu.cit.skillmatch.service;

import edu.cit.skillmatch.entity.AppointmentEntity;
import edu.cit.skillmatch.entity.AppointmentStatus;
import edu.cit.skillmatch.entity.UserEntity;
import edu.cit.skillmatch.repository.AppointmentRepository;
import edu.cit.skillmatch.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    public List<AppointmentEntity> getAppointmentsByServiceProvider(Long serviceProviderId) {
        return appointmentRepository.findByServiceProviderId(serviceProviderId);
    }

    public List<AppointmentEntity> getAppointmentsByCustomer(Long customerId) {
        return appointmentRepository.findByCustomerId(customerId);
    }

    public Optional<AppointmentEntity> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    public AppointmentEntity bookAppointment(Long customerId, Long serviceProviderId, LocalDateTime appointmentTime, String notes) {
        Optional<UserEntity> customer = userRepository.findById(customerId);
        Optional<UserEntity> serviceProvider = userRepository.findById(serviceProviderId);

        if (customer.isEmpty() || serviceProvider.isEmpty()) {
            throw new RuntimeException("Customer or Service Provider not found");
        }

        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setCustomer(customer.get());
        appointment.setServiceProvider(serviceProvider.get());
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes(notes);

        return appointmentRepository.save(appointment);
    }

    public Optional<AppointmentEntity> rescheduleAppointment(Long id, LocalDateTime newTime) {
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setAppointmentTime(newTime);
            appointment.setStatus(AppointmentStatus.RESCHEDULED);
            return appointmentRepository.save(appointment);
        });
    }

    public Optional<AppointmentEntity> cancelAppointment(Long id) {
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setStatus(AppointmentStatus.CANCELED);
            return appointmentRepository.save(appointment);
        });
    }
}
