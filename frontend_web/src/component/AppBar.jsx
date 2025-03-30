import React from "react";
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

const drawerWidth = 240;

const AppBar = () => {
  return (
    <Drawer
      variant="permanent"
      sx={{
        width: drawerWidth,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width: drawerWidth,
          backgroundColor: "#F8D7A8", // Light peach color
          color: "black",
          display: "flex",
          flexDirection: "column",
          justifyContent: "space-between",
        },
      }}
    >
      {/* Logo */}
      <Box sx={{ textAlign: "center", padding: "16px" }}>
        <Typography variant="h6" sx={{ fontWeight: "bold" }}>
          SKILL MATCH
        </Typography>
      </Box>

      {/* Navigation List */}
      <List>
        {[
          { text: "Home", icon: <Home /> },
          { text: "Profile", icon: <Person /> },
          { text: "Portfolio", icon: <Work /> },
          { text: "Appointments", icon: <Event /> },
        ].map((item, index) => (
          <ListItem button key={index} sx={{ padding: "12px 24px" }}>
            <ListItemIcon sx={{ color: "black" }}>{item.icon}</ListItemIcon>
            <ListItemText primary={item.text} />
          </ListItem>
        ))}
      </List>

      <Divider />

      {/* Bottom Section */}
      <Box sx={{ padding: "16px", textAlign: "center" }}>
        <ListItem button>
          <ListItemIcon sx={{ color: "black" }}>
            <Settings />
          </ListItemIcon>
          <ListItemText primary="Settings" />
        </ListItem>
        <Box sx={{ display: "flex", alignItems: "center", mt: 2 }}>
          <Avatar sx={{ bgcolor: "gray", marginRight: "8px" }}>F</Avatar>
          <Box>
            <Typography variant="body2" fontWeight="bold">
              Feerdee Liban
            </Typography>
            <Typography variant="caption">freerdee@skillmatch.com</Typography>
          </Box>
        </Box>
      </Box>
    </Drawer>
  );
};

export default AppBar;
