import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

const AddPortfolioPage = () => {
  const { userID } = useParams(); // ✅ Get userID from URL
  const navigate = useNavigate();
  const token = localStorage.getItem("token"); // ✅ Get token from localStorage

  const [portfolioData, setPortfolioData] = useState({
    workExperience: "",
    servicesOffered: "",
    clientTestimonials: "",
  });

  const handleChange = (e) => {
    setPortfolioData({ ...portfolioData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!token) {
      alert("No authentication token found. Please log in again.");
      return;
    }

    try {
      const response = await fetch(`http://localhost:8080/api/portfolios/createPortfolio/${userID}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify(portfolioData),
      });

      if (!response.ok) {
        throw new Error("Failed to add portfolio.");
      }

      alert("Portfolio added successfully!");
      navigate(`/portfolio/${userID}`); // ✅ Redirect to user's portfolio page
    } catch (error) {
      console.error("Error adding portfolio:", error);
      alert("Error adding portfolio.");
    }
  };

  return (
    <div>
      <h1>Add Portfolio</h1>
      <form onSubmit={handleSubmit}>
        <label>
          Work Experience:
          <input type="text" name="workExperience" value={portfolioData.workExperience} onChange={handleChange} required />
        </label>
        <br />
        <label>
          Services Offered:
          <input type="text" name="servicesOffered" value={portfolioData.servicesOffered} onChange={handleChange} required />
        </label>
        <br />
        <label>
          Client Testimonials:
          <textarea name="clientTestimonials" value={portfolioData.clientTestimonials} onChange={handleChange} required />
        </label>
        <br />
        <button type="submit">Submit</button>
      </form>
    </div>
  );
};

export default AddPortfolioPage;
