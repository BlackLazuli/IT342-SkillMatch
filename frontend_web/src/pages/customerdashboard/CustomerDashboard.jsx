import React, { useEffect, useState } from "react";
import axios from "axios";
import AppBar from "../../component/AppBarCustomer";
import {
  Box,
  Typography,
  Card,
  CardContent,
  Grid,
  Avatar,
  Button,
  Rating,
} from "@mui/material";

const drawerWidth = 240;

const ProviderDashboard = () => {
  const [portfolios, setPortfolios] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchPortfolios = async () => {
      try {
        const token = localStorage.getItem("token");

        const response = await axios.get("http://localhost:8080/api/portfolios/getAllPortfolios", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        setPortfolios(response.data);
      } catch (err) {
        console.error("Error fetching portfolios:", err);
        setError("Failed to fetch portfolios.");
      }
    };

    fetchPortfolios();
  }, []);

  const getProfilePictureUrl = (user) => {
    if (!user?.profilePicture) return "/default-avatar.png";
    return user.profilePicture.startsWith("http")
      ? user.profilePicture
      : `http://localhost:8080${user.profilePicture}`;
  };

  return (
    <Box sx={{ display: "flex" }}>
      <AppBar />
      <Box sx={{ flexGrow: 1, p: 3, ml: `${drawerWidth}px` }}>
        <Typography variant="h4" fontWeight="bold" gutterBottom>
          Professionals Near You
        </Typography>

        {error && (
          <Typography color="error" mb={2}>
            {error}
          </Typography>
        )}

        <Grid container spacing={4}>
          {portfolios.flatMap((portfolio) =>
            portfolio.servicesOffered?.map((service, index) => (
              <Grid item xs={12} sm={6} md={4} key={`${portfolio.id}-${index}`}>
                <Card
                  elevation={3}
                  sx={{
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    backgroundColor: "#e0f7fa",
                    borderRadius: 2,
                    padding: 2,
                    textAlign: "center",
                  }}
                >
                  <Avatar
                    src={getProfilePictureUrl(portfolio.user)}
                    alt={portfolio.user?.firstName || "User"}
                    sx={{ width: 100, height: 100, mb: 1 }}
                  />
                  <Typography variant="h6" fontWeight="bold">
                    {portfolio.user?.firstName || "Unknown"} {portfolio.user?.lastName || ""}
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    {service.name}
                  </Typography>

                  <Box sx={{ mt: 1, mb: 1 }}>
                    <Rating value={5} readOnly size="small" />
                  </Box>

                  <Typography variant="body2">
                    <strong>Price:</strong> {service.pricing}
                  </Typography>

                  <Button
                      variant="contained"
                      sx={{
                        mt: 2,
                        backgroundColor: "#607d8b",
                        ":hover": { backgroundColor: "#455a64" },
                        color: "white",
                        fontWeight: "bold",
                      }}
                      onClick={() =>
                        window.location.href = `/provider-portfolio/${portfolio.user.id}`
                      }
                    >
                      MORE
                    </Button>

                                    </Card>
                                  </Grid>
                                ))
                              )}
                            </Grid>
                          </Box>
                        </Box>
                      );
                    };

export default ProviderDashboard;
