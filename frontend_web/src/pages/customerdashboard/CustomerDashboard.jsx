import React from "react";
import AppBar from "../../component/AppBarCustomer";
import { Box, Typography } from "@mui/material";

const drawerWidth = 240; // Same width as the AppBar (Drawer)

const ProviderDashboard = () => {
  return (
    <Box sx={{ display: "flex" }}>
      {/* Sidebar (AppBar) */}
      <AppBar />

      {/* Main Content */}
      <Box sx={{ flexGrow: 1, p: 3, ml: `${drawerWidth}px` }}>
        <Typography variant="h4" fontWeight="bold">
          Welcome, Customer!
        </Typography>
        <Typography variant="body1">
          This is your customer dashboard.
        </Typography>
      </Box>
    </Box>
  );
};

export default ProviderDashboard;
