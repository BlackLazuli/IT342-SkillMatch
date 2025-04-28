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
  Link
} from "@mui/material";
import AppBar from "../../component/AppBar";
import {
  CalendarToday,
  Person,
  Work,
  AccessTime,
  CheckCircle,
  Notes,
  Event
} from "@mui/icons-material";

const AppointmentDetailsCustomerPage = () => {
  const { userID } = useParams();
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const token = localStorage.getItem("token");
  const baseUrl = "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080"; // Change to your EC2 public IP/DNS

  const handleClientClick = (userId) => {
    navigate(`/client-profile/${userId}`);
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
      
      // Determine endpoint based on action
      if (newStatus === 'COMPLETED') {
        endpoint = `/api/appointments/${appointmentId}/complete`;
      } else if (newStatus === 'CANCELED') {
        endpoint = `/api/appointments/${appointmentId}/cancel`;
      } else {
        endpoint = `/api/appointments/${appointmentId}`;
        method = 'PATCH'; // More appropriate for partial updates
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
  
      // Update local state
      setAppointments(appointments.map(appt => 
        appt.id === appointmentId ? { ...appt, status: newStatus } : appt
      ));
    } catch (error) {
      console.error("Update error:", error);
      // Show error to user (e.g., using a snackbar or alert)
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
            <Button variant="contained" sx={{ mt: 2 }}>
              Book an Appointment
            </Button>
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
                        <strong>Client:</strong>
                        <Link
                          component="button"
                          variant="body1"
                          onClick={() => handleClientClick(appointment.userId)}
                          sx={{
                            ml: 0.5,
                            cursor: 'pointer',
                            '&:hover': {
                              textDecoration: 'underline',
                              color: 'primary.main'
                            }
                          }}
                        >
                          {appointment.userFirstName} {appointment.userLastName}
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
                  {appointment.status.toLowerCase() === 'pending' && (
                    <Button 
                      size="small" 
                      color="error" 
                      sx={{ ml: 1 }}
                      onClick={() => updateAppointmentStatus(appointment.id, 'CANCELED')}
                      disabled={updating}
                    >
                      Cancel
                    </Button>
                  )}
                  {(appointment.status.toLowerCase() === 'scheduled' || 
                    appointment.status.toLowerCase() === 'confirmed') && (
                    <Button 
                      size="small" 
                      color="success" 
                      sx={{ ml: 1 }}
                      onClick={() => updateAppointmentStatus(appointment.id, 'COMPLETED')}
                      disabled={updating}
                      startIcon={updating ? <CircularProgress size={20} /> : null}
                    >
                      Mark as Completed
                    </Button>
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