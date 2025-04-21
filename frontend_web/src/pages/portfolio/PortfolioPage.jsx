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
  Chip,
  Divider,
  Rating,
} from "@mui/material";
import AppBar from "../../component/AppBar";
import { usePersonalInfo } from "../../context/PersonalInfoContext";

const PortfolioPage = () => {
  const { userID } = useParams();
  const navigate = useNavigate();
  const [portfolio, setPortfolio] = useState(null);
  const [comments, setComments] = useState([]);
  const [rating, setRating] = useState(0);
  const [loading, setLoading] = useState(true);
  const [userDetails, setUserDetails] = useState(null);
  const { personalInfo } = usePersonalInfo();

  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      alert("Please log in first.");
      return;
    }

    const fetchPortfolio = async () => {
      try {
        const res = await fetch(`http://localhost:8080/api/portfolios/${userID}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (res.status === 404) {
          setPortfolio(null);
          setLoading(false);
          return;
        }

        if (!res.ok) throw new Error("Failed to fetch portfolio.");

        const data = await res.json();
        setPortfolio(data);
        fetchComments(data.id);
        fetchRatings(userID);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    const fetchComments = async (portfolioId) => {
      try {
        const res = await fetch(`http://localhost:8080/api/comments/portfolio/${portfolioId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const data = await res.json();
        setComments(data);
      } catch {
        setComments([]);
      }
    };

    const fetchRatings = async (userId) => {
      try {
        const res = await fetch(`http://localhost:8080/api/ratings/user/${userId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (res.ok) {
          const ratingsData = await res.json();
          const avg = ratingsData.length
            ? ratingsData.reduce((acc, curr) => acc + curr.rating, 0) / ratingsData.length
            : 0;
          setRating(avg);
        }
      } catch (err) {
        console.error("Failed to fetch ratings:", err);
        setRating(0);
      }
    };

    const fetchUserDetails = async () => {
      try {
        const res = await fetch(`http://localhost:8080/api/users/${userID}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (res.ok) {
          const data = await res.json();
          setUserDetails(data);
        }
      } catch (err) {
        console.error("Failed to fetch user details:", err);
      }
    };

    fetchPortfolio();
    fetchUserDetails();
  }, [userID]);

  const handleAddPortfolio = () => navigate(`/add-portfolio/${userID}`);
  const handleUpdatePortfolio = () => navigate(`/edit-portfolio/${userID}`);

  const getProfilePictureUrl = () => {
    const pic = userDetails?.profilePicture || personalInfo?.profilePicture;
    if (pic) {
      return pic.startsWith("http") ? pic : `http://localhost:8080${pic}`;
    }
    return "/default-avatar.png";
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
            <Typography variant="h4" gutterBottom>Portfolio</Typography>
            <Typography>No portfolio found for this user.</Typography>
            <Button variant="contained" sx={{ mt: 2 }} onClick={handleAddPortfolio}>
              Add Portfolio
            </Button>
          </>
        ) : (
          <>
            {/* Header */}
            <Box sx={{ display: "flex", alignItems: "center", mb: 2 }}>
              <Avatar
                alt={userDetails?.firstName || "User"}
                src={getProfilePictureUrl()}
                sx={{ width: 80, height: 80, mr: 2 }}
              />
              <Box>
                <Typography variant="h4" fontWeight="bold">
                  {userDetails?.firstName || "User"}'s Portfolio
                </Typography>
                <Box sx={{ display: "flex", alignItems: "center", gap: 1, mt: 0.5 }}>
                  <Rating value={rating} precision={0.1} readOnly size="small" sx={{ color: "#ffb400" }} />
                  <Typography variant="body2" color="text.secondary">
                    {rating.toFixed(1)} Stars
                  </Typography>
                </Box>
              </Box>
            </Box>

            {personalInfo?.userId === userID && (
              <Button variant="contained" sx={{ mb: 4 }} onClick={handleUpdatePortfolio}>
                Update Portfolio
              </Button>
            )}

            {/* Portfolio Content */}
            <Card sx={{ backgroundColor: "#fff4e6", mb: 4 }}>
              <CardContent>
                <Typography variant="h5" fontWeight="bold" gutterBottom>
                  Work Experience
                </Typography>
                <Typography variant="body2" gutterBottom>
                  {portfolio?.workExperience || "Not provided."}
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

                          <Typography variant="body2" fontWeight="bold">Pricing:</Typography>
                          <Typography variant="body2">{service.pricing}</Typography>

                          <Typography variant="body2" fontWeight="bold" sx={{ mt: 1 }}>
                            Days Available:
                          </Typography>
                          <Box sx={{ display: "flex", flexWrap: "wrap", gap: 0.5 }}>
                            {service.daysOfTheWeek?.map((day, i) => (
                              <Chip key={i} label={day} size="small" />
                            )) || "N/A"}
                          </Box>

                          <Typography variant="body2" fontWeight="bold" sx={{ mt: 1 }}>
                            Time:
                          </Typography>
                          <Typography variant="body2">{service.time}</Typography>

                          <Button fullWidth variant="contained" sx={{ mt: 2 }}>
                            View Ratings
                          </Button>
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              </CardContent>
            </Card>

            {/* Comments */}
            <Box>
  <Divider sx={{ mb: 2 }} />
  <Typography variant="h5" fontWeight="bold" gutterBottom>
    Comments
  </Typography>
  {comments.length === 0 ? (
    <Typography>No Comments</Typography>
  ) : (
    comments.map((comment, index) => (
      <Card key={index} sx={{ mb: 2, backgroundColor: "#f9f9f9" }}>
        <CardContent sx={{ display: "flex", alignItems: "flex-start" }}>
          <Avatar
            alt={comment.authorName || "Anonymous"}
            src={comment.authorProfilePicture ? `http://localhost:8080${comment.authorProfilePicture}` : "/default-avatar.png"}
            sx={{ width: 40, height: 40, mr: 2 }}
          />
          <Box sx={{ flex: 1 }}>
            <Typography variant="body1" fontWeight="bold" gutterBottom>
              {comment.authorName || "Anonymous"}
            </Typography>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 1 }}>
              <Rating value={comment.authorRating} precision={0.1} readOnly size="small" sx={{ color: "#ffb400" }} />
            </Box>
            <Typography variant="body2">
              {comment.message}
            </Typography>
          </Box>
        </CardContent>
      </Card>
    ))
  )}
</Box>



          </>
        )}
      </Box>
    </Box>
  );
};

export default PortfolioPage;