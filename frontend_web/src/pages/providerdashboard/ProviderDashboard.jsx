import React, { useEffect, useState } from "react";
import AppBar from "../../component/AppBar";
import { useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  Grid,
  Card,
  CardContent,
  Paper,
  Avatar,
  Divider,
  useTheme,
  useMediaQuery
} from "@mui/material";
import { usePersonalInfo } from "../../context/PersonalInfoContext";
import {
  CalendarToday,
  WorkOutline,
  MonetizationOn,
  Description
} from "@mui/icons-material";

const ProviderDashboard = () => {
  const [appointmentCount, setAppointmentCount] = useState(0);
  const navigate = useNavigate();
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { personalInfo } = usePersonalInfo();
  const token = localStorage.getItem("token");
  const theme = useTheme();
  const baseUrl = "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080"; // Change to your EC2 public IP/DNS
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

  // Redirect to login if user is not authenticated
useEffect(() => {
  if (!personalInfo?.userId) {
    navigate("/", { replace: true });
  }
}, [personalInfo?.userId, navigate]);
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
          `/api/appointments/all/${personalInfo.userId}`,
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
        const scheduledAppointments = appointmentsData.filter(
          (appt) => appt.status === "SCHEDULED" || appt.status === "RESCHEDULED"
        );
        setAppointmentCount(scheduledAppointments.length);
        

        // Fetch portfolio
        const portfolioRes = await fetch(
          `/api/portfolios/${personalInfo.userId}`,
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
    <Box sx={{ display: "flex", minHeight: "100vh" }}>
      <AppBar />
      <Box sx={{ flexGrow: 1, p: isMobile ? 2 : 3, backgroundColor: "#f5f7fa" }}>
        <Box sx={{ mb: 4 }}>
          <Typography variant="h4" fontWeight="bold" gutterBottom>
            Welcome back, {personalInfo?.firstName || "Provider"}!
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Here's what's happening with your business today
          </Typography>
        </Box>

        {loading ? (
          <Box sx={{ display: "flex", alignItems: "center", justifyContent: "center", height: "60vh" }}>
            <CircularProgress size={60} />
          </Box>
        ) : error ? (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        ) : (
          <>
            <Grid container spacing={3} sx={{ mb: 4 }}>
              <Grid item xs={12} md={6} lg={4}>
                <Paper elevation={3} sx={{ p: 3, borderRadius: 2 }}>
                  <Box sx={{ display: "flex", alignItems: "center" }}>
                    <Avatar sx={{ 
                      backgroundColor: theme.palette.primary.light, 
                      mr: 2,
                      width: 56,
                      height: 56
                    }}>
                      <CalendarToday />
                    </Avatar>
                    <Box>
                      <Typography variant="h6" color="text.secondary">
                        Appointments
                      </Typography>
                      <Typography variant="h4" fontWeight="bold">
                        {appointmentCount}
                      </Typography>
                    </Box>
                  </Box>
                </Paper>
              </Grid>
              <Grid item xs={12} md={6} lg={4}>
                <Paper elevation={3} sx={{ p: 3, borderRadius: 2 }}>
                  <Box sx={{ display: "flex", alignItems: "center" }}>
                    <Avatar sx={{ 
                      backgroundColor: theme.palette.secondary.light, 
                      mr: 2,
                      width: 56,
                      height: 56
                    }}>
                      <WorkOutline />
                    </Avatar>
                    <Box>
                      <Typography variant="h6" color="text.secondary">
                        Services Offered
                      </Typography>
                      <Typography variant="h4" fontWeight="bold">
                        {services.length}
                      </Typography>
                    </Box>
                  </Box>
                </Paper>
              </Grid>
            </Grid>

            {services.length > 0 && (
              <Paper elevation={3} sx={{ p: 3, borderRadius: 2, mb: 4 }}>
                <Box sx={{ display: "flex", alignItems: "center", mb: 3 }}>
                  <Description color="primary" sx={{ mr: 1, fontSize: 32 }} />
                  <Typography variant="h5" fontWeight="bold">
                    Your Services
                  </Typography>
                </Box>
                <Divider sx={{ mb: 3 }} />
                <Grid container spacing={3}>
                  {services.map((service, index) => (
                    <Grid item xs={12} sm={6} md={4} key={index}>
                      <Card sx={{ 
                        height: "100%", 
                        display: "flex", 
                        flexDirection: "column",
                        transition: "transform 0.3s, box-shadow 0.3s",
                        "&:hover": {
                          transform: "translateY(-5px)",
                          boxShadow: theme.shadows[6]
                        }
                      }}>
                        <CardContent sx={{ flexGrow: 1 }}>
                          <Typography 
                            variant="h6" 
                            fontWeight="bold" 
                            gutterBottom
                            sx={{ color: theme.palette.primary.main }}
                          >
                            {service.name}
                          </Typography>
                          <Typography 
                            variant="body2" 
                            color="text.secondary" 
                            gutterBottom
                            sx={{ mb: 2 }}
                          >
                            {service.description}
                          </Typography>
                          <Box sx={{ 
                            backgroundColor: theme.palette.grey[100], 
                            p: 2, 
                            borderRadius: 1 
                          }}>
                            <Typography variant="subtitle2" fontWeight="bold">
                              Pricing:
                            </Typography>
                            <Typography variant="body1">
                              {service.pricing}
                            </Typography>
                          </Box>
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              </Paper>
            )}
          </>
        )}
      </Box>
    </Box>
  );
};

export default ProviderDashboard;