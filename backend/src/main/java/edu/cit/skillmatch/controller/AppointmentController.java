package edu.cit.skillmatch.controller;

import edu.cit.skillmatch.dto.AppointmentDTO;
import edu.cit.skillmatch.entity.AppointmentEntity;
import edu.cit.skillmatch.entity.PortfolioEntity;
import edu.cit.skillmatch.entity.ServiceEntity;
import edu.cit.skillmatch.entity.UserEntity;
import edu.cit.skillmatch.service.AppointmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.skillmatch.repository.AppointmentRepository;
import edu.cit.skillmatch.repository.PortfolioRepository;
import edu.cit.skillmatch.repository.ServiceRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

        @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
private ServiceRepository serviceRepository;


    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByUser(@PathVariable Long userId, @RequestParam String role) {
        List<AppointmentDTO> dtos = appointmentService.getAppointmentsByUser(userId, role).stream().map(appointment -> {
            AppointmentDTO dto = new AppointmentDTO();
            dto.setId(appointment.getId());
            dto.setUserId(appointment.getUser().getId());
            dto.setUserFirstName(appointment.getUser().getFirstName());
            dto.setUserLastName(appointment.getUser().getLastName());
            dto.setRole(appointment.getRole());
            dto.setAppointmentTime(appointment.getAppointmentTime());
            dto.setStatus(appointment.getStatus());
            dto.setNotes(appointment.getNotes());
            dto.setCreatedAt(appointment.getCreatedAt());
            dto.setPortfolioId(appointment.getPortfolio().getId());  // Set portfolioId
            return dto;
        }).toList();
    
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        Optional<AppointmentEntity> optional = appointmentService.getAppointmentById(id);
    
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
    
        AppointmentEntity appointment = optional.get();
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setUserId(appointment.getUser().getId());
        dto.setUserFirstName(appointment.getUser().getFirstName());
        dto.setUserLastName(appointment.getUser().getLastName());
        dto.setRole(appointment.getRole());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setPortfolioId(appointment.getPortfolio().getId());  // Set portfolioId
    
        return ResponseEntity.ok(dto);
    }
@PostMapping("/")
public ResponseEntity<AppointmentDTO> bookAppointment(@RequestBody AppointmentEntity appointmentEntity) {
    try {
        // Validate portfolio
        Optional<PortfolioEntity> portfolio = portfolioRepository.findByIdWithUser(
            appointmentEntity.getPortfolio().getId()
        );

        if (portfolio.isEmpty()) {
            return ResponseEntity.badRequest().build(); // Portfolio not found
        }

// Manually resolve and attach existing service
if (appointmentEntity.getService() != null && appointmentEntity.getService().getId() != null) {
    Optional<ServiceEntity> serviceOpt = serviceRepository.findById(
        appointmentEntity.getService().getId()
    );

    if (serviceOpt.isEmpty()) {
        return ResponseEntity.badRequest().build(); // Service not found
    }

    appointmentEntity.setService(serviceOpt.get());
}

        // Save the appointment
        AppointmentEntity booked = appointmentRepository.save(appointmentEntity);

        // Construct DTO
        UserEntity provider = portfolio.get().getUser();

        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(booked.getId());
        dto.setUserId(booked.getUser().getId());
        dto.setUserFirstName(booked.getUser().getFirstName());
        dto.setUserLastName(booked.getUser().getLastName());
        dto.setRole(booked.getRole());
        dto.setAppointmentTime(booked.getAppointmentTime());
        dto.setStatus(booked.getStatus());
        dto.setNotes(booked.getNotes());
        dto.setCreatedAt(booked.getCreatedAt());
        dto.setPortfolioId(booked.getPortfolio().getId());
        dto.setProviderFirstName(provider.getFirstName());
        dto.setProviderLastName(provider.getLastName());
        dto.setProviderId(provider.getId());

        if (booked.getService() != null) {
            dto.setServiceId(booked.getService().getId());
            dto.setServiceName(booked.getService().getName());
        }

        return ResponseEntity.ok(dto);
    } catch (RuntimeException e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().build();
    }
}


    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentDTO> rescheduleAppointment(@PathVariable Long id, @RequestParam String newTime) {
        LocalDateTime time = LocalDateTime.parse(newTime);
        Optional<AppointmentEntity> optional = appointmentService.rescheduleAppointment(id, time);
    
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
    
        AppointmentEntity appointment = optional.get();
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setUserId(appointment.getUser().getId());
        dto.setUserFirstName(appointment.getUser().getFirstName());
        dto.setUserLastName(appointment.getUser().getLastName());
        dto.setRole(appointment.getRole());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setPortfolioId(appointment.getPortfolio().getId());  // Add portfolioId to DTO

        UserEntity provider = appointment.getPortfolio().getUser();
        dto.setProviderFirstName(provider.getFirstName());
        dto.setProviderLastName(provider.getLastName());
        dto.setProviderId(provider.getId());  // Add this line to set providerId
    
        return ResponseEntity.ok(dto);
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentDTO> cancelAppointment(@PathVariable Long id) {
        Optional<AppointmentEntity> optional = appointmentService.cancelAppointment(id);
    
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
    
        AppointmentEntity appointment = optional.get();
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setUserId(appointment.getUser().getId());
        dto.setUserFirstName(appointment.getUser().getFirstName());
        dto.setUserLastName(appointment.getUser().getLastName());
        dto.setRole(appointment.getRole());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setPortfolioId(appointment.getPortfolio().getId());  // Add portfolioId to DTO

                // Get provider details from portfolio
                UserEntity provider = appointment.getPortfolio().getUser();
                dto.setProviderFirstName(provider.getFirstName());
                dto.setProviderLastName(provider.getLastName());
                dto.setProviderId(provider.getId());  // Add this line to set providerId
    
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<AppointmentEntity>> getAppointmentsByPortfolio(@PathVariable Long portfolioId) {
        List<AppointmentEntity> appointments = appointmentService.getAppointmentsByPortfolio(portfolioId);
        return ResponseEntity.ok(appointments);
    }

  @GetMapping("/all/{userId}")
public ResponseEntity<List<AppointmentDTO>> getAllAppointmentsForUser(@PathVariable Long userId) {
    // Fetch appointments for the user
    List<AppointmentEntity> appointments = appointmentService.getAllAppointmentsForUser(userId);

    // Map appointments to DTOs and include service details
    List<AppointmentDTO> dtos = appointments.stream().map(appointment -> {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setUserId(appointment.getUser().getId());
        dto.setUserFirstName(appointment.getUser().getFirstName());
        dto.setUserLastName(appointment.getUser().getLastName());
        dto.setRole(appointment.getRole());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setPortfolioId(appointment.getPortfolio().getId());

        // Get provider details from portfolio
        UserEntity provider = appointment.getPortfolio().getUser();
        dto.setProviderFirstName(provider.getFirstName());
        dto.setProviderLastName(provider.getLastName());
        dto.setProviderId(provider.getId());  // Add this line to set providerId

        // Get service details if available
        if (appointment.getService() != null) {
            dto.setServiceId(appointment.getService().getId());
            dto.setServiceName(appointment.getService().getName());
        }

        return dto;
    }).collect(Collectors.collectingAndThen(
        Collectors.toMap(AppointmentDTO::getId, Function.identity(), (a, b) -> a),  // Deduplicate by ID
        map -> new ArrayList<>(map.values()) // Return as a list
    ));

    return ResponseEntity.ok(dtos);
}
   @PutMapping("/{id}/complete")
public ResponseEntity<AppointmentDTO> completeAppointment(@PathVariable Long id) {
    Optional<AppointmentEntity> optional = appointmentService.completeAppointment(id);

    if (optional.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    AppointmentEntity appointment = optional.get();
    AppointmentDTO dto = new AppointmentDTO();
    dto.setId(appointment.getId());
    dto.setUserId(appointment.getUser().getId());
    dto.setUserFirstName(appointment.getUser().getFirstName());
    dto.setUserLastName(appointment.getUser().getLastName());
    dto.setRole(appointment.getRole());
    dto.setAppointmentTime(appointment.getAppointmentTime());
    dto.setStatus(appointment.getStatus());
    dto.setNotes(appointment.getNotes());
    dto.setCreatedAt(appointment.getCreatedAt());
    dto.setPortfolioId(appointment.getPortfolio().getId());
    // Get provider details from portfolio
    UserEntity provider = appointment.getPortfolio().getUser();
    dto.setProviderFirstName(provider.getFirstName());
    dto.setProviderLastName(provider.getLastName());
    dto.setProviderId(provider.getId());

    return ResponseEntity.ok(dto);
}

}
