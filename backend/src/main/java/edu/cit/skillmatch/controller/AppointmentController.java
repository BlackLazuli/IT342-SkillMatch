package edu.cit.skillmatch.controller;

import edu.cit.skillmatch.entity.AppointmentEntity;
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

    // Get all appointments for a service provider
    @GetMapping("/service-provider/{serviceProviderId}")
    public ResponseEntity<List<AppointmentEntity>> getAppointmentsByServiceProvider(@PathVariable Long serviceProviderId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByServiceProvider(serviceProviderId));
    }

    // Get all appointments for a customer
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AppointmentEntity>> getAppointmentsByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByCustomer(customerId));
    }

    // Get an appointment by ID
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentEntity> getAppointmentById(@PathVariable Long id) {
        Optional<AppointmentEntity> appointment = appointmentService.getAppointmentById(id);
        return appointment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Book a new appointment
    @PostMapping("/")
    public ResponseEntity<AppointmentEntity> bookAppointment(@RequestParam Long customerId,
                                                             @RequestParam Long serviceProviderId,
                                                             @RequestParam String appointmentTime,
                                                             @RequestParam(required = false) String notes) {
        LocalDateTime time = LocalDateTime.parse(appointmentTime);
        try {
            AppointmentEntity booked = appointmentService.bookAppointment(customerId, serviceProviderId, time, notes);
            return ResponseEntity.ok(booked);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Reschedule an appointment
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentEntity> rescheduleAppointment(@PathVariable Long id, @RequestParam String newTime) {
        LocalDateTime time = LocalDateTime.parse(newTime);
        Optional<AppointmentEntity> updated = appointmentService.rescheduleAppointment(id, time);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Cancel an appointment
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentEntity> cancelAppointment(@PathVariable Long id) {
        Optional<AppointmentEntity> canceled = appointmentService.cancelAppointment(id);
        return canceled.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
