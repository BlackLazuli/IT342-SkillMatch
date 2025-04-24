package edu.cit.skillmatch.service;

import edu.cit.skillmatch.entity.AppointmentEntity;
import edu.cit.skillmatch.entity.AppointmentStatus;
import edu.cit.skillmatch.entity.PortfolioEntity;
import edu.cit.skillmatch.entity.UserEntity;
import edu.cit.skillmatch.repository.AppointmentRepository;
import edu.cit.skillmatch.repository.PortfolioRepository;
import edu.cit.skillmatch.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository, PortfolioRepository portfolioRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public List<AppointmentEntity> getAppointmentsByUser(Long userId, String role) {
        return appointmentRepository.findByUserIdAndRole(userId, role);
    }

    public Optional<AppointmentEntity> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    public AppointmentEntity bookAppointment(Long userId, String role, Long portfolioId, LocalDateTime appointmentTime, String notes) {
        Optional<UserEntity> user = userRepository.findById(userId);
        Optional<PortfolioEntity> portfolio = portfolioRepository.findById(portfolioId); // Fetch the portfolio
    
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        if (portfolio.isEmpty()) {
            throw new RuntimeException("Portfolio not found");
        }
    
        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setUser(user.get());
        appointment.setRole(role);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED); // Default status
        appointment.setNotes(notes);
        appointment.setPortfolio(portfolio.get()); // Set the portfolio
    
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

    public List<AppointmentEntity> getAppointmentsByPortfolio(Long portfolioId) {
        return appointmentRepository.findByPortfolioId(portfolioId);
    }

    public List<AppointmentEntity> getAppointmentsByPortfolioAndStatus(Long portfolioId, AppointmentStatus status) {
        return appointmentRepository.findByPortfolioIdAndStatus(portfolioId, status);
    }

    public List<AppointmentEntity> getAllAppointmentsForUser(Long userId) {
        // Appointments booked by the user (customer)
        List<AppointmentEntity> asCustomer = appointmentRepository.findByUserId(userId);
    
        // Get all portfolios owned by the user (as provider)
        List<PortfolioEntity> portfolios = portfolioRepository.findAllByUserId(userId);
    
        // Appointments linked to any of their portfolios (as provider)
        List<AppointmentEntity> asProvider = portfolios.stream()
                .flatMap(p -> appointmentRepository.findByPortfolioId(p.getId()).stream())
                .toList();
    
        // Combine both lists
        asCustomer.addAll(asProvider);
        return asCustomer;
    }
    
}
