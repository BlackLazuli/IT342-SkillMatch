import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { usePersonalInfo } from "./context/PersonalInfoContext";
import LoginPage from "./pages/login/LoginPage";
import RegistrationPage from "./pages/registration/RegistrationPage";
import CustomerDashboard from "./pages/customerdashboard/CustomerDashboard";
import ProviderDashboard from "./pages/providerdashboard/ProviderDashboard";
import PortfolioPage from "./pages/portfolio/PortfolioPage";
import AddPortfolioPage from "./pages/portfolio/AddPortfolioPage";
import EditPortfolioPage from "./pages/portfolio/EditPortfolioPage"; // ✅ Added
import ProfilePage from "./pages/profile/ProfilePage"; // ✅ Added

function PrivateRoute({ children }) {
  const { personalInfo } = usePersonalInfo();
  console.log("Checking personalInfo in PrivateRoute:", personalInfo);
  return personalInfo && personalInfo.userId ? children : <Navigate to="/" />;
}

function App() {
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
        {/* ✅ Edit Portfolio Page route */}
        <Route
          path="/edit-portfolio/:userID"
          element={
            <PrivateRoute>
              <EditPortfolioPage />
            </PrivateRoute>
          }
        />

        {/* ✅ Profile Page route */}
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
