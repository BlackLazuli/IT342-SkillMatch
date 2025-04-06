import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Container,
  Box,
  TextField,
  Button,
  Typography,
  ToggleButtonGroup,
  ToggleButton,
  Paper,
  CircularProgress,
  Alert,
} from "@mui/material";

const RegistrationPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    role: "CUSTOMER",
    phoneNumber: "",
  });

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRoleSelect = (event, newRole) => {
    if (newRole) setFormData({ ...formData, role: newRole });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (
      !formData.firstName ||
      !formData.lastName ||
      !formData.email ||
      !formData.password ||
      !formData.phoneNumber
    ) {
      setError("All fields are required.");
      return;
    }

    setError("");
    setLoading(true);

    try {
      const response = await fetch("http://localhost:8080/api/users/createUser", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        throw new Error("Failed to register. Please try again.");
      }

      const result = await response.json();
      alert("Registration successful! Redirecting to login.");
      navigate("/");
    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: "100vh",
        backgroundColor: "#f4f0ff",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        px: 2,
      }}
    >
      {/* Logo */}
      <Box mb={3}>
        <img src="/skillmatchlogo.png" alt="Skill Match Logo" style={{ height: "80px" }} />
      </Box>

      {/* Registration Form */}
      <Paper
        elevation={3}
        sx={{
          p: 4,
          borderRadius: 3,
          width: "100%",
          maxWidth: 400,
          textAlign: "center",
        }}
      >
        <Typography variant="h4" fontWeight="bold" gutterBottom>
          Register
        </Typography>
        <Typography variant="body2" color="text.secondary" mb={2}>
          Create your account
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth
            placeholder="First Name"
            name="firstName"
            value={formData.firstName}
            onChange={handleChange}
            margin="normal"
            required
            InputProps={{
              sx: { backgroundColor: "#c1e3c6", borderRadius: 1 },
            }}
          />
          <TextField
            fullWidth
            placeholder="Last Name"
            name="lastName"
            value={formData.lastName}
            onChange={handleChange}
            margin="normal"
            required
            InputProps={{
              sx: { backgroundColor: "#c1e3c6", borderRadius: 1 },
            }}
          />
          <TextField
            fullWidth
            type="email"
            placeholder="Email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            margin="normal"
            required
            InputProps={{
              sx: { backgroundColor: "#c1e3c6", borderRadius: 1 },
            }}
          />
          <TextField
            fullWidth
            type="password"
            placeholder="Password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            margin="normal"
            required
            InputProps={{
              sx: { backgroundColor: "#c1e3c6", borderRadius: 1 },
            }}
          />
          <TextField
            fullWidth
            placeholder="Phone Number"
            name="phoneNumber"
            value={formData.phoneNumber}
            onChange={handleChange}
            margin="normal"
            required
            InputProps={{
              sx: { backgroundColor: "#c1e3c6", borderRadius: 1 },
            }}
          />

          <Typography variant="body1" sx={{ mt: 2, mb: 1 }}>
            Select Role:
          </Typography>
          <ToggleButtonGroup
            fullWidth
            value={formData.role}
            exclusive
            onChange={handleRoleSelect}
            color="primary"
            sx={{
              mb: 2,
              "& .MuiToggleButton-root": {
                textTransform: "none",
                fontWeight: 500,
                borderRadius: 2,
                borderColor: "#a3d2d3",
              },
              "& .Mui-selected": {
                backgroundColor: "#a3d2d3 !important",
                color: "#fff",
              },
            }}
          >
            <ToggleButton value="CUSTOMER">Customer</ToggleButton>
            <ToggleButton value="SERVICE PROVIDER">Service Provider</ToggleButton>
          </ToggleButtonGroup>

          <Button
            fullWidth
            variant="contained"
            sx={{
              backgroundColor: "#a3d2d3",
              color: "#fff",
              fontWeight: "bold",
              "&:hover": { backgroundColor: "#91c6c6" },
            }}
            type="submit"
            disabled={loading}
          >
            {loading ? <CircularProgress size={24} /> : "Register"}
          </Button>

          <Typography variant="body2" mt={2}>
            Already have an account?{" "}
            <a href="/" style={{ color: "#5dd39e", fontWeight: 500 }}>
              Login
            </a>
          </Typography>
        </form>
      </Paper>
    </Box>
  );
};

export default RegistrationPage;
