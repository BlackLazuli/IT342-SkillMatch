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
  Paper,
  useTheme,
  useMediaQuery,
  Stack,
  Container
} from "@mui/material";
import AppBar from "../../component/AppBar";
import { usePersonalInfo } from "../../context/PersonalInfoContext";
import {
  Edit,
  Add,
  Schedule,
  WorkOutline,
  Comment
} from "@mui/icons-material";

const baseUrl = "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080"; // Change to your EC2 public IP/DNS

const PortfolioPage = () => {
  const { userID } = useParams();
  const navigate = useNavigate();
  const [portfolio, setPortfolio] = useState(null);
  const [comments, setComments] = useState([]);
  const [rating, setRating] = useState(0);
  const [loading, setLoading] = useState(true);
  const [userDetails, setUserDetails] = useState(null);
  const { personalInfo } = usePersonalInfo();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      alert("Please log in first.");
      return;
    }

    const fetchPortfolio = async () => {
      try {
        const res = await fetch(`${baseUrl}/api/portfolios/${userID}`, {
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
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    const fetchComments = async (portfolioId) => {
      try {
        const res = await fetch(`${baseUrl}/api/comments/portfolio/${portfolioId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const data = await res.json();
        setComments(data);
    
        const avgRating = data.length
          ? data.reduce((acc, curr) => acc + curr.rating, 0) / data.length
          : 0;
        setRating(avgRating);
      } catch {
        setComments([]);
        setRating(0);
      }
    };

    const fetchUserDetails = async () => {
      try {
        const res = await fetch(`${baseUrl}/api/users/${userID}`, {
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
      return pic.startsWith("http") ? pic : `${baseUrl}${pic}`;
    }
    return "/default-avatar.png";
  };

  if (loading) {
    return (
      <Box sx={{ display: "flex" }}>
        <AppBar />
        <Box sx={{ 
          display: "flex", 
          justifyContent: "center", 
          alignItems: "center", 
          height: "80vh",
          width: "100%"
        }}>
          <CircularProgress size={60} />
        </Box>
      </Box>
    );
  }

  return (
    <Box sx={{ display: "flex", minHeight: "100vh" }}>
      <AppBar />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        {!portfolio ? (
          <Paper elevation={3} sx={{ p: 4, textAlign: "center" }}>
            <Typography variant="h4" gutterBottom>Portfolio</Typography>
            <Typography variant="body1" sx={{ mb: 3 }}>
              No portfolio found for this user.
            </Typography>
            {personalInfo?.userId === Number(userID) && (
              <Button 
                variant="contained" 
                startIcon={<Add />}
                onClick={handleAddPortfolio}
                sx={{ 
                  px: 4,
                  py: 1.5,
                  borderRadius: 2
                }}
              >
                Create Your Portfolio
              </Button>
            )}
          </Paper>
        ) : (
          <>
            {/* Header Section */}
            <Box sx={{ 
              display: "flex", 
              flexDirection: isMobile ? "column" : "row", 
              alignItems: isMobile ? "flex-start" : "center",
              gap: 3,
              mb: 4
            }}>
              <Avatar
                alt={userDetails?.firstName || "User"}
                src={getProfilePictureUrl()}
                sx={{ 
                  width: 100, 
                  height: 100, 
                  boxShadow: theme.shadows[4]
                }}
              />
              <Box sx={{ flex: 1 }}>
                <Typography variant="h3" fontWeight="bold">
                  {userDetails?.firstName || "User"}'s Portfolio
                </Typography>
                <Box sx={{ display: "flex", alignItems: "center", gap: 1, mt: 1 }}>
                  <Rating 
                    value={rating} 
                    precision={0.1} 
                    readOnly 
                    sx={{ 
                      color: theme.palette.warning.main,
                      fontSize: isMobile ? "1.5rem" : "1.75rem"
                    }} 
                  />
                  <Typography variant="h6" color="text.secondary">
                    {rating.toFixed(1)} ({comments.length} reviews)
                  </Typography>
                </Box>
              </Box>
              
              {personalInfo?.userId === Number(userID) && (
                <Button 
                  variant="contained" 
                  startIcon={<Edit />}
                  onClick={handleUpdatePortfolio}
                  sx={{ 
                    alignSelf: isMobile ? "flex-start" : "center",
                    px: 4,
                    py: 1.5,
                    borderRadius: 2
                  }}
                >
                  Edit Portfolio
                </Button>
              )}
            </Box>

            {/* About & Availability Section */}
            <Paper elevation={3} sx={{ p: 4, mb: 4, borderRadius: 2 }}>
              <Typography variant="h5" fontWeight="bold" gutterBottom sx={{ mb: 3 }}>
                About
              </Typography>
              <Typography variant="body1" paragraph sx={{ mb: 3 }}>
                {userDetails?.bio || "This user hasn't added a bio yet."}
              </Typography>

              <Divider sx={{ my: 3 }} />

              <Box sx={{ display: "flex", gap: 4, flexDirection: isMobile ? "column" : "row" }}>
                <Box sx={{ flex: 1 }}>
                  <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 2 }}>
                    <Schedule color="primary" />
                    <Typography variant="h6" fontWeight="bold">
                      Availability
                    </Typography>
                  </Box>
                  
                  <Typography variant="body1" fontWeight="medium" gutterBottom>
                    Days Available:
                  </Typography>
                  <Stack direction="row" spacing={1} sx={{ flexWrap: "wrap", gap: 1, mb: 2 }}>
                    {portfolio?.daysAvailable?.length > 0 ? (
                      portfolio.daysAvailable.map((day, index) => (
                        <Chip 
                          key={index} 
                          label={day} 
                          color="primary"
                          variant="outlined"
                        />
                      ))
                    ) : (
                      <Typography variant="body2">Not specified</Typography>
                    )}
                  </Stack>

                  <Typography variant="body1" fontWeight="medium" gutterBottom>
                    Hours:
                  </Typography>
                  <Typography variant="body1">
                    {portfolio?.time || "Not specified"}
                  </Typography>
                </Box>
              </Box>
            </Paper>

            {/* Services Section */}
            <Paper elevation={3} sx={{ p: 4, mb: 4, borderRadius: 2 }}>
              <Typography variant="h5" fontWeight="bold" gutterBottom sx={{ mb: 3 }}>
                Services Offered
              </Typography>
              
              {portfolio?.servicesOffered?.length > 0 ? (
                <Grid container spacing={3}>
                  {portfolio.servicesOffered.map((service, index) => (
                    <Grid item xs={12} sm={6} md={4} key={index}>
                      <Card sx={{ 
                        height: "100%",
                        transition: "transform 0.3s, box-shadow 0.3s",
                        "&:hover": {
                          transform: "translateY(-5px)",
                          boxShadow: theme.shadows[6]
                        }
                      }}>
                        <CardContent>
                          <Typography variant="h6" fontWeight="bold" gutterBottom>
                            {service.name}
                          </Typography>
                          <Typography variant="body2" color="text.secondary" paragraph>
                            {service.description || "No description provided."}
                          </Typography>
                          
                          <Box sx={{ 
                            backgroundColor: theme.palette.grey[100], 
                            p: 2, 
                            borderRadius: 1,
                            mt: 2
                          }}>
                            <Typography variant="subtitle2" fontWeight="bold">
                              Pricing:
                            </Typography>
                            <Typography variant="body1">
                              {service.pricing || "Not specified"}
                            </Typography>
                          </Box>
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              ) : (
                <Typography variant="body1">
                  No services listed yet.
                </Typography>
              )}
            </Paper>

            {/* Comments Section */}
            <Paper elevation={3} sx={{ p: 4, borderRadius: 2 }}>
              <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 3 }}>
                <Comment color="primary" />
                <Typography variant="h5" fontWeight="bold">
                  Customer Reviews
                </Typography>
              </Box>
              
              {comments.length === 0 ? (
                <Typography variant="body1" sx={{ textAlign: "center", py: 4 }}>
                  No reviews yet. Be the first to leave a review!
                </Typography>
              ) : (
                <Stack spacing={3}>
                  {comments.map((comment, index) => (
                    <Card key={index} elevation={0} sx={{ 
                      backgroundColor: theme.palette.grey[50],
                      borderRadius: 2
                    }}>
                      <CardContent>
                        <Box sx={{ display: "flex", gap: 2, mb: 2 }}>
                          <Avatar
                            alt={comment.authorName || "Anonymous"}
                            src={
                              comment.profilePicture
                                ? comment.profilePicture.startsWith("http")
                                  ? comment.profilePicture
                                  : `${baseUrl}${comment.profilePicture}`
                                : "/default-avatar.png"
                            }
                            sx={{ width: 48, height: 48 }}
                          />
                          <Box>
                            <Typography variant="subtitle1" fontWeight="bold">
                              {comment.authorName || "Anonymous"}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              {new Date(comment.timestamp).toLocaleDateString('en-US', {
                                year: 'numeric',
                                month: 'long',
                                day: 'numeric'
                              })}
                            </Typography>
                          </Box>
                        </Box>
                        
                        <Rating
                          value={comment.rating}
                          precision={0.1}
                          readOnly
                          sx={{ 
                            color: theme.palette.warning.main,
                            mb: 1
                          }}
                        />
                        
                        <Typography variant="body1">
                          {comment.message}
                        </Typography>
                      </CardContent>
                    </Card>
                  ))}
                </Stack>
              )}
            </Paper>
          </>
        )}
      </Container>
    </Box>
  );
};

export default PortfolioPage;