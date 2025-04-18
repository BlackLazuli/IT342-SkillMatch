import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  Button,
  CircularProgress,
  Avatar,
  Card,
  CardContent,
  Grid,
  Chip
} from "@mui/material";
import AppBar from "../../component/AppBar";
import { usePersonalInfo } from "../../context/PersonalInfoContext";

const PortfolioPage = () => {
  const { userID } = useParams();
  const navigate = useNavigate();
  const [portfolio, setPortfolio] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { personalInfo } = usePersonalInfo();

  useEffect(() => {
    const fetchPortfolio = async () => {
      const token = localStorage.getItem("token");

      if (!token) {
        alert("Please log in first.");
        return;
      }

      try {
        const response = await fetch(`http://localhost:8080/api/portfolios/${userID}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (response.status === 404) {
          setPortfolio(null);
          setLoading(false);
          return;
        }

        if (!response.ok) {
          throw new Error(`Error: ${response.status} - ${response.statusText}`);
        }

        const portfolioData = await response.json();
        setPortfolio(portfolioData);
      } catch (error) {
        setError("Failed to load portfolio.");
      } finally {
        setLoading(false);
      }
    };

    fetchPortfolio();
  }, [userID]);

  const handleAddPortfolio = () => {
    navigate(`/add-portfolio/${userID}`);
  };

  const handleUpdatePortfolio = () => {
    navigate(`/edit-portfolio/${userID}`);
  };

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

  return (
    <Box sx={{ display: "flex" }}>
      <AppBar />

      <Box component="main" sx={{ flexGrow: 1, p: 4 }}>
        {!portfolio ? (
          <>
            <Typography variant="h4" gutterBottom>
              Portfolio
            </Typography>
            <Typography>No portfolio found for this user.</Typography>
            <Button variant="contained" sx={{ mt: 2 }} onClick={handleAddPortfolio}>
              Add Portfolio
            </Button>
          </>
        ) : (
          <>
            <Box sx={{ display: "flex", alignItems: "center", mb: 4 }}>
              <Avatar
                alt={personalInfo?.firstName}
                src="/placeholder-avatar.png"
                sx={{ width: 80, height: 80, mr: 2 }}
              />
              <Typography variant="h4" fontWeight="bold">
                My Portfolio
              </Typography>
            </Box>

            <Button
              variant="contained"
              sx={{ mb: 4 }}
              onClick={handleUpdatePortfolio}
            >
              Update Portfolio
            </Button>

            <Card sx={{ backgroundColor: "#fff4e6", mb: 4 }}>
              <CardContent>
                <Typography variant="h5" fontWeight="bold" gutterBottom>
                  Work Experience
                </Typography>
                <Typography variant="body2" gutterBottom>
                  {portfolio?.workExperience}
                </Typography>

                <Typography variant="h5" fontWeight="bold" gutterBottom sx={{ mt: 3 }}>
                  Services Offered
                </Typography>
                <Grid container spacing={2}>
                  {portfolio?.servicesOffered?.map((service, index) => (
                    <Grid item xs={12} sm={6} md={4} key={index}>
                      <Card elevation={2}>
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
                          <Typography variant="body2">{service.pricing}</Typography>

                          <Typography variant="body2" fontWeight="bold" sx={{ mt: 1 }}>
                            Days Available:
                          </Typography>
                          <Box sx={{ display: "flex", flexWrap: "wrap", gap: 0.5 }}>
                            {service.daysOfTheWeek?.map((day, i) => (
                              <Chip key={i} label={day} size="small" />
                            ))}
                          </Box>

                          <Typography variant="body2" fontWeight="bold" sx={{ mt: 1 }}>
                            Time:
                          </Typography>
                          <Typography variant="body2">{service.time}</Typography>

                          <Button fullWidth variant="contained" sx={{ mt: 2 }}>
                            RATINGS
                          </Button>
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              </CardContent>
            </Card>
          </>
        )}
      </Box>
    </Box>
  );
};

export default PortfolioPage;
