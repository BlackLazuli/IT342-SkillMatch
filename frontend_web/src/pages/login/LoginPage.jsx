import React, { useState } from "react";
import { useNavigate } from "react-router-dom"; // For redirection
import AppBar from "../../component/AppBar";
import { Container, TextField, Button, Typography, Paper, Box } from "@mui/material";

const LoginPage = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate(); // Hook for navigation

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

      // Store token and role
      localStorage.setItem("token", data.token);
      localStorage.setItem("role", data.role);

      // Redirect based on role
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
    <div>
      <AppBar />
      <Container maxWidth="sm">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
          <Paper elevation={3} sx={{ padding: 4, width: "100%" }}>
            <Typography variant="h4" align="center" gutterBottom>
              Login
            </Typography>
            {error && (
              <Typography color="error" variant="body2" align="center" gutterBottom>
                {error}
              </Typography>
            )}
            <form onSubmit={handleSubmit}>
              <TextField
                label="Email"
                variant="outlined"
                fullWidth
                margin="normal"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
              <TextField
                label="Password"
                type="password"
                variant="outlined"
                fullWidth
                margin="normal"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <Button type="submit" variant="contained" color="primary" fullWidth sx={{ mt: 2 }}>
                Login
              </Button>
            </form>
          </Paper>
        </Box>
      </Container>
    </div>
  );
};

export default LoginPage;
