import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { usePersonalInfo } from "./context/PersonalInfoContext";
import LoginPage from "./pages/login/LoginPage";
import RegistrationPage from "./pages/registration/RegistrationPage";
import CustomerDashboard from "./pages/customerdashboard/CustomerDashboard";
import ProviderDashboard from "./pages/providerdashboard/ProviderDashboard";
import PortfolioPage from "./pages/portfolio/PortfolioPage";
import AddPortfolioPage from "./pages/portfolio/AddPortfolioPage";
import EditPortfolioPage from "./pages/portfolio/EditPortfolioPage"; 
import ProfilePage from "./pages/profile/ProfilePage";

function App() {
  const { personalInfo } = usePersonalInfo(); // Access context

  // PrivateRoute equivalent logic
  const PrivateRoute = ({ children }) => {
    if (personalInfo === null) {
      return <div>Loading...</div>; // Show loading state until personalInfo is set
    }

    return personalInfo && personalInfo.userId ? children : <Navigate to="/" />;
  };

  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/register" element={<RegistrationPage />} />

        <Route
          path="/customer-dashboard"
          element={
            <PrivateRoute>
              <CustomerDashboard />
            </PrivateRoute>
          }
        />
        <Route
          path="/provider-dashboard"
          element={
            <PrivateRoute>
              <ProviderDashboard />
            </PrivateRoute>
          }
        />
        <Route
          path="/portfolio/:userID"
          element={
            <PrivateRoute>
              <PortfolioPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/add-portfolio/:userID"
          element={
            <PrivateRoute>
              <AddPortfolioPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/edit-portfolio/:userID"
          element={
            <PrivateRoute>
              <EditPortfolioPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <PrivateRoute>
              <ProfilePage />
            </PrivateRoute>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
