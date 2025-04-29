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
  Slider,
  Chip,
  CardContent,
  CardActions,
  useTheme,
} from "@mui/material";
import { CalendarMonth, AccessTime, LocationOn } from "@mui/icons-material";
import { usePersonalInfo } from "../../context/PersonalInfoContext";

const ProviderDashboard = () => {
  const theme = useTheme();
  const [portfolios, setPortfolios] = useState([]);
  const [ratings, setRatings] = useState({});
  const [error, setError] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [userLocations, setUserLocations] = useState({});
  const [distanceFilter, setDistanceFilter] = useState(5);
  const [currentUserLocation, setCurrentUserLocation] = useState(null);
  const { personalInfo } = usePersonalInfo();
  const userId = personalInfo?.userId;

  useEffect(() => {
    const fetchPortfolios = async () => {
      try {
        const token = localStorage.getItem("token");
        const response = await axios.get("/api/portfolios/getAllPortfolios", {
          headers: { Authorization: `Bearer ${token}` },
        });
        setPortfolios(response.data);

        const locations = {};
        for (const portfolio of response.data) {
          await fetchAverageRating(portfolio.id);
          if (portfolio.user?.id) {
            try {
              const locationRes = await axios.get(
                `/api/locations/${portfolio.user.id}`,
                { headers: { Authorization: `Bearer ${token}` } }
              );
              if (locationRes.data) {
                locations[portfolio.user.id] = {
                  latitude: Number(locationRes.data.latitude),
                  longitude: Number(locationRes.data.longitude),
                };
              }
            } catch (locErr) {
              console.error(`Error fetching location for user ${portfolio.user.id}:`, locErr);
            }
          }
        }
        setUserLocations(locations);
      } catch (err) {
        console.error("Error fetching portfolios:", err);
        setError("Failed to fetch portfolios.");
      }
    };

    const fetchCurrentUserLocation = async () => {
      if (!userId) return;
      try {
        const token = localStorage.getItem("token");
        const response = await axios.get(`/api/locations/${userId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (response.data) {
          setCurrentUserLocation({
            lat: Number(response.data.latitude),
            lng: Number(response.data.longitude),
          });
        }
      } catch (err) {
        console.error("Error fetching current user location:", err);
      }
    };

    const fetchAverageRating = async (portfolioId) => {
      const token = localStorage.getItem("token");
      try {
        const response = await axios.get(
          `/api/comments/portfolio/${portfolioId}`,
          { headers: { Authorization: `Bearer ${token}` } }
        );
        const comments = response.data;
        const avg = comments.length > 0
          ? comments.reduce((acc, c) => acc + (c.rating || 0), 0) / comments.length
          : 0;
        setRatings((prev) => ({ ...prev, [portfolioId]: avg }));
      } catch (err) {
        console.error(`Error fetching comments for portfolio ${portfolioId}:`, err);
        setRatings((prev) => ({ ...prev, [portfolioId]: 0 }));
      }
    };

    fetchPortfolios();
    fetchCurrentUserLocation();
  }, [userId]);

  const calculateDistance = (lat1, lon1, lat2, lon2) => {
    const toRad = (value) => (value * Math.PI) / 180;
    const R = 6371;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  };

  const getProfilePictureUrl = (user) => {
    if (!user?.profilePicture) return "/default-avatar.png";
    return user.profilePicture.startsWith("http") 
      ? user.profilePicture
      : user.profilePicture;
  };

  const filteredPortfolios = portfolios.filter((portfolio) => {
    const matchesWorkExperience = portfolio.workExperience
      ?.toLowerCase()
      .includes(searchQuery.toLowerCase());
  
    if (!currentUserLocation) return matchesWorkExperience;
  
    const providerLocation = userLocations[portfolio.user.id];
    if (!providerLocation) return matchesWorkExperience;
  
    const distance = calculateDistance(
      currentUserLocation.lat,
      currentUserLocation.lng,
      providerLocation.latitude,
      providerLocation.longitude
    );
    return matchesWorkExperience && distance <= distanceFilter;
  });

  return (
    <Box sx={{ display: "flex", minHeight: "100vh" }}>
      <AppBar />
      <Box sx={{ flexGrow: 1, p: 3, maxWidth: 'calc(100% - 240px)' }}>
        <Typography variant="h4" fontWeight="bold" gutterBottom>
          Professionals Near You
        </Typography>

        <Box sx={{ 
          display: 'flex', 
          gap: 2, 
          mb: 3,
          [theme.breakpoints.down('sm')]: {
            flexDirection: 'column'
          }
        }}>
          <TextField
            label="Search by job title"
            variant="outlined"
            fullWidth
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            sx={{
              backgroundColor: 'background.paper',
              borderRadius: 1
            }}
          />
          
          <Box sx={{ width: '100%', maxWidth: 300 }}>
            <Typography variant="body2" gutterBottom>
              Filter by distance: <strong>{distanceFilter} km</strong>
            </Typography>
            <Slider
              value={distanceFilter}
              min={1}
              max={50}
              step={1}
              valueLabelDisplay="auto"
              onChange={(e, newValue) => setDistanceFilter(newValue)}
              sx={{
                color: theme.palette.primary.main,
              }}
            />
          </Box>
        </Box>

        {error && (
          <Typography color="error" mb={2}>
            {error}
          </Typography>
        )}

        <Grid container spacing={3}>
          {filteredPortfolios.map((portfolio) => {
            const { id: portfolioId, user, workExperience, daysAvailable, time } = portfolio;
            const distance = currentUserLocation && userLocations[user.id] 
              ? calculateDistance(
                  currentUserLocation.lat,
                  currentUserLocation.lng,
                  userLocations[user.id].latitude,
                  userLocations[user.id].longitude
                ).toFixed(2)
              : null;

            return (
              <Grid item xs={12} sm={6} md={4} key={portfolioId}>
                <Card sx={{ 
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  transition: 'transform 0.3s, box-shadow 0.3s',
                  '&:hover': {
                    transform: 'translateY(-5px)',
                    boxShadow: theme.shadows[6]
                  }
                }}>
                  <CardContent sx={{ 
                    flexGrow: 1,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    textAlign: 'center',
                    p: 3
                  }}>
                    <Avatar
                      src={getProfilePictureUrl(user)}
                      alt={user?.firstName || "User"}
                      sx={{ 
                        width: 120, 
                        height: 120, 
                        mb: 2,
                        border: `3px solid ${theme.palette.primary.main}`
                      }}
                    />
                    
                    <Typography variant="h6" fontWeight="bold" gutterBottom>
                      {user?.firstName || "Unknown"} {user?.lastName || ""}
                    </Typography>

                    {distance && (
                      <Chip
                        icon={<LocationOn fontSize="small" />}
                        label={`${distance} km away`}
                        size="small"
                        sx={{ 
                          mb: 1,
                          backgroundColor: theme.palette.grey[100]
                        }}
                      />
                    )}

                    <Box sx={{ my: 1 }}>
                      <Rating
                        value={ratings[portfolioId] || 0}
                        precision={0.1}
                        readOnly
                      />
                    </Box>

                    <Typography variant="subtitle1" color="primary" gutterBottom>
                      <strong>{workExperience || "Not specified"}</strong>
                    </Typography>

                    <Box sx={{ 
                      width: '100%',
                      mt: 2,
                      p: 1,
                      backgroundColor: theme.palette.grey[50],
                      borderRadius: 1
                    }}>
                      <Box display="flex" alignItems="center" gap={1} mb={1}>
                        <CalendarMonth fontSize="small" color="primary" />
                        <Typography variant="body2">
                          {daysAvailable?.join(", ") || "N/A"}
                        </Typography>
                      </Box>
                      <Box display="flex" alignItems="center" gap={1}>
                        <AccessTime fontSize="small" color="primary" />
                        <Typography variant="body2">
                          {time || "Not specified"}
                        </Typography>
                      </Box>
                    </Box>
                  </CardContent>

                  <CardActions sx={{ justifyContent: 'center', p: 2 }}>
                    <Button
                      variant="contained"
                      fullWidth
                      sx={{
                        py: 1,
                        fontWeight: 'bold',
                        letterSpacing: 0.5
                      }}
                      onClick={() => window.location.href = `/provider-portfolio/${user.id}`}
                    >
                      View Profile
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            );
          })}
        </Grid>

        {filteredPortfolios.length === 0 && (
          <Box sx={{ 
            display: 'flex', 
            justifyContent: 'center', 
            alignItems: 'center', 
            height: '50vh'
          }}>
            <Typography variant="h6" color="text.secondary">
              No professionals found matching your criteria
            </Typography>
          </Box>
        )}
      </Box>
    </Box>
  );
};

export default ProviderDashboard;