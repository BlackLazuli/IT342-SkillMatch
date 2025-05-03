import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  TextField,
  Button,
  Paper,
  Chip,
  FormControlLabel,
  Checkbox,
  FormGroup,
} from "@mui/material";
import AppBar from "../../component/AppBar";

const daysOfWeek = [
  "Monday", "Tuesday", "Wednesday",
  "Thursday", "Friday", "Saturday", "Sunday", "Everyday"
];

const AddPortfolioPage = () => {
  const { userID } = useParams();
  const navigate = useNavigate();
  const token = localStorage.getItem("token");
  const baseUrl = "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080"; // Change to your EC2 public IP/DNS

  const [portfolioData, setPortfolioData] = useState({
    workExperience: "",
    servicesOffered: [],
    newServiceName: "",
    newServiceDescription: "",
    newServicePricing: "",
    daysAvailable: [], // Global availability
    availableTime: "", // Global availability
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setPortfolioData((prevState) => ({
      ...prevState,
      [name]: value,
    }));
  };

  const handleDayCheckboxChange = (day) => {
    setPortfolioData((prevState) => {
      const currentDays = prevState.daysAvailable;
      const updatedDays = currentDays.includes(day)
        ? currentDays.filter((d) => d !== day)
        : [...currentDays, day];
      return {
        ...prevState,
        daysAvailable: updatedDays,
      };
    });
  };

  const handleServiceAdd = () => {
    const {
      newServiceName,
      newServiceDescription,
      newServicePricing,
    } = portfolioData;

    if (
      newServiceName.trim() &&
      newServiceDescription.trim() &&
      newServicePricing.trim()
    ) {
      setPortfolioData((prevState) => ({
        ...prevState,
        servicesOffered: [
          ...prevState.servicesOffered,
          {
            name: newServiceName,
            description: newServiceDescription,
            pricing: newServicePricing,
          },
        ],
        newServiceName: "",
        newServiceDescription: "",
        newServicePricing: "",
      }));
    }
  };

  const handleServiceRemove = (serviceToRemove) => {
    setPortfolioData((prevState) => ({
      ...prevState,
      servicesOffered: prevState.servicesOffered.filter(
        (service) => service.name !== serviceToRemove.name
      ),
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!token) {
      alert("No authentication token found. Please log in again.");
      return;
    }

    try {
      const response = await fetch(
        `/api/portfolios/${userID}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            workExperience: portfolioData.workExperience,
            daysAvailable: portfolioData.daysAvailable,
            time: portfolioData.availableTime,
            servicesOffered: portfolioData.servicesOffered,
          }),
        }
      );

      if (!response.ok) {
        throw new Error("Failed to add portfolio.");
      }

      alert("Portfolio added successfully!");
      navigate(`/portfolio/${userID}`);
    } catch (error) {
      console.error("Error adding portfolio:", error);
      alert("Error adding portfolio.");
    }
  };

  return (
    <Box sx={{ backgroundColor: "#ffffff", minHeight: "100vh" }}>
      <AppBar />

      <Box sx={{ display: "flex", justifyContent: "center", pt: 8 }}>
        <Paper elevation={3} sx={{ p: 4, width: "100%", maxWidth: 600 }}>
          <Typography variant="h4" gutterBottom>
            Add Portfolio
          </Typography>
          <form onSubmit={handleSubmit}>
            <TextField
              label="Work Experience"
              name="workExperience"
              value={portfolioData.workExperience}
              onChange={handleChange}
              fullWidth
              required
              margin="normal"
            />

            <Typography variant="h6" sx={{ mt: 2 }}>
              Availability
            </Typography>
            <FormGroup row sx={{ flexWrap: "wrap", gap: 1 }}>
              {daysOfWeek.map((day) => (
                <FormControlLabel
                  key={day}
                  control={
                    <Checkbox
                      checked={portfolioData.daysAvailable.includes(day)}
                      onChange={() => handleDayCheckboxChange(day)}
                    />
                  }
                  label={day}
                />
              ))}
            </FormGroup>
            <TextField
              label="Available Time"
              name="availableTime"
              value={portfolioData.availableTime}
              onChange={handleChange}
              fullWidth
              margin="normal"
            />

            <Typography variant="h6" sx={{ mt: 2 }}>
              Services
            </Typography>
            <TextField
              label="New Service Name"
              name="newServiceName"
              value={portfolioData.newServiceName}
              onChange={handleChange}
              fullWidth
              margin="normal"
            />
            <TextField
              label="New Service Description"
              name="newServiceDescription"
              value={portfolioData.newServiceDescription}
              onChange={handleChange}
              fullWidth
              margin="normal"
            />
            <TextField
              label="New Service Pricing"
              name="newServicePricing"
              value={portfolioData.newServicePricing}
              onChange={handleChange}
              fullWidth
              margin="normal"
            />
            <Button
              type="button"
              variant="outlined"
              onClick={handleServiceAdd}
              sx={{ alignSelf: "flex-start", mb: 2 }}
            >
              Add Service
            </Button>
            <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1 }}>
              {portfolioData.servicesOffered.map((service, index) => (
                <Chip
                  key={index}
                  label={`${service.name} - ${service.pricing}`}
                  onDelete={() => handleServiceRemove(service)}
                />
              ))}
            </Box>

            <Button type="submit" variant="contained" fullWidth sx={{ mt: 2 }}>
              Submit
            </Button>
          </form>
        </Paper>
      </Box>
    </Box>
  );
};

export default AddPortfolioPage;
