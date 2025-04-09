import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { usePersonalInfo } from "../../context/PersonalInfoContext";
import {
  Container,
  Paper,
  Box,
  TextField,
  Typography,
  Button,
  InputAdornment,
} from "@mui/material";
import AccountCircle from "@mui/icons-material/AccountCircle";
import LockIcon from "@mui/icons-material/Lock";

const LoginPage = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const { setPersonalInfo } = usePersonalInfo();

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!email || !password) {
      setError("Both fields are required.");
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        throw new Error("Invalid email or password.");
      }

      const data = await response.json();

      if (!data.userId || !data.token) {
        throw new Error("User ID or token is missing in response.");
      }

      localStorage.setItem("token", data.token);
      setPersonalInfo({
        userId: data.userId,
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        role: data.role,
        contactNumber: data.phoneNumber, 
      });

      if (data.role === "CUSTOMER") {
        navigate("/customer-dashboard");
      } else if (data.role === "SERVICE PROVIDER") {
        navigate("/provider-dashboard");
      } else {
        throw new Error("Invalid role received.");
      }
    } catch (error) {
      setError(error.message);
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

      {/* Login Form Card */}
      <Paper
        elevation={3}
        sx={{
          p: 4,
          borderRadius: 3,
          width: "100%",
          maxWidth: 380,
          textAlign: "center",
        }}
      >
        <Typography variant="h4" fontWeight="bold" gutterBottom>
          Welcome
        </Typography>
        <Typography variant="body2" color="text.secondary" mb={2}>
          We are glad to see you
        </Typography>

        {error && (
          <Typography color="error" variant="body2" mb={2}>
            {error}
          </Typography>
        )}

        <form onSubmit={handleSubmit}>
          <TextField
            placeholder="Username"
            variant="outlined"
            fullWidth
            margin="normal"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <AccountCircle />
                </InputAdornment>
              ),
              sx: { backgroundColor: "#c1e3c6", borderRadius: 1 },
            }}
          />
          <TextField
            placeholder="Password"
            type="password"
            variant="outlined"
            fullWidth
            margin="normal"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <LockIcon />
                </InputAdornment>
              ),
              sx: { backgroundColor: "#c1e3c6", borderRadius: 1 },
            }}
          />

          <Button
            type="submit"
            variant="contained"
            fullWidth
            sx={{
              mt: 2,
              backgroundColor: "#a3d2d3",
              color: "#fff",
              fontWeight: "bold",
              "&:hover": { backgroundColor: "#91c6c6" },
            }}
          >
            LOGIN
          </Button>
        </form>

        <Typography variant="body2" mt={3}>
          <span style={{ color: "#777" }}>Don't have an account? </span>
          <a href="/register" style={{ color: "#5dd39e", fontWeight: 500 }}>
            SIGN UP
          </a>
        </Typography>
      </Paper>
    </Box>
  );
};

export default LoginPage;
