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
} from "@mui/material";
import { CalendarMonth, AccessTime } from "@mui/icons-material";
import { useContext } from "react";
import { usePersonalInfo } from "../../context/PersonalInfoContext";

const drawerWidth = 240;

const ProviderDashboard = () => {
  const [portfolios, setPortfolios] = useState([]);
  const [ratings, setRatings] = useState({});
  const [error, setError] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [userLocations, setUserLocations] = useState({}); // ⭐ New
  const [distanceFilter, setDistanceFilter] = useState(5); // ⭐ New (5 km default)
  const [currentUserLocation, setCurrentUserLocation] = useState(null); // for logged-in user's lat/lng
  const { personalInfo } = usePersonalInfo();
  const userId = personalInfo?.userId;
  const baseUrl = "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080"; // Change to your EC2 public IP/DNS

  
  useEffect(() => {
    const fetchPortfolios = async () => {
      try {
        const token = localStorage.getItem("token");
    
        const response = await axios.get("/api/portfolios/getAllPortfolios", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
    
        setPortfolios(response.data);
    
        const locations = {};
    
        for (const portfolio of response.data) {
          await fetchAverageRating(portfolio.id);
    
          // Fetch provider's address
          if (portfolio.user?.id) {
            try {
              const locationRes = await axios.get(
                `/api/locations/${portfolio.user.id}`,
                {
                  headers: { Authorization: `Bearer ${token}` },
                }
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
    

    const calculateDistance = (lat1, lon1, lat2, lon2) => {
      const toRad = (value) => (value * Math.PI) / 180;
      const R = 6371; // Earth radius in km
    
      const dLat = toRad(lat2 - lat1);
      const dLon = toRad(lon2 - lon1);
    
      const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(toRad(lat1)) *
          Math.cos(toRad(lat2)) *
          Math.sin(dLon / 2) *
          Math.sin(dLon / 2);
    
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      const distance = R * c;
    
      return distance; // in km
    };
    const fetchCurrentUserLocation = async () => {
      if (!userId) {
        console.error("User ID not available in context");
        return;
      }
    
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

    const fetchUserLocation = async (userId) => { // ⭐ New
      const token = localStorage.getItem("token");
      try {
        const response = await axios.get(`/api/locations/${userId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (response.data) {
          setUserLocations((prev) => ({
            ...prev,
            [userId]: {
              latitude: Number(response.data.latitude),
              longitude: Number(response.data.longitude),
            },
          }));
        }
      } catch (error) {
        console.error(`Error fetching location for user ${userId}:`, error);
      }
    };

    fetchPortfolios();
    fetchCurrentUserLocation(); // ✅ Only this
  }, []);

  // ⭐ New function
  const calculateDistance = (lat1, lon1, lat2, lon2) => {
    const toRad = (value) => (value * Math.PI) / 180;
    const R = 6371; // Radius of Earth in km
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c; // Distance in km
  };

  const getProfilePictureUrl = (user) => {
    if (!user?.profilePicture) return "/default-avatar.png";
    return user.profilePicture.startsWith("http") 
      ? user.profilePicture  // Use as-is if full HTTPS URL
      : user.profilePicture; // Assume backend returns "/uploads/..." (relative path)
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

        {/* ⭐ New: Distance filter */}
        <Box my={2}>
          <Typography gutterBottom>Filter by distance (km)</Typography>
          <Slider
            value={distanceFilter}
            min={1}
            max={50}
            step={1}
            valueLabelDisplay="auto"
            onChange={(e, newValue) => setDistanceFilter(newValue)}
          />
        </Box>

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

  {/* ⭐ Distance shown here */}
  {currentUserLocation && userLocations[user.id] && (
  <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
    {`${calculateDistance(
      currentUserLocation.lat,
      currentUserLocation.lng,
      userLocations[user.id].latitude,
      userLocations[user.id].longitude
    ).toFixed(2)} km away`}
  </Typography>
)}


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
  {currentUserLocation && userLocations[user.id] && (
  <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
    {`${calculateDistance(
      currentUserLocation.lat,
      currentUserLocation.lng,
      userLocations[user.id].latitude,
      userLocations[user.id].longitude
    ).toFixed(2)} km away`}
  </Typography>
)}


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
