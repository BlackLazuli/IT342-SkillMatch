import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { usePersonalInfo } from "./context/PersonalInfoContext";

// Page imports
import LoginPage from "./pages/login/LoginPage";
import RegistrationPage from "./pages/registration/RegistrationPage";
import CustomerDashboard from "./pages/customerdashboard/CustomerDashboard";
import ProviderDashboard from "./pages/providerdashboard/ProviderDashboard";
import PortfolioPage from "./pages/portfolio/PortfolioPage";
import AddPortfolioPage from "./pages/portfolio/AddPortfolioPage";
import EditPortfolioPage from "./pages/portfolio/EditPortfolioPage";
import ProfilePage from "./pages/profile/ProfilePage";
import ProfilePageCustomer from "./pages/profile/ProfilePageCustomer";
import ProviderPortfolioPage from "./pages/portfolio/ProviderPortfolioPage"; // ✅ Imported
import AppointmentDetailsPage from "./pages/appointment/AppointmentDetailsPage";
import AppointmentDetailsCustomerPage from "./pages/appointment/AppointmentDetailsCustomerPage";
import ClientProfilePage from './pages/profile/ClientProfilePage'; 


function App() {
  const { personalInfo } = usePersonalInfo(); // Access context

  // PrivateRoute logic
  const PrivateRoute = ({ children }) => {
    if (personalInfo === null) {
      return <div>Loading...</div>; // Show loading until context is ready
    }

    return personalInfo && personalInfo.userId ? children : <Navigate to="/" />;
  };

  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<LoginPage />} />
        <Route path="/register" element={<RegistrationPage />} />

        {/* Protected Routes */}
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
                <Route
          path="/profile-customer"
          element={
            <PrivateRoute>
              <ProfilePageCustomer />
            </PrivateRoute>
          }
        />
        <Route
          path="/provider-portfolio/:userID"
          element={
            <PrivateRoute>
              <ProviderPortfolioPage />
            </PrivateRoute>
          }
        />

<Route
    path="/appointments/:userID"
    element={
      <PrivateRoute>
        <AppointmentDetailsPage />
      </PrivateRoute>
    }
  />

<Route
  path="/appointments-customer/:userID"
  element={
    <PrivateRoute>
      <AppointmentDetailsCustomerPage />
    </PrivateRoute>
  }
/>

<Route
  path="/client-profile/:userId"
  element={
    <PrivateRoute>
      <ClientProfilePage />
    </PrivateRoute>
  }
/>

      </Routes>

    </Router>
  );
}

export default App;
