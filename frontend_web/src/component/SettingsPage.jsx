import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import { 
  Box, 
  Typography, 
  Avatar, 
  Paper, 
  Container,
  Grid,
  TextField,
  Button,
  Divider,
  IconButton
} from "@mui/material";
import { 
  Edit as EditIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
  Check as CheckIcon,
  Close as CloseIcon
} from "@mui/icons-material";
import AppBar from "./AppBar";

const SettingsPage = () => {
  const { userId } = useParams();
  const [user, setUser] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: ""
  });

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const response = await axios.get(`/api/users/${userId}`);
        setUser(response.data);
        setFormData({
          firstName: response.data.firstName,
          lastName: response.data.lastName,
          email: response.data.email,
          phoneNumber: response.data.phoneNumber || ""
        });
      } catch (error) {
        console.error("Error fetching user data:", error);
      }
    };
    
    fetchUserData();
  }, [userId]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSave = async () => {
    try {
      await axios.put(`/api/users/${userId}`, formData);
      setUser({ ...user, ...formData });
      setEditMode(false);
    } catch (error) {
      console.error("Error updating user data:", error);
    }
  };

  return (
    <Box sx={{ display: "flex" }}>
      <AppBar />
      
      <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
        <Container maxWidth="md">
          {!user ? (
            <Typography>Loading...</Typography>
          ) : (
            <Paper elevation={3} sx={{ p: 4, borderRadius: 3 }}>
              {/* Header Section */}
              <Box sx={{ 
                display: 'flex', 
                alignItems: 'center', 
                mb: 4,
                justifyContent: 'space-between'
              }}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <Avatar
                    src={user.profilePicture || "/default-avatar.png"}
                    sx={{ 
                      width: 80, 
                      height: 80, 
                      mr: 3,
                      border: '2px solid #7b1fa2'
                    }}
                  />
                  <Typography variant="h4" sx={{ fontWeight: 600 }}>
                    {user.firstName} {user.lastName}
                  </Typography>
                </Box>
                {!editMode ? (
                  <Button 
                    variant="outlined" 
                    startIcon={<EditIcon />}
                    onClick={() => setEditMode(true)}
                    sx={{ textTransform: 'none' }}
                  >
                    Edit Profile
                  </Button>
                ) : (
                  <Box>
                    <IconButton 
                      color="success" 
                      onClick={handleSave}
                      sx={{ mr: 1 }}
                    >
                      <CheckIcon />
                    </IconButton>
                    <IconButton 
                      color="error" 
                      onClick={() => setEditMode(false)}
                    >
                      <CloseIcon />
                    </IconButton>
                  </Box>
                )}
              </Box>

              <Divider sx={{ my: 3 }} />

              {/* Personal Information Section */}
              <Box sx={{ mb: 4 }}>
                <Typography variant="h5" gutterBottom sx={{ 
                  fontWeight: 600,
                  color: 'primary.main',
                  mb: 2
                }}>
                  Personal Information
                </Typography>
                
                <Grid container spacing={3}>
                  <Grid item xs={12} sm={6}>
                    <TextField
                      label="First Name"
                      name="firstName"
                      value={formData.firstName}
                      onChange={handleInputChange}
                      fullWidth
                      disabled={!editMode}
                      variant={editMode ? "outlined" : "filled"}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <TextField
                      label="Last Name"
                      name="lastName"
                      value={formData.lastName}
                      onChange={handleInputChange}
                      fullWidth
                      disabled={!editMode}
                      variant={editMode ? "outlined" : "filled"}
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      label="Email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      fullWidth
                      disabled={!editMode}
                      variant={editMode ? "outlined" : "filled"}
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      label="Phone Number"
                      name="phoneNumber"
                      value={formData.phoneNumber}
                      onChange={handleInputChange}
                      fullWidth
                      disabled={!editMode}
                      variant={editMode ? "outlined" : "filled"}
                      placeholder="Not provided"
                    />
                  </Grid>
                </Grid>
              </Box>

              <Divider sx={{ my: 3 }} />

              {/* Account Security Section */}
              <Box sx={{ mb: 3 }}>
                <Typography variant="h5" gutterBottom sx={{ 
                  fontWeight: 600,
                  color: 'primary.main',
                  mb: 2
                }}>
                  Account Security
                </Typography>
                
                <TextField
                  label="Password"
                  type={showPassword ? "text" : "password"}
                  value="••••••••"
                  fullWidth
                  disabled
                  variant="filled"
                  InputProps={{
                    endAdornment: (
                      <IconButton
                        onClick={() => setShowPassword(!showPassword)}
                        edge="end"
                      >
                        {showPassword ? <VisibilityOffIcon /> : <VisibilityIcon />}
                      </IconButton>
                    )
                  }}
                />
                
                <Box sx={{ mt: 3 }}>
                  <Button 
                    variant="contained" 
                    color="primary"
                    sx={{ textTransform: 'none' }}
                    onClick={() => {/* Add change password functionality */}}
                  >
                    Change Password
                  </Button>
                </Box>
              </Box>
            </Paper>
          )}
        </Container>
      </Box>
    </Box>
  );
};

export default SettingsPage;