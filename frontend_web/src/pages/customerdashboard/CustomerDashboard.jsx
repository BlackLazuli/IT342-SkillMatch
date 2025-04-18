import React, { useEffect, useState } from "react";
import axios from "axios";
import AppBar from "../../component/AppBarCustomer";
import {
  Box,
  Typography,
  Card,
  CardContent,
  Grid,
  Button,
  Collapse,
} from "@mui/material";

const drawerWidth = 240;

const ProviderDashboard = () => {
  const [portfolios, setPortfolios] = useState([]);
  const [error, setError] = useState("");
  const [openPortfolioIds, setOpenPortfolioIds] = useState([]);

  useEffect(() => {
    const fetchPortfolios = async () => {
      try {
        const token = localStorage.getItem("token");

        const response = await axios.get("http://localhost:8080/api/portfolios/getAllPortfolios", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        setPortfolios(response.data);
      } catch (err) {
        console.error("Error fetching portfolios:", err);
        setError("Failed to fetch portfolios.");
      }
    };

    fetchPortfolios();
  }, []);

  const togglePortfolio = (id) => {
    setOpenPortfolioIds((prev) =>
      prev.includes(id) ? prev.filter((pid) => pid !== id) : [...prev, id]
    );
  };

  return (
    <Box sx={{ display: "flex" }}>
      <AppBar />
      <Box sx={{ flexGrow: 1, p: 3, ml: `${drawerWidth}px` }}>
        <Typography variant="h4" fontWeight="bold" gutterBottom>
          Welcome, Customer!
        </Typography>
        <Typography variant="body1" gutterBottom>
          Here are the services offered by our providers:
        </Typography>

        {error && (
          <Typography color="error" mb={2}>
            {error}
          </Typography>
        )}

        <Grid container spacing={2}>
        {portfolios.map((portfolio) =>
  portfolio.servicesOffered?.map((service) => (
    <Grid item xs={12} md={6} key={service.id}>
      <Card variant="outlined">
        <CardContent>
          <Typography variant="h6">{service.name}</Typography>

          {/* Description removed */}

          <Typography variant="body2">Pricing: {service.pricing}</Typography>
          <Typography variant="body2">Time: {service.time}</Typography>
          <Typography variant="body2">
            Days: {service.daysOfTheWeek.join(", ")}
          </Typography>

          <Button
            size="small"
            sx={{ mt: 1 }}
            onClick={() => togglePortfolio(portfolio.id)}
          >
            {openPortfolioIds.includes(portfolio.id)
              ? "Hide Portfolio"
              : "View Portfolio"}
          </Button>

          <Collapse in={openPortfolioIds.includes(portfolio.id)}>
            <Box mt={2}>
              <Typography variant="subtitle1" fontWeight="bold">
                Work Experience
              </Typography>
              <Typography variant="body2" gutterBottom>
                {portfolio.workExperience || "N/A"}
              </Typography>

              <Typography variant="subtitle1" fontWeight="bold">
                Client Testimonials
              </Typography>
              <Typography variant="body2">
                {portfolio.clientTestimonials || "N/A"}
              </Typography>
            </Box>
          </Collapse>
        </CardContent>
      </Card>
    </Grid>
  ))
)}
        </Grid>
      </Box>
    </Box>
  );
};

export default ProviderDashboard;
