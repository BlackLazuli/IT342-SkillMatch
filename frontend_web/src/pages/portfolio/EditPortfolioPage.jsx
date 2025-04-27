import React, { useState, useEffect } from "react";
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
  FormGroup
} from "@mui/material";
import AppBar from "../../component/AppBar";

const daysOfWeek = [
  "Monday", "Tuesday", "Wednesday",
  "Thursday", "Friday", "Saturday", "Sunday"
];

const baseUrl = "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080"; // Change to your EC2 public IP/DNS

const EditPortfolioPage = () => {
  const { userID } = useParams();
  const navigate = useNavigate();
  const token = localStorage.getItem("token");

  const [portfolioData, setPortfolioData] = useState({
    workExperience: "",
    servicesOffered: [],
    newServiceName: "",
    newServiceDescription: "",
    newServicePricing: "",
    daysAvailable: [],
    time: "",
  });

  useEffect(() => {
    const fetchPortfolio = async () => {
      if (!token) {
        alert("Please log in first.");
        return;
      }

      try {
        const response = await fetch(`${baseUrl}/api/portfolios/${userID}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) {
          throw new Error("Failed to fetch portfolio.");
        }

        const portfolio = await response.json();
        setPortfolioData({
          workExperience: portfolio.workExperience,
          servicesOffered: portfolio.servicesOffered || [],
          daysAvailable: portfolio.daysAvailable || [],
          time: portfolio.time || "",
          newServiceName: "",
          newServiceDescription: "",
          newServicePricing: "",
        });
      } catch (error) {
        console.error("Error fetching portfolio:", error);
        alert("Error loading portfolio data.");
      }
    };

    fetchPortfolio();
  }, [userID, token]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setPortfolioData((prevState) => ({
      ...prevState,
      [name]: value,
    }));
  };

  const handleDayCheckboxChange = (day) => {
    console.log("Toggling day: ", day);  // Debugging

    setPortfolioData((prevState) => {
      const currentDays = prevState.daysAvailable;
      const updatedDays = currentDays.includes(day)
        ? currentDays.filter((d) => d !== day)
        : [...currentDays, day];

      console.log("Updated daysAvailable: ", updatedDays);  // Debugging

      return {
        ...prevState,
        daysAvailable: updatedDays,
      };
    });
  };

  const handleServiceAdd = () => {
    const { newServiceName, newServiceDescription, newServicePricing } = portfolioData;

    if (
      newServiceName.trim() !== "" &&
      newServiceDescription.trim() !== "" &&
      newServicePricing.trim() !== ""
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

    console.log("Submitting data: ", portfolioData); // Debugging

    try {
      const response = await fetch(
        `${baseUrl}/api/portfolios/${userID}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            workExperience: portfolioData.workExperience,
            daysAvailable: portfolioData.daysAvailable,
            time: portfolioData.time,
            servicesOffered: portfolioData.servicesOffered,
          }),
        }
      );

      if (!response.ok) {
        throw new Error("Failed to update portfolio.");
      }

      alert("Portfolio updated successfully!");
      navigate(`/portfolio/${userID}`);
    } catch (error) {
      console.error("Error updating portfolio:", error);
      alert("Error updating portfolio.");
    }
  };

  return (
    <Box sx={{ backgroundColor: "#ffffff", minHeight: "100vh" }}>
      <AppBar />

      <Box sx={{ display: "flex", justifyContent: "center", pt: 8 }}>
        <Paper elevation={3} sx={{ p: 4, width: "100%", maxWidth: 600 }}>
          <Typography variant="h4" gutterBottom>
            Edit Portfolio
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
            <Box sx={{ display: "flex", flexDirection: "column", gap: 1 }}>
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
                name="time"
                value={portfolioData.time}
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
            </Box>
            <Button type="submit" variant="contained" fullWidth sx={{ mt: 2 }}>
              Update Portfolio
            </Button>
          </form>
        </Paper>
      </Box>
    </Box>
  );
};

export default EditPortfolioPage;
