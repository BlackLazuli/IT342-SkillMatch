import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Box, Typography, CircularProgress, Card, CardContent, Divider } from "@mui/material";
import AppBar from "../../component/AppBar";

const AppointmentDetailsCustomerPage = () => {
  const { userID } = useParams(); // Get userID from URL parameters
  console.log("User ID from URL:", userID); 
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const token = localStorage.getItem("token");

  useEffect(() => {
    if (!userID) {
      console.error("User ID is not available");
      return;
    }

    const fetchAppointments = async () => {
      try {
        if (!userID) {
          console.error("User ID is not available");
          return;
        }
    
        const res = await fetch(`http://localhost:8080/api/appointments/all/${userID}`, {
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

    fetchAppointments();
  }, [userID, token]); // Only include userID and token in the dependencies

  if (loading) {
    return (
      <Box sx={{ display: "flex" }}>
        <AppBar />
        <Box sx={{ p: 4 }}>
          <CircularProgress />
        </Box>
      </Box>
    );
  }

  if (appointments.length === 0) {
    return (
      <Box sx={{ display: "flex" }}>
        <AppBar />
        <Box sx={{ p: 4 }}>
          <Typography>No appointments found.</Typography>
        </Box>
      </Box>
    );
  }

  return (
    <Box sx={{ display: "flex" }}>
      <AppBar />
      <Box component="main" sx={{ flexGrow: 1, p: 4 }}>
        <Typography variant="h4" gutterBottom>
          Appointment Details
        </Typography>

        {appointments.map((appointment) => (
          <Card key={appointment.id}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Appointment ID: {appointment.id}
              </Typography>

              <Divider sx={{ my: 2 }} />

              <Typography variant="body1">
                <strong>User:</strong> {appointment.userFirstName} {appointment.userLastName}
              </Typography>

              <Typography variant="body1">
                <strong>Role:</strong> {appointment.role}
              </Typography>

              <Typography variant="body1">
                <strong>Time:</strong> {new Date(appointment.appointmentTime).toLocaleString()}
              </Typography>

              <Typography variant="body1">
                <strong>Status:</strong> {appointment.status}
              </Typography>

              <Typography variant="body1">
                <strong>Notes:</strong> {appointment.notes || "None"}
              </Typography>

              <Typography variant="body1">
                <strong>Portfolio ID:</strong> {appointment.portfolioId}
              </Typography>

              <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                Created At: {new Date(appointment.createdAt).toLocaleString()}
              </Typography>
            </CardContent>
          </Card>
        ))}
      </Box>
    </Box>
  );
};

export default AppointmentDetailsCustomerPage;
