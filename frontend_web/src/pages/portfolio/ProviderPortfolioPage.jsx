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
  Paper,
  useTheme,
  useMediaQuery,
  Stack,
  Container
} from "@mui/material";
import AppBar from "../../component/AppBarCustomer";
import { usePersonalInfo } from "../../context/PersonalInfoContext";
import {
  Edit,
  Add,
  Schedule,
  WorkOutline,
  Comment,
  Event
} from "@mui/icons-material";
const baseUrl = "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080"; // Change to your EC2 public IP/DNS

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
  const [appointmentModalOpen, setAppointmentModalOpen] = useState(false);
  const [appointmentDateTime, setAppointmentDateTime] = useState("");
  const [appointmentNotes, setAppointmentNotes] = useState("");
  const { personalInfo } = usePersonalInfo();
  const token = localStorage.getItem("token");
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));
  const [profilePictureUrl, setProfilePictureUrl] = useState("/default-avatar.png");
  const [selectedService, setSelectedService] = useState(null); // Store the entire service object
// Helper to check if selected time is within available hours
const isTimeInRange = (selectedDateTime, startTime, endTime) => {
  if (!selectedDateTime || !startTime || !endTime) return false;
  
  const selectedDate = new Date(selectedDateTime);
  const selectedHours = selectedDate.getHours();
  const selectedMinutes = selectedDate.getMinutes();

  const [startHours, startMinutes] = startTime.split(':').map(Number);
  const [endHours, endMinutes] = endTime.split(':').map(Number);

  const selectedTotalMinutes = selectedHours * 60 + selectedMinutes;
  const startTotalMinutes = startHours * 60 + startMinutes;
  const endTotalMinutes = endHours * 60 + endMinutes;

  return selectedTotalMinutes >= startTotalMinutes && 
         selectedTotalMinutes <= endTotalMinutes;
};

// Helper to get minimum datetime for input (current datetime)
const getMinDateTime = () => {
  const now = new Date();
  // Format as YYYY-MM-DDTHH:MM (browser datetime-local input format)
  return now.toISOString().slice(0, 16);
};

// Helper to check if selected date is an available day
const isAvailableDay = (selectedDateTime, availableDays) => {
  if (!selectedDateTime || !availableDays?.length) return false;
  const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  const selectedDay = dayNames[new Date(selectedDateTime).getDay()];
  return availableDays.includes(selectedDay) || availableDays.includes('Everyday');
};

  const formatTime = (time) => {
    if (!time) return "";
    const [hours, minutes] = time.split(':');
    const hour = parseInt(hours);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const hour12 = hour % 12 || 12;
    return `${hour12}:${minutes} ${ampm}`;
  };


  const handleSubmitAppointment = async () => {
    console.log("[1] Starting appointment submission...");
    
    if (!appointmentDateTime) {
      console.error("[Validation] No appointment date/time selected");
      return alert("Please select a date and time.");
    }
  
    if (!selectedService) {
      console.error("[Validation] No service selected");
      return alert("Please select a service.");
    }
  
    // Validate day availability
    if (!isAvailableDay(appointmentDateTime, portfolio?.daysAvailable)) {
      console.error("[Validation] Provider not available on selected day");
      return alert("Provider is not available on the selected day.");
    }
  
    // Validate time range
    if (!isTimeInRange(appointmentDateTime, portfolio?.startTime, portfolio?.endTime)) {
      console.error("[Validation] Time outside working hours");
      return alert("Please select a time within the provider's working hours.");
    }
  
    const requestData = {
      user: { id: personalInfo.userId },
      role: "CUSTOMER",
      portfolio: { id: portfolio.id },
      service: { id: selectedService.id },
      appointmentTime: appointmentDateTime,
      notes: appointmentNotes,
    };
  
    console.log("[2] Request payload:", JSON.stringify(requestData, null, 2));
    console.log("[3] Selected service details:", selectedService);
    console.log("[4] Auth token:", token ? "Present" : "Missing");
  
    try {
      console.log("[5] Sending request to /api/appointments/");
      
      const res = await fetch("/api/appointments/", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(requestData),
      });
  
      console.log("[6] Response status:", res.status);
      
      if (!res.ok) {
        const errorResponse = await res.text();
        console.error("[7] Error response:", errorResponse);
        throw new Error(`Failed to book appointment: ${res.status}`);
      }
  
      const data = await res.json();
      console.log("[8] Success response:", data);
      
      setAppointmentModalOpen(false);
      setAppointmentDateTime("");
      setAppointmentNotes("");
      setSelectedService(null);
      
      console.log("[9] Appointment booked successfully!");
      alert("Appointment booked successfully!");
      
    } catch (error) {
      console.error("[10] Appointment booking failed:", error);
      alert("Failed to book appointment. Please check console for details.");
    }
  };
  
  // Fetch portfolio data
  const fetchPortfolio = async () => {
    try {
      const res = await fetch(`/api/portfolios/${userID}`, {
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

  // Fetch comments for a portfolio
  const fetchComments = async (portfolioId) => {
    try {
      const res = await fetch(`/api/comments/portfolio/${portfolioId}`, {
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
      const res = await fetch(`/api/users/${userID}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        const data = await res.json();
        setUserDetails(data);

        // Get the profile picture URL after user details are fetched
        const profilePicUrl = getProfilePictureUrl(data);
        setProfilePictureUrl(profilePicUrl); // Update the state with the profile picture URL
      }
    } catch (err) {
      console.error("Failed to fetch user details:", err);
    }
  };

  // useEffect hook to fetch user details and portfolio when userID changes
  useEffect(() => {
    fetchPortfolio();  // Make sure this function is defined elsewhere
    fetchUserDetails(); // Fetch user details
  }, [userID]); // Re-fetch on userID change

  // Function to get the profile picture URL (with base URL handling)
  const getProfilePictureUrl = (user) => {
    console.log("User object:", user);
    const pic = user?.profilePicture; // Get profile picture path
    console.log("Profile Picture URL:", pic);

    if (!pic) return "/default-avatar.png"; // Fallback to default if no picture
    return pic.startsWith("http") ? pic : `https://your-backend-url.com${pic}`; // Handle relative path if necessary
  };

  // Ensure that user details are fetched before trying to render the profile picture
  if (!userDetails) {
    return <div>Loading...</div>; // Optionally, a loading state while data is being fetched
  }



  // Handle feedback submission (comment + rating)
  const handleSubmitFeedback = async () => {
    try {
      // Submit comment with rating
      await fetch(
        `/api/comments/${personalInfo.userId}/${portfolio.id}`,
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
      await fetch(`/api/ratings/`, {
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

      // Refresh comments
      fetchComments(portfolio.id);
    } catch (error) {
      console.error("Failed to submit feedback", error);
    }
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
  src={userDetails?.profilePicture} // Pass userDetails to the function
  sx={{
    width: 100,
    height: 100,
    boxShadow: theme.shadows[4],
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
              
              <Stack direction={isMobile ? "column" : "row"} spacing={2}>
                <Button 
                  variant="contained" 
                  startIcon={<Comment />}
                  onClick={() => setCommentModalOpen(true)}
                  sx={{ borderRadius: 2 }}
                >
                  Add Review
                </Button>
                <Button
  variant="outlined"
  startIcon={<Event />}
  onClick={() => {
    setAppointmentModalOpen(true);
    setSelectedService(null);
  }}
  sx={{ borderRadius: 2 }}
>
  Book Appointment
</Button>
              </Stack>
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
  {portfolio?.startTime && portfolio?.endTime 
    ? `${formatTime(portfolio.startTime)} - ${formatTime(portfolio.endTime)}`
    : "Not specified"}
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
                                : comment.profilePicture // Remove baseUrl
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

        {/* Comment Modal */}
        <Dialog 
          open={commentModalOpen} 
          onClose={() => setCommentModalOpen(false)}
          fullWidth
          maxWidth="sm"
        >
          <DialogTitle>Add Review</DialogTitle>
          <DialogContent>
            <Box sx={{ mt: 2 }}>
              <Typography variant="subtitle1" gutterBottom>
                Your Rating:
              </Typography>
              <Rating
                value={newRating}
                onChange={(event, newValue) => setNewRating(newValue)}
                size="large"
                sx={{ color: theme.palette.warning.main, mb: 3 }}
              />
              <TextField
                label="Your Review"
                variant="outlined"
                multiline
                fullWidth
                rows={4}
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button 
              onClick={() => setCommentModalOpen(false)} 
              color="primary"
              sx={{ borderRadius: 2 }}
            >
              Cancel
            </Button>
            <Button 
              onClick={handleSubmitFeedback} 
              color="primary"
              variant="contained"
              sx={{ borderRadius: 2 }}
            >
              Submit Review
            </Button>
          </DialogActions>
        </Dialog>

{/* Appointment Modal */}
<Dialog 
  open={appointmentModalOpen} 
  onClose={() => setAppointmentModalOpen(false)}
  fullWidth
  maxWidth="sm"
>
  <DialogTitle>Book Appointment</DialogTitle>
  <DialogContent>
    <Box sx={{ mt: 2 }}>
      {/* Service Selection Dropdown */}
      <TextField
  select
  fullWidth
  value={selectedService?.id || ''}
  onChange={(e) => {
    const serviceId = e.target.value;
    if (!serviceId) {
      setSelectedService(null);
      return;
    }
    const service = portfolio?.servicesOffered?.find(s => 
      String(s.id) === String(serviceId)
    );
    setSelectedService(service || null);
  }}
  sx={{ mb: 3 }}
  SelectProps={{
    native: true,
  }}
>
  <option value="">Select a service...</option>
  {portfolio?.servicesOffered?.map((service) => (
    <option 
      key={service.id} 
      value={service.id}
      onMouseDown={(e) => e.preventDefault()} // Prevent immediate menu close
    >
      {service.name} - {service.pricing}
    </option>
  ))}
</TextField>

      {/* Rest of your appointment modal content */}
      <TextField
        label="Appointment Date & Time"
        type="datetime-local"
        fullWidth
        InputLabelProps={{ shrink: true }}
        value={appointmentDateTime}
        onChange={(e) => setAppointmentDateTime(e.target.value)}
        sx={{ mb: 3 }}
        inputProps={{
          min: getMinDateTime(),
          step: 900 // 15-minute intervals
        }}
        error={appointmentDateTime && (
          !isAvailableDay(appointmentDateTime, portfolio?.daysAvailable) || 
          !isTimeInRange(appointmentDateTime, portfolio?.startTime, portfolio?.endTime)
        )}
        helperText={
          appointmentDateTime && (
            !isAvailableDay(appointmentDateTime, portfolio?.daysAvailable) 
              ? "Provider is not available on this day" 
              : !isTimeInRange(appointmentDateTime, portfolio?.startTime, portfolio?.endTime)
                ? "Time must be between provider's working hours"
                : ""
          )
        }
      />
      
      <TextField
        label="Notes (optional)"
        fullWidth
        multiline
        rows={3}
        value={appointmentNotes}
        onChange={(e) => setAppointmentNotes(e.target.value)}
      />
    </Box>
  </DialogContent>
  <DialogActions>
    <Button 
      onClick={() => {
        setAppointmentModalOpen(false);
        setSelectedService(null);
      }} 
      sx={{ borderRadius: 2 }}
    >
      Cancel
    </Button>
    <Button 
      onClick={handleSubmitAppointment} 
      variant="contained"
      disabled={!selectedService}
      sx={{ borderRadius: 2 }}
    >
      Confirm Booking
    </Button>
  </DialogActions>
</Dialog>
      </Container>
    </Box>
  );
};

export default ProviderPortfolioPage;