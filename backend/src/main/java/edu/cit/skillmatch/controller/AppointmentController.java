package edu.cit.skillmatch.controller;

import edu.cit.skillmatch.dto.AppointmentDTO;
import edu.cit.skillmatch.entity.AppointmentEntity;
import edu.cit.skillmatch.entity.UserEntity;
import edu.cit.skillmatch.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

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
            AppointmentEntity booked = appointmentService.bookAppointment(
                    appointmentEntity.getUser().getId(),
                    appointmentEntity.getRole(),
                    appointmentEntity.getPortfolio().getId(), // Pass portfolioId
                    appointmentEntity.getAppointmentTime(),
                    appointmentEntity.getNotes()
            );
    
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
            dto.setPortfolioId(booked.getPortfolio().getId());  // Add portfolioId to DTO
    
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
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
    
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<AppointmentEntity>> getAppointmentsByPortfolio(@PathVariable Long portfolioId) {
        List<AppointmentEntity> appointments = appointmentService.getAppointmentsByPortfolio(portfolioId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointmentsForUser(@PathVariable Long userId) {
        List<AppointmentEntity> appointments = appointmentService.getAllAppointmentsForUser(userId);
    
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
    
            return dto;
        }).toList();
    
        return ResponseEntity.ok(dtos);
    }

}
