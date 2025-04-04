import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
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
import { Home, Person, Work, Event, Settings } from "@mui/icons-material";
import { usePersonalInfo } from "../context/PersonalInfoContext"; // Import context

const drawerWidth = 240;

const AppBar = () => {
  const { personalInfo } = usePersonalInfo(); // Get user info from context
  const navigate = useNavigate(); // Hook for navigation

  const menuItems = [
    { text: "Home", icon: <Home />, path: "/" },
    { text: "Profile", icon: <Person />, path: "/profile" },
    { 
      text: "Portfolio", 
      icon: <Work />, 
      path: personalInfo?.userId ? `/portfolio/${personalInfo.userId}` : "/" 
    },
    { text: "Appointments", icon: <Event />, path: "/appointments" },
  ];

  const handleNavigation = (path) => {
    if (path.includes("/portfolio/") && !personalInfo?.userId) {
      alert("You need to log in to access your portfolio.");
      return;
    }
    navigate(path);
  };

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: drawerWidth,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width: drawerWidth,
          backgroundColor: "#F8D7A8",
          color: "black",
          display: "flex",
          flexDirection: "column",
          justifyContent: "space-between",
        },
      }}
    >
      <Box sx={{ textAlign: "center", padding: "16px" }}>
        <img src="/skillmatchlogo.png" alt="Skill Match Logo" style={{ width: "200px", height: "80px" }} />
      </Box>

      <List>
        {menuItems.map((item, index) => (
          <ListItem button key={index} sx={{ padding: "12px 24px" }} onClick={() => handleNavigation(item.path)}>
            <ListItemIcon sx={{ color: "black" }}>{item.icon}</ListItemIcon>
            <ListItemText primary={item.text} />
          </ListItem>
        ))}
      </List>

      <Divider />

      <Box sx={{ padding: "16px", textAlign: "center" }}>
        <ListItem button>
          <ListItemIcon sx={{ color: "black" }}>
            <Settings />
          </ListItemIcon>
          <ListItemText primary="Settings" />
        </ListItem>
        <Box sx={{ display: "flex", alignItems: "center", mt: 2 }}>
          <Avatar sx={{ bgcolor: "gray", marginRight: "8px" }}>
            {personalInfo?.firstName ? personalInfo.firstName[0] : "?"}
          </Avatar>
          <Box>
            <Typography variant="body2" fontWeight="bold">
              {personalInfo?.firstName || "Guest"} {personalInfo?.lastName || ""}
            </Typography>
            <Typography variant="caption">{personalInfo?.email || "No email"}</Typography>
          </Box>
        </Box>
      </Box>
    </Drawer>
  );
};

export default AppBar;
