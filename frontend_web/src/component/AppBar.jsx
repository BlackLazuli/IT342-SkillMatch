import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import {
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Divider,
  Avatar,
  Typography,
  Box,
} from "@mui/material";
import { Home, Person, Work, Event, Settings, Logout } from "@mui/icons-material";
import { usePersonalInfo } from "../context/PersonalInfoContext";

const drawerWidth = 240;

const AppBar = () => {
  const { personalInfo, setPersonalInfo } = usePersonalInfo();
  const navigate = useNavigate();

  const [user, setUser] = useState(null);
  const [profilePictureUrl, setProfilePictureUrl] = useState("/default-avatar.png");

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        if (personalInfo?.userId) {
          const res = await axios.get(`http://localhost:8080/api/users/${personalInfo.userId}`);
          const userData = res.data;
          setUser(userData);
          if (userData.profilePicture) {
            setProfilePictureUrl(
              userData.profilePicture.startsWith("http")
                ? userData.profilePicture
                : `http://localhost:8080${userData.profilePicture}`
            );
          }
        }
      } catch (err) {
        console.error("Error fetching user info:", err);
      }
    };

    fetchUserInfo();
  }, [personalInfo?.userId]);

  const handleLogout = () => {
    setPersonalInfo(null); // Clear context
    navigate("/"); // Navigate to homepage
  };

  const menuItems = [
    { text: "Home", icon: <Home />, path: "/provider-dashboard" },
    { text: "Profile", icon: <Person />, path: "/profile" },
    {
      text: "Portfolio",
      icon: <Work />,
      path: personalInfo?.userId ? `/portfolio/${personalInfo.userId}` : "/",
    },
    { text: "Appointments", icon: <Event />, path: "/appointments-customer" },
  ];

  const handleNavigation = (path) => {
    if (!personalInfo?.userId) {
      alert("You need to log in to access this page.");
      return;
    }
  
    // If path needs dynamic user info
    if (path === "/appointments-customer") {
      navigate(`/appointments-customer/${personalInfo.userId}`); // Or fetch latest appointment
    } else {
      navigate(path);
    }
  };

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: drawerWidth,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width: drawerWidth,
          height: "100vh",
          backgroundColor: "#f4f0ff",
          color: "#333",
          display: "flex",
          flexDirection: "column",
          px: 2,
          py: 3,
          boxSizing: "border-box",
        },
      }}
    >
      {/* Logo */}
      <Box sx={{ textAlign: "center", mb: 2 }}>
        <img
          src="/skillmatchlogo.png"
          alt="Skill Match Logo"
          style={{ width: "180px", height: "70px", objectFit: "contain" }}
        />
      </Box>

      {/* Scrollable content */}
      <Box sx={{ flexGrow: 1, overflowY: "auto" }}>
        <List>
          {menuItems.map((item, index) => (
            <ListItem
              button
              key={index}
              sx={{
                borderRadius: 2,
                mb: 1,
                "&:hover": { backgroundColor: "#c1e3c6" },
              }}
              onClick={() => handleNavigation(item.path)}
            >
              <ListItemIcon sx={{ color: "#333" }}>{item.icon}</ListItemIcon>
              <ListItemText
                primary={item.text}
                primaryTypographyProps={{ fontWeight: 500 }}
              />
            </ListItem>
          ))}
        </List>
      </Box>

      <Divider sx={{ my: 2 }} />

      {/* Bottom section: settings, logout and user info */}
      <Box>
        <ListItem
          button
          sx={{
            borderRadius: 2,
            mb: 1,
            "&:hover": {
              backgroundColor: "#c1e3c6",
            },
          }}
        >
          <ListItemIcon sx={{ color: "#333" }}>
            <Settings />
          </ListItemIcon>
          <ListItemText primary="Settings" primaryTypographyProps={{ fontWeight: 500 }} />
        </ListItem>

        <ListItem
          button
          sx={{
            borderRadius: 2,
            mb: 1,
            "&:hover": {
              backgroundColor: "#f2c6c6",
            },
          }}
          onClick={handleLogout}
        >
          <ListItemIcon sx={{ color: "#333" }}>
            <Logout />
          </ListItemIcon>
          <ListItemText primary="Logout" primaryTypographyProps={{ fontWeight: 500 }} />
        </ListItem>

        <Box sx={{ display: "flex", alignItems: "center", mt: 3, px: 1 }}>
          <Avatar
            src={profilePictureUrl}
            alt="Profile"
            sx={{ width: 40, height: 40, mr: 2 }}
          >
            {!user?.profilePicture && (user?.firstName?.[0] || "?")}
          </Avatar>

          <Box>
            <Typography variant="body2" fontWeight="bold">
              {user?.firstName || "Guest"} {user?.lastName || ""}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {user?.email || "No email"}
            </Typography>
          </Box>
        </Box>
      </Box>
    </Drawer>
  );
};

export default AppBar;
