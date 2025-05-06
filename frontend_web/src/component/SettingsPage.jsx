import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import { Box, Typography, Avatar, Paper, Container } from "@mui/material";
import AppBar from "./AppBar"; // Adjust the import path based on your file structure

const SettingsPage = () => {
  const { userId } = useParams();
  const [user, setUser] = useState(null);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const response = await axios.get(`/api/users/${userId}`);
        setUser(response.data);
      } catch (error) {
        console.error("Error fetching user data:", error);
      }
    };
    
    fetchUserData();
  }, [userId]);

  return (
    <Box sx={{ display: "flex" }}>
      {/* Sidebar/Navigation */}
      <AppBar />
      
      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          marginLeft: `${drawerWidth}px`,
          width: `calc(100% - ${drawerWidth}px)`,
        }}
      >
        <Container maxWidth="md">
          {!user ? (
            <Typography>Loading...</Typography>
          ) : (
            <Paper elevation={3} sx={{ p: 4, mt: 4 }}>
              <Box sx={{ display: "flex", alignItems: "center", mb: 4 }}>
                <Avatar
                  src={user.profilePicture || "/default-avatar.png"}
                  sx={{ width: 80, height: 80, mr: 3 }}
                />
                <Typography variant="h4">
                  {user.firstName} {user.lastName}
                </Typography>
              </Box>
              
              <Box sx={{ mb: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Personal Information
                </Typography>
                <Typography>Email: {user.email}</Typography>
                <Typography>Phone: {user.phone || "Not provided"}</Typography>
                {/* Add more user fields as needed */}
              </Box>
              
              <Box sx={{ mb: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Account Settings
                </Typography>
                {/* Add settings options here */}
              </Box>
            </Paper>
          )}
        </Container>
      </Box>
    </Box>
  );
};

// Make sure this matches the drawerWidth in your AppBar component
const drawerWidth = 240;

export default SettingsPage;