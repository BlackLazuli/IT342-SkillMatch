import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { usePersonalInfo } from "./context/PersonalInfoContext"; // Import context
import LoginPage from "./pages/login/LoginPage";
import RegistrationPage from "./pages/registration/RegistrationPage";
import CustomerDashboard from "./pages/customerdashboard/CustomerDashboard";
import ProviderDashboard from "./pages/providerdashboard/ProviderDashboard";
import PortfolioPage from "./pages/portfolio/PortfolioPage"; // Import PortfolioPage
import AddPortfolioPage from "./pages/portfolio/AddPortfolioPage";

function PrivateRoute({ children }) {
  const { personalInfo } = usePersonalInfo(); // ✅ Get correct state

  console.log("Checking personalInfo in PrivateRoute:", personalInfo); // ✅ Debugging log

  return personalInfo && personalInfo.userId ? children : <Navigate to="/" />;
}

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/register" element={<RegistrationPage />} />

        {/* Protect dashboards */}
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

        {/* Protect Portfolio Page */}
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
      </Routes>
    </Router>
  );
}

export default App;
