import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { 
  Box, 
  Typography, 
  CircularProgress, 
  Card, 
  CardContent, 
  Divider,
  Chip,
  Grid,
  Paper,
  Avatar,
  Stack,
  Button,
  Link,
  Alert,
  Snackbar,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField
} from "@mui/material";
import AppBar from "../../component/AppBarCustomer";
import {
  CalendarToday,
  Person,
  AccessTime,
  CheckCircle,
  Notes,
  Event,
  Schedule
} from "@mui/icons-material";
import { DateTimePicker } from "@mui/x-date-pickers";

const AppointmentDetailsCustomerPage = () => {
  const { userID } = useParams();
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState(null);
  const [openSnackbar, setOpenSnackbar] = useState(false);
  const [rescheduleDialogOpen, setRescheduleDialogOpen] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [newAppointmentTime, setNewAppointmentTime] = useState(null);
  const token = localStorage.getItem("token");
  const baseUrl = "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080"; // Change to your EC2 public IP/DNS

  const handleProviderClick = (providerId) => {
    if (!providerId) {
      console.error("No provider ID available");
      return;
    }
    navigate(`/provider-profile/${providerId}`);
  };

  const handleCloseSnackbar = () => {
    setOpenSnackbar(false);
    setError(null);
  };

  const handleOpenRescheduleDialog = (appointment) => {
    setSelectedAppointment(appointment);
    setNewAppointmentTime(new Date(appointment.appointmentTime));
    setRescheduleDialogOpen(true);
  };

  const handleCloseRescheduleDialog = () => {
    setRescheduleDialogOpen(false);
    setSelectedAppointment(null);
    setNewAppointmentTime(null);
  };

  const updateAppointmentStatus = async (appointmentId, newStatus) => {
    setUpdating(true);
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        throw new Error("No authentication token found");
      }
  
      let endpoint;
      let method = 'PUT';
      
      if (newStatus === 'COMPLETED') {
        endpoint = `/api/appointments/${appointmentId}/complete`;
      } else if (newStatus === 'CANCELED') {
        endpoint = `/api/appointments/${appointmentId}/cancel`;
      } else {
        endpoint = `/api/appointments/${appointmentId}`;
        method = 'PATCH';
      }
  
      const headers = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      };
  
      const body = newStatus !== 'COMPLETED' && newStatus !== 'CANCELED' 
        ? JSON.stringify({ status: newStatus }) 
        : undefined;
  
      const res = await fetch(endpoint, {
        method,
        headers,
        body
      });
  
      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || `Failed to update status to ${newStatus}`);
      }
  
      setAppointments(appointments.map(appt => 
        appt.id === appointmentId ? { ...appt, status: newStatus } : appt
      ));
    } catch (error) {
      console.error("Update error:", error);
      setError(error.message);
      setOpenSnackbar(true);
    } finally {
      setUpdating(false);
    }
  };

  const handleRescheduleAppointment = async () => {
    if (!selectedAppointment || !newAppointmentTime) return;
    
    setUpdating(true);
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        throw new Error("No authentication token found");
      }

      const res = await fetch(`/api/appointments/${selectedAppointment.id}/reschedule`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          newTime: newAppointmentTime.toISOString()
        })
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || "Failed to reschedule appointment");
      }

      const updatedAppointment = await res.json();
      
      setAppointments(appointments.map(appt => 
        appt.id === updatedAppointment.id ? { 
          ...appt, 
          status: 'RESCHEDULED',
          appointmentTime: updatedAppointment.appointmentTime 
        } : appt
      ));

      setOpenSnackbar(true);
      handleCloseRescheduleDialog();
    } catch (error) {
      console.error("Reschedule error:", error);
      setError(error.message);
      setOpenSnackbar(true);
    } finally {
      setUpdating(false);
    }
  };

  useEffect(() => {
    const fetchAppointments = async () => {
      try {
        const res = await fetch(`/api/appointments/all/${userID}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
    
        if (!res.ok) throw new Error("Failed to fetch appointments");
    
        const data = await res.json();
        setAppointments(data);
      } catch (error) {
        console.error("Error fetching appointments:", error);
        setError(error.message);
        setOpenSnackbar(true);
      } finally {
        setLoading(false);
      }
    };

    if (userID) fetchAppointments();
  }, [userID, token]);

  const getStatusChip = (status) => {
    let color;
    switch(status.toLowerCase()) {
      case 'confirmed':
      case 'completed':
        color = 'success';
        break;
      case 'pending':
      case 'scheduled':
        color = 'warning';
        break;
      case 'cancelled':
      case 'canceled':
        color = 'error';
        break;
      case 'rescheduled':
        color = 'info';
        break;
      default:
        color = 'default';
    }
    return <Chip label={status} color={color} size="small" />;
  };

  if (loading) {
    return (
      <Box sx={{ display: "flex" }}>
        <AppBar />
        <Box sx={{ p: 4, width: '100%', display: 'flex', justifyContent: 'center' }}>
          <CircularProgress />
        </Box>
      </Box>
    );
  }

  if (appointments.length === 0) {
    return (
      <Box sx={{ display: "flex" }}>
        <AppBar />
        <Box sx={{ p: 4, width: '100%', textAlign: 'center' }}>
          <Paper sx={{ p: 4, maxWidth: 600, margin: '0 auto' }}>
            <Typography variant="h5" gutterBottom>
              No Appointments Found
            </Typography>
            <Typography variant="body1" color="text.secondary">
              You don't have any appointments scheduled yet.
            </Typography>
          </Paper>
        </Box>
      </Box>
    );
  }

  return (
    <Box sx={{ display: "flex" }}>
      <AppBar />
      <Box component="main" sx={{ flexGrow: 1, p: { xs: 2, md: 4 } }}>
        <Typography variant="h4" gutterBottom sx={{ mb: 4 }}>
          My Appointments
        </Typography>

        <Snackbar
          open={openSnackbar}
          autoHideDuration={6000}
          onClose={handleCloseSnackbar}
          anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
        >
          <Alert
            onClose={handleCloseSnackbar}
            severity={error ? "error" : "success"}
            sx={{ width: '100%' }}
          >
            {error || "Appointment updated successfully!"}
          </Alert>
        </Snackbar>

        {/* Reschedule Dialog */}
        <Dialog open={rescheduleDialogOpen} onClose={handleCloseRescheduleDialog}>
          <DialogTitle>Reschedule Appointment</DialogTitle>
          <DialogContent>
            <Box sx={{ mt: 2 }}>
            <TextField
  label="New Appointment Time"
  type="datetime-local"
  fullWidth
  value={newAppointmentTime ? newAppointmentTime.toISOString().slice(0, 16) : ''}
  onChange={(e) => setNewAppointmentTime(new Date(e.target.value))}
  InputLabelProps={{
    shrink: true,
  }}
  inputProps={{
    min: new Date().toISOString().slice(0, 16) // Disable past dates
  }}
/>

            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseRescheduleDialog}>Cancel</Button>
            <Button 
              onClick={handleRescheduleAppointment} 
              color="primary"
              disabled={updating || !newAppointmentTime}
              startIcon={updating ? <CircularProgress size={20} /> : <Schedule />}
            >
              Reschedule
            </Button>
          </DialogActions>
        </Dialog>

        <Grid container spacing={3}>
          {appointments.map((appointment) => (
            <Grid item xs={12} md={6} lg={4} key={appointment.id}>
              <Card sx={{ 
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                transition: '0.3s',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 3
                }
              }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Stack direction="row" spacing={2} alignItems="center" mb={2}>
                    <Avatar sx={{ bgcolor: 'primary.main' }}>
                      <CalendarToday />
                    </Avatar>
                    <Typography variant="h6" component="div">
                      Appointment #{appointments.indexOf(appointment) + 1}
                    </Typography>
                  </Stack>

                  <Divider sx={{ my: 2 }} />

                  <Stack spacing={2}>
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <Person color="action" sx={{ mr: 1 }} />
                      <Typography variant="body1">
                        <strong>Provider:</strong>
                        <Link
                          component="button"
                          variant="body1"
                          onClick={() => handleProviderClick(appointment.providerId)}
                          sx={{
                            ml: 0.5,
                            cursor: 'pointer',
                            '&:hover': {
                              textDecoration: 'underline',
                              color: 'primary.main'
                            }
                          }}
                        >
                          {appointment.providerFirstName} {appointment.providerLastName}
                        </Link>
                      </Typography>
                    </Box>

                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <AccessTime color="action" sx={{ mr: 1 }} />
                      <Typography variant="body1">
                        <strong>Time:</strong> {new Date(appointment.appointmentTime).toLocaleString()}
                      </Typography>
                    </Box>

                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <CheckCircle color="action" sx={{ mr: 1 }} />
                      <Typography variant="body1">
                        <strong>Status:</strong> {getStatusChip(appointment.status)}
                      </Typography>
                    </Box>

                    {appointment.notes && (
                      <Box sx={{ display: 'flex', alignItems: 'flex-start' }}>
                        <Notes color="action" sx={{ mr: 1, mt: 0.5 }} />
                        <Typography variant="body1">
                          <strong>Notes:</strong> {appointment.notes}
                        </Typography>
                      </Box>
                    )}

                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <Event color="action" sx={{ mr: 1 }} />
                      <Typography variant="body2" color="text.secondary">
                        Created: {new Date(appointment.createdAt).toLocaleString()}
                      </Typography>
                    </Box>
                  </Stack>
                </CardContent>

                <Box sx={{ p: 2, display: 'flex', justifyContent: 'flex-end' }}>
                  {(appointment.status.toLowerCase() === 'scheduled' || 
                    appointment.status.toLowerCase() === 'confirmed' ||
                    appointment.status.toLowerCase() === 'pending') && (
                    <>
                      <Button 
                        size="small" 
                        color="error" 
                        sx={{ ml: 1 }}
                        onClick={() => updateAppointmentStatus(appointment.id, 'CANCELED')}
                        disabled={updating}
                        startIcon={updating ? <CircularProgress size={20} /> : null}
                      >
                        Cancel
                      </Button>
                    </>
                  )}
                </Box>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Box>
    </Box>
  );
};

export default AppointmentDetailsCustomerPage;