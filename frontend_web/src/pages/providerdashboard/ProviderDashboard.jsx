import React, { useEffect, useState } from "react";
import AppBar from "../../component/AppBar";
import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Grid,
  Card,
  CardContent,
} from "@mui/material";
import { usePersonalInfo } from "../../context/PersonalInfoContext";

const ProviderDashboard = () => {
  const [appointmentCount, setAppointmentCount] = useState(0);
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { personalInfo } = usePersonalInfo();
  const token = localStorage.getItem("token");

  useEffect(() => {
    if (!personalInfo?.userId) {
      console.log("No user ID available yet");
      setLoading(false);
      return;
    }

    const fetchData = async () => {
      try {
        console.log(`Fetching data for user ${personalInfo.userId}`);

        // Fetch appointments
        const appointmentsRes = await fetch(
          `http://localhost:8080/api/appointments/all/${personalInfo.userId}`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
        if (!appointmentsRes.ok) {
          const err = await appointmentsRes.text();
          throw new Error(`Appointments error ${appointmentsRes.status}: ${err}`);
        }
        const appointmentsData = await appointmentsRes.json();
        setAppointmentCount(appointmentsData.length);

        // Fetch portfolio
        const portfolioRes = await fetch(
          `http://localhost:8080/api/portfolios/${personalInfo.userId}`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
        if (!portfolioRes.ok && portfolioRes.status !== 404) {
          const err = await portfolioRes.text();
          throw new Error(`Portfolio error ${portfolioRes.status}: ${err}`);
        }
        if (portfolioRes.ok) {
          const portfolioData = await portfolioRes.json();
          setServices(portfolioData.servicesOffered || []);
        }

        setError(null);
      } catch (err) {
        console.error("Dashboard error:", err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [personalInfo?.userId]);

  return (
    <Box sx={{ display: "flex" }}>
      <AppBar />
      <Box sx={{ flexGrow: 1, p: 3 }}>
        <Typography variant="h4" fontWeight="bold" gutterBottom>
          Welcome, {personalInfo?.firstName || "Service Provider"}!
        </Typography>

        {loading ? (
          <Box sx={{ display: "flex", alignItems: "center" }}>
            <CircularProgress size={24} />
            <Typography sx={{ ml: 2 }}>Loading...</Typography>
          </Box>
        ) : error ? (
          <Alert severity="error">{error}</Alert>
        ) : (
          <>
            <Typography variant="h5" sx={{ mt: 2 }}>
              You have <strong>{appointmentCount}</strong> appointment
              {appointmentCount !== 1 ? "s" : ""}
            </Typography>

            {services.length > 0 && (
              <Box sx={{ mt: 4 }}>
                <Typography variant="h5" fontWeight="bold" gutterBottom>
                  Services Offered
                </Typography>
                <Grid container spacing={2}>
                  {services.map((service, index) => (
                    <Grid item xs={12} sm={6} md={4} key={index}>
                      <Card>
                        <CardContent>
                          <Typography variant="h6" fontWeight="bold">
                            {service.name}
                          </Typography>
                          <Typography variant="body2" gutterBottom>
                            {service.description}
                          </Typography>
                          <Typography variant="body2" fontWeight="bold">
                            Pricing:
                          </Typography>
                          <Typography variant="body2">
                            {service.pricing}
                          </Typography>
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              </Box>
            )}
          </>
        )}
      </Box>
    </Box>
  );
};

export default ProviderDashboard;
