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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from "@mui/material";
import AppBar from "../../component/AppBar";
import { usePersonalInfo } from "../../context/PersonalInfoContext";

const ProviderPortfolioPage = () => {
  const { userID } = useParams();
  const navigate = useNavigate();
  const [portfolio, setPortfolio] = useState(null);
  const [comments, setComments] = useState([]);
  const [rating, setRating] = useState(0);
  const [loading, setLoading] = useState(true);
  const [userDetails, setUserDetails] = useState(null);
  const [commentModalOpen, setCommentModalOpen] = useState(false);
  const [newComment, setNewComment] = useState("");
  const [newRating, setNewRating] = useState(0);
  const { personalInfo } = usePersonalInfo();
  const token = localStorage.getItem("token");

  // Fetch portfolio data
  const fetchPortfolio = async () => {
    const token = localStorage.getItem("token");

    if (!token) {
      alert("Please log in first.");
      return;
    }

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
      fetchComments(data.id);  // Fetch comments when portfolio is loaded
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  // Fetch comments for a portfolio
  const fetchComments = async (portfolioId) => {
    try {
      const res = await fetch(`http://localhost:8080/api/comments/portfolio/${portfolioId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await res.json();
      setComments(data);

      // Calculate the average rating from comments
      const avgRating = data.length
        ? data.reduce((acc, curr) => acc + curr.rating, 0) / data.length
        : 0;

      setRating(avgRating); // Set the average rating
    } catch {
      setComments([]);
      setRating(0); // Default to 0 if there's an error
    }
  };

  // Fetch user details
  const fetchUserDetails = async () => {
    const token = localStorage.getItem("token");
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

  useEffect(() => {
    fetchPortfolio();
    fetchUserDetails();
  }, [userID]);

  // Get the profile picture URL
  const getProfilePictureUrl = () => {
    const pic = userDetails?.profilePicture || personalInfo?.profilePicture;
    if (pic) {
      return pic.startsWith("http") ? pic : `http://localhost:8080${pic}`;
    }
    return "/default-avatar.png";
  };

  // Handle feedback submission (comment + rating)
  const handleSubmitFeedback = async () => {
    const token = localStorage.getItem("token");
  
    try {
      // Submit comment with rating
      await fetch(
        `http://localhost:8080/api/comments/${personalInfo.userId}/${portfolio.id}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            message: newComment,
            rating: newRating,
          }),
        }
      );
  
      // Submit rating (for the separate ratings table)
      await fetch(`http://localhost:8080/api/ratings/`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          user: { id: personalInfo.userId },
          rating: newRating,
          review: newComment,
        }),
      });
  
      // Reset form
      setCommentModalOpen(false);
      setNewComment("");
      setNewRating(0);
  
      // Refresh both
      fetchComments(portfolio.id);
    } catch (error) {
      console.error("Failed to submit feedback", error);
    }
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

            {/* Add Comment Button */}
            <Button
              variant="contained"
              sx={{ mb: 2 }}
              onClick={() => setCommentModalOpen(true)}
            >
              Add a Comment
            </Button>

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
                        src={
                          comment.profilePicture
                            ? comment.profilePicture.startsWith("http")
                              ? comment.profilePicture
                              : `http://localhost:8080${comment.profilePicture}`
                            : "/default-avatar.png"
                        }
                        sx={{ width: 40, height: 40, mr: 2 }}
                      />
                      <Box sx={{ flex: 1 }}>
                        <Typography variant="body1" fontWeight="bold" gutterBottom>
                          {comment.authorName || "Anonymous"}
                        </Typography>
                        <Typography variant="caption" color="text.secondary" gutterBottom>
                          {new Date(comment.timestamp).toLocaleString()}
                        </Typography>

                        <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 1 }}>
                          <Rating
                            value={comment.rating}
                            precision={0.1}
                            readOnly
                            size="small"
                            sx={{ color: "#ffb400" }}
                          />
                          <Typography variant="body2" color="text.secondary">
                            {comment.rating.toFixed(1)}
                          </Typography>
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

        {/* Comment Modal */}
        <Dialog open={commentModalOpen} onClose={() => setCommentModalOpen(false)}>
          <DialogTitle>Add Comment</DialogTitle>
          <DialogContent>
            <TextField
              label="Comment"
              variant="outlined"
              multiline
              fullWidth
              rows={4}
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
            />
            <Rating
              value={newRating}
              onChange={(event, newValue) => setNewRating(newValue)}
              sx={{ mt: 2 }}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setCommentModalOpen(false)} color="primary">
              Cancel
            </Button>
            <Button onClick={handleSubmitFeedback} color="primary">
              Submit
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    </Box>
  );
};

export default ProviderPortfolioPage;
