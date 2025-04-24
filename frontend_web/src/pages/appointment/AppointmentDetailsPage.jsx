import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
  Box,
  Typography,
  CircularProgress,
  Card,
  CardContent,
  Divider,
} from "@mui/material";
import AppBar from "../../component/AppBarCustomer";

const AppointmentDetailsPage = () => {
  const { appointmentId } = useParams();
  const [appointment, setAppointment] = useState(null);
  const [loading, setLoading] = useState(true);
  const token = localStorage.getItem("token");

  useEffect(() => {
    const fetchAppointment = async () => {
      try {
        const res = await fetch(`http://localhost:8080/api/appointments/${appointmentId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!res.ok) throw new Error("Failed to fetch appointment");

        const data = await res.json();
        setAppointment(data);
      } catch (error) {
        console.error("Error fetching appointment:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchAppointment();
  }, [appointmentId, token]);

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

  if (!appointment) {
    return (
      <Box sx={{ display: "flex" }}>
        <AppBar />
        <Box sx={{ p: 4 }}>
          <Typography>Appointment not found.</Typography>
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

        <Card>
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
      </Box>
    </Box>
  );
};

export default AppointmentDetailsPage;
