import React, { useEffect, useState } from "react";
import axios from "axios";
import AppBar from "../../component/AppBarCustomer";
import {
  Box,
  Typography,
  Card,
  Grid,
  Avatar,
  Button,
  Rating,
  TextField,
} from "@mui/material";
import { CalendarMonth, AccessTime } from "@mui/icons-material";

const drawerWidth = 240;

const ProviderDashboard = () => {
  const [portfolios, setPortfolios] = useState([]);
  const [ratings, setRatings] = useState({});
  const [error, setError] = useState("");
  const [searchQuery, setSearchQuery] = useState("");

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
        console.log(response.data); // inside fetchPortfolios

        for (const portfolio of response.data) {
          await fetchAverageRating(portfolio.id);
        }
      } catch (err) {
        console.error("Error fetching portfolios:", err);
        setError("Failed to fetch portfolios.");
      }
    };

    const fetchAverageRating = async (portfolioId) => {
      const token = localStorage.getItem("token");
      try {
        const response = await axios.get(
          `http://localhost:8080/api/comments/portfolio/${portfolioId}`,
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );

        const comments = response.data;
        const avg =
          comments.length > 0
            ? comments.reduce((acc, c) => acc + (c.rating || 0), 0) / comments.length
            : 0;

        setRatings((prev) => ({ ...prev, [portfolioId]: avg }));
      } catch (err) {
        console.error(`Error fetching comments for portfolio ${portfolioId}:`, err);
        setRatings((prev) => ({ ...prev, [portfolioId]: 0 }));
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

  const filteredPortfolios = portfolios.filter((portfolio) =>
    portfolio.workExperience
      ?.toLowerCase()
      .includes(searchQuery.toLowerCase()) // Search using workExperience string
  );

  return (
    <Box sx={{ display: "flex" }}>
      <AppBar />
      <Box sx={{ flexGrow: 1, p: 3 }}>
        <Typography variant="h4" fontWeight="bold" gutterBottom>
          Professionals Near You
        </Typography>

        <TextField
          label="Search by job title"
          variant="outlined"
          fullWidth
          margin="normal"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />

        {error && (
          <Typography color="error" mb={2}>
            {error}
          </Typography>
        )}

        <Grid container spacing={4}>
          {filteredPortfolios.map((portfolio) => {
            const { id: portfolioId, user, workExperience, daysAvailable, startTime, endTime } = portfolio;
            return (
              <Grid item xs={12} sm={6} md={4} key={portfolioId}>
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
                    src={getProfilePictureUrl(user)}
                    alt={user?.firstName || "User"}
                    sx={{ width: 100, height: 100, mb: 1 }}
                  />
                  <Typography variant="h6" fontWeight="bold">
                    {user?.firstName || "Unknown"} {user?.lastName || ""}
                  </Typography>

                  <Box sx={{ mt: 1, mb: 1 }}>
                    <Rating
                      value={ratings[portfolioId] || 0}
                      precision={0.1}
                      readOnly
                      size="small"
                    />
                  </Box>
                  <Typography variant="h6">
                    <strong>{workExperience || "Not specified"}</strong>
                  </Typography>
                  {/* Days and Time */}
                  <Box display="flex" flexDirection="column" gap={0.5} mt={1} mb={1}>
                    <Box display="flex" alignItems="center" gap={1}>
                      <CalendarMonth fontSize="small" color="action" />
                      <Typography variant="body2" color="text.secondary">
                        {portfolio.daysAvailable?.join(", ") || "N/A"}
                      </Typography>
                    </Box>
                    <Box display="flex" alignItems="center" gap={1}>
                      <AccessTime fontSize="small" color="action" />
                      <Typography variant="body2" color="text.secondary">
                      {portfolio.time || "Not specified"}
                      </Typography>
                    </Box>
                  </Box>
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
                      (window.location.href = `/provider-portfolio/${user.id}`)
                    }
                  >
                    MORE
                  </Button>
                </Card>
              </Grid>
            );
          })}
        </Grid>
      </Box>
    </Box>
  );
};

export default ProviderDashboard;
