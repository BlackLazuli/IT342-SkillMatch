import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

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

  const handleRoleSelect = (role) => {
    setFormData({ ...formData, role });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Basic validation
    if (!formData.firstName || !formData.lastName || !formData.email || !formData.password || !formData.phoneNumber) {
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
      console.log("User registered:", result);

      alert("Registration successful! Redirecting to login.");
      navigate("/"); // Redirect to login

    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center h-screen bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-md w-96">
        <h2 className="text-2xl font-semibold mb-4 text-center">Register</h2>
        {error && <p className="text-red-500 text-sm mb-4">{error}</p>}
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-gray-700">First Name:</label>
            <input type="text" name="firstName" className="w-full p-2 border rounded mt-1" value={formData.firstName} onChange={handleChange} required />
          </div>
          <div className="mb-4">
            <label className="block text-gray-700">Last Name:</label>
            <input type="text" name="lastName" className="w-full p-2 border rounded mt-1" value={formData.lastName} onChange={handleChange} required />
          </div>
          <div className="mb-4">
            <label className="block text-gray-700">Email:</label>
            <input type="email" name="email" className="w-full p-2 border rounded mt-1" value={formData.email} onChange={handleChange} required />
          </div>
          <div className="mb-4">
            <label className="block text-gray-700">Password:</label>
            <input type="password" name="password" className="w-full p-2 border rounded mt-1" value={formData.password} onChange={handleChange} required />
          </div>
          <div className="mb-4">
            <label className="block text-gray-700">Phone Number:</label>
            <input type="text" name="phoneNumber" className="w-full p-2 border rounded mt-1" value={formData.phoneNumber} onChange={handleChange} required />
          </div>
          <div className="mb-4">
            <label className="block text-gray-700 mb-2">Role:</label>
            <div className="flex gap-4">
              <button type="button" className={`p-2 w-1/2 rounded ${formData.role === "CUSTOMER" ? "bg-blue-500 text-white" : "bg-gray-200"}`} onClick={() => handleRoleSelect("CUSTOMER")}>
                CUSTOMER
              </button>
              <button type="button" className={`p-2 w-1/2 rounded ${formData.role === "SERVICE PROVIDER" ? "bg-blue-500 text-white" : "bg-gray-200"}`} onClick={() => handleRoleSelect("SERVICE PROVIDER")}>
                SERVICE PROVIDER
              </button>
            </div>
          </div>
          <button type="submit" className="w-full bg-green-500 text-white p-2 rounded hover:bg-green-600" disabled={loading}>
            {loading ? "Registering..." : "Register"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default RegistrationPage;
