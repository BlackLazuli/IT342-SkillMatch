import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

const PortfolioPage = () => {
  const { userID } = useParams();
  const navigate = useNavigate();
  const [portfolio, setPortfolio] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchPortfolio = async () => {
      const token = localStorage.getItem("token");

      if (!token) {
        alert("Please log in first.");
        return;
      }

      try {
        const response = await fetch(`http://localhost:8080/api/portfolios/getPortfolio/${userID}`, {
          headers: {
            "Authorization": `Bearer ${token}`,
          },
        });

        if (response.status === 404) {
          console.warn("No portfolio found for this user.");
          setPortfolio(null); // Explicitly set portfolio to null when not found
          setLoading(false);
          return;
        }

        if (!response.ok) {
          throw new Error(`Error: ${response.status} - ${response.statusText}`);
        }

        const portfolioData = await response.json();
        console.log("Portfolio data:", portfolioData);

        setPortfolio(portfolioData);
      } catch (error) {
        console.error("Error fetching portfolio:", error);
        setError("Failed to load portfolio.");
      } finally {
        setLoading(false);
      }
    };

    fetchPortfolio();
  }, [userID]);

  const handleAddPortfolio = () => {
    const token = localStorage.getItem("token");
    navigate(`/add-portfolio/${userID}`);
// ✅ Pass userID and token via state
  };

  if (loading) return <p>Loading portfolio...</p>;
  if (error) return <p>Error: {error}</p>;

  // If portfolio is null, show the Add Portfolio button
  if (!portfolio) {
    return (
      <div>
        <h1>Portfolio</h1>
        <p>No portfolio found for this user.</p>
        <button onClick={handleAddPortfolio}>Add Portfolio</button>
      </div>
    );
  }

  return (
    <div>
      <h1>Portfolio</h1>
      <h2>Work Experience</h2>
      <p>{portfolio?.workExperience || "No work experience provided."}</p>

      <h2>Services Offered</h2>
      <p>{portfolio?.servicesOffered || "No services listed."}</p>

      <h2>Client Testimonials</h2>
      <p>{portfolio?.clientTestimonials || "No testimonials available."}</p>

      <h2>Comments</h2>
      {portfolio?.comments && portfolio.comments.length > 0 ? (
        <ul>
          {portfolio.comments.map((comment, index) => (
            <li key={index}>{comment}</li>
          ))}
        </ul>
      ) : (
        <p>No comments available.</p>
      )}
    </div>
  );
};

export default PortfolioPage;
