import React from "react";
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
import { usePersonalInfo } from "../context/PersonalInfoContext";

const drawerWidth = 240;

const AppBar = () => {
  const { personalInfo } = usePersonalInfo();
  const navigate = useNavigate();

  const menuItems = [
    { text: "Home", icon: <Home />, path: "/" },
    { text: "Profile", icon: <Person />, path: "/profile" },
    {
      text: "Portfolio",
      icon: <Work />,
      path: personalInfo?.userId ? `/portfolio/${personalInfo.userId}` : "/",
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

        {/* Bottom section: settings and user */}
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

          <Box sx={{ display: "flex", alignItems: "center", mt: 3, px: 1 }}>
            <Avatar sx={{ bgcolor: "#a3d2d3", mr: 2 }}>
              {personalInfo?.firstName ? personalInfo.firstName[0] : "?"}
            </Avatar>
            <Box>
              <Typography variant="body2" fontWeight="bold">
                {personalInfo?.firstName || "Guest"} {personalInfo?.lastName || ""}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {personalInfo?.email || "No email"}
              </Typography>
            </Box>
          </Box>
        </Box>
      </Drawer>
  );
};

export default AppBar;
